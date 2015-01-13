#!/bin/bash
# Kina Deployment script

SPARK_REPO="$1"

if [ -z "$1" ]; then
    SPARK_REPO="git@github.com:apache/spark.git"
fi

SPARK_BRANCH="$2"

if [ -z "$2" ]; then
    SPARK_BRANCH="v1.1.1"
fi

LOCAL_EDITOR=$(which vi)

if [ -z "$LOCAL_EDITOR" ]; then
    $LOCAL_EDITOR=$(which vi)
fi

if [ -z "$LOCAL_EDITOR" ]; then
    echo "Cannot find any command line editor, ChangeLog.txt won't be edited interactively"
fi

LOCAL_DIR=`pwd`

echo "LOCAL_DIR=$LOCAL_DIR"

TMPDIR=/tmp/kina-clone
TMPDIR_SPARK=/tmp/spark-clone

cd ${TMPDIR}
RELEASE_VER=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version 2>/dev/null | grep -v '\[' | sed s/\-SNAPSHOT//) || { echo "Cannot generate next version number"; exit 1; }

rm -rf ${TMPDIR_SPARK}
mkdir -p ${TMPDIR_SPARK}

mvn -version >/dev/null || { echo "Cannot find Maven in path, aborting"; exit 1; }

# Clone spark repo from github
git clone ${SPARK_REPO} ${TMPDIR_SPARK} || { echo "Cannot clone spark repo from github"; exit 1; }

cd ${TMPDIR_SPARK}
git checkout "$SPARK_BRANCH" || { echo "Cannot checkout branch: ${SPARK_BRANCH}"; exit 1; }

# Apply patch
patch -p1 < ${TMPDIR}/kina-scripts/spark-patches/kina-spark-${SPARK_BRANCH}-patches.diff

chmod +x bin/kina-shell

echo " >>> Executing make distribution script"
##  --skip-java-test has been added to Spark 1.1.1, avoids prompting the user about not having JDK 6 installed
./make-distribution.sh --name --skip-java-test -DskipTests -Phadoop-2.4 -Pyarn || { echo "Cannot make Spark distribution"; exit 1; }

DISTDIR=kina-distribution-${RELEASE_VER}
DISTFILENAME=${DISTDIR}.tgz

cp ${TMPDIR}/lib/*.jar ${TMPDIR_SPARK}/dist/lib/
rm -f ${TMPDIR_SPARK}/dist/lib/*-sources.jar
rm -f ${TMPDIR_SPARK}/dist/lib/*-javadoc.jar
rm -f ${TMPDIR_SPARK}/dist/lib/*-tests.jar

mv ${TMPDIR_SPARK}/dist/ ${DISTDIR}
cp ${TMPDIR_SPARK}/LICENSE ${DISTDIR}
cp ${TMPDIR}/ChangeLog.txt ${DISTDIR}/

echo "DISTFILENAME: ${DISTFILENAME}"

tar czf ${DISTFILENAME} ${DISTDIR} || { echo "Cannot create tgz"; exit 1; }

mv ${DISTFILENAME} ${LOCAL_DIR} || { echo "Cannot move tar.gz file"; exit 1; }

cd "$LOCAL_DIR"

echo " >>> Finishing"

cd ..

# Delete cloned spark project
# rm -rf ${TMPDIR}  || { echo "Cannot remove kina-clone project"; exit 1; }

# Delete cloned Kina project
# rm -rf ${TMPDIR_SPARK}  || { echo "Cannot remove spark-clone project"; exit 1; }

cd "$LOCAL_DIR"/..
# git checkout develop

echo " >>> SCRIPT EXECUTION FINISHED <<< "
