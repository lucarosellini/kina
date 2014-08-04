#!/bin/bash
# Kina Deployment script

KINA_REPO="git@github.com:lucarosellini/kina.git"

echo " >>> Kina DEPLOYMENT <<< "

SPARK_REPO="$1"

if [ -z "$1" ]; then
    SPARK_REPO="git@github.com:apache/spark.git"
fi

SPARK_BRANCH="$2"

if [ -z "$2" ]; then
    SPARK_BRANCH="v1.0.1"
fi

LOCAL_EDITOR=$(which vim)

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

rm -rf ${TMPDIR}
mkdir -p ${TMPDIR}

rm -rf ${TMPDIR_SPARK}
mkdir -p ${TMPDIR_SPARK}

mvn -version >/dev/null || { echo "Cannot find Maven in path, aborting"; exit 1; }

#### Create Deep jars from github (master tag) through maven release plugin

# Clone Deep (master tag) from github
git clone ${KINA_REPO} ${TMPDIR} || { echo "Cannot clone deep project"; exit 1; }
cd ${TMPDIR}

git checkout develop

cd deep-parent
echo "Generating version number"
RELEASE_VER=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version 2>/dev/null | grep -v '\[' | sed s/\-SNAPSHOT//) || { echo "Cannot generate next version number"; exit 1; }

cd ${TMPDIR}/

git flow init -d || { echo "Cannot initialize git flow in kina-clone project"; exit 1; }
git flow release start version-$RELEASE_VER || { echo "Cannot create $RELEASE_VER branch"; exit 1; }

git status

echo "Updating pom version numbers"
cd ${TMPDIR}/deep-parent/
mvn versions:set -DnewVersion=${RELEASE_VER} || { echo "Cannot modify pom file with next version number"; exit 1; }

cd ..

find . -name 'pom.xml.versionsBackup' | xargs rm

git commit -a -m "[kina release prepare] preparing for version ${RELEASE_VER}"  || { echo "Cannot commit changes in kina-clone project"; exit 1; }

echo " >>> Uploading new release branch to remote repository"
git flow release publish version-$RELEASE_VER || { echo "Cannot publish $RELEASE_VER branch"; exit 1; }

cd deep-parent

mvn clean package || { echo "Cannot deploy $RELEASE_VER of Kina"; exit 1; }

mkdir -p ${TMPDIR}/lib || { echo "Cannot create output lib directory"; exit 1; }

cp ../*/target/*.jar ${TMPDIR}/lib || { echo "Cannot copy target jars to output lib directory, aborting"; exit 1; }
cp ../*/target/alternateLocation/*.jar ${TMPDIR}/lib || { echo "Cannot copy alternate jars to output lib directory, aborting"; exit 1; }

# Generating ChangeLog
git fetch --tags
latest_tag=$(git describe --tags `git rev-list --tags --max-count=1`)

echo -e "[${RELEASE_VER}]\n\n$(git log ${latest_tag}..HEAD)\n\n$(cat ChangeLog.txt)" > ChangeLog.txt

if [ -n "$LOCAL_EDITOR" ]; then
    $LOCAL_EDITOR ChangeLog.txt
fi

echo "Finishing release ${RELEASE_VER}"
mvn clean

git commit -a -m "[Updated ChangeLog.txt for release ${RELEASE_VER}]"

echo "Generating next SNAPSHOT version"
curr_version=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version 2>/dev/null | grep -v '\[')
major=$(echo $curr_version | cut -d "." -f 1)
minor=$(echo $curr_version | cut -d "." -f 2)
bugfix=$(echo $curr_version | cut -d "." -f 3)

next_bugfix=$(expr $bugfix + 1)

next_version="${major}.${minor}.${next_bugfix}-SNAPSHOT"

echo "Next SNAPSHOT version: ${next_version}"

cd .. 

echo "Finishing release"
git flow release finish -k -mFinishing_Release_$RELEASE_VER version-$RELEASE_VER || { echo "Cannot finish Kina ${next_version}"; exit 1; }
git push --tags

git checkout master
git push origin || { echo "Cannot push to master"; exit 1; }

git checkout develop

cd deep-parent

echo "Setting new snapshot version"
mvn versions:set -DnewVersion=${next_version} || { echo "Cannot set new version: ${next_version}"; exit 1; }
cd ..

find . -name 'pom.xml.versionsBackup' | xargs rm

echo "Commiting next_version"
git commit -a -m "[kina release finish] next snapshot version ${next_version}" || { echo "Cannot commit new changes for ${next_version}"; exit 1; }

git push origin || { echo "Cannot push new version: ${next_version}"; exit 1; }

echo "RELEASE_VER=$RELEASE_VER"
#echo "CASS_VER=$CASS_VER"

# Clone spark repo from github
git clone ${SPARK_REPO} ${TMPDIR_SPARK} || { echo "Cannot clone spark repo from github"; exit 1; }

cd ${TMPDIR_SPARK}
git checkout "$SPARK_BRANCH" || { echo "Cannot checkout branch: ${SPARK_BRANCH}"; exit 1; }

chmod +x bin/kina-shell

echo " >>> Executing make distribution script"
##  --skip-java-test has been added to Spark 1.0.0, avoids prompting the user about not having JDK 6 installed
./make-distribution.sh --skip-java-test --hadoop 2.4.0 --with-yarn || { echo "Cannot make Spark distribution"; exit 1; }

DISTDIR=spark-deep-distribution-${RELEASE_VER}
DISTFILENAME=${DISTDIR}.tgz

cp ${TMPDIR}/lib/*.jar ${TMPDIR_SPARK}/dist/lib/
rm -f ${TMPDIR_SPARK}/dist/lib/*-sources.jar
rm -f ${TMPDIR_SPARK}/dist/lib/*-javadoc.jar
rm -f ${TMPDIR_SPARK}/dist/lib/*-tests.jar

mv ${TMPDIR_SPARK}/dist/ ${DISTDIR}
cp ${TMPDIR_SPARK}/LICENSE ${DISTDIR}
cp ${TMPDIR}/deep-parent/ChangeLog.txt ${DISTDIR}/

echo "DISTFILENAME: ${DISTFILENAME}"

tar czf ${DISTFILENAME} ${DISTDIR} || { echo "Cannot create tgz"; exit 1; }

mv ${DISTFILENAME} ${LOCAL_DIR} || { echo "Cannot move tar.gz file"; exit 1; }

cd "$LOCAL_DIR"

echo " >>> Finishing"

cd ..

# Delete cloned spark project
rm -rf ${TMPDIR}  || { echo "Cannot remove kina-clone project"; exit 1; }

# Delete cloned Kina project
rm -rf ${TMPDIR_SPARK}  || { echo "Cannot remove spark-clone project"; exit 1; }

cd "$LOCAL_DIR"/..
git checkout develop

echo " >>> SCRIPT EXECUTION FINISHED <<< "
