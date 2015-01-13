#!/bin/bash
# Kina Deployment script

KINA_REPO="git@github.com:lucarosellini/kina.git"

echo " >>> Kina DEPLOYMENT <<< "

TAGVERSION="true"

LOCAL_EDITOR=$(which vim)

if [ -z "$LOCAL_EDITOR" ]; then
    LOCAL_EDITOR=$(which vi)
fi

if [ -z "$LOCAL_EDITOR" ]; then
    echo "Cannot find any command line editor, ChangeLog.txt won't be edited interactively"
fi

LOCAL_DIR=`pwd`

echo "LOCAL_DIR=$LOCAL_DIR"

TMPDIR=/tmp/kina-clone

rm -rf ${TMPDIR}
mkdir -p ${TMPDIR}

mvn -version >/dev/null || { echo "Cannot find Maven in path, aborting"; exit 1; }

#### Create Kina jars from github (master tag) through maven release plugin

# Clone Kina (master tag) from github
git clone ${KINA_REPO} ${TMPDIR} || { echo "Cannot clone kina project"; exit 1; }
cd ${TMPDIR}

git checkout develop

echo "Generating version number"
RELEASE_VER=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version 2>/dev/null | grep -v '\[' | sed s/\-SNAPSHOT//) || { echo "Cannot generate next version number"; exit 1; }

cd ${TMPDIR}/

echo "Updating pom version numbers"
cd ${TMPDIR}/
mvn -q versions:set -DnewVersion=${RELEASE_VER} || { echo "Cannot modify pom file with next version number"; exit 1; }

echo "Building Kina ${RELEASE_VER} ..."
mvn -q clean package -DskipTests || { echo "Cannot build Kina $RELEASE_VER "; exit 1; }
#mvn -q test || { echo "Tests failed for Kina $RELEASE_VER "; exit 1; }

#fix version number for travis CI
sed -i -e s/\?branch=develop\)/\?branch=version-${RELEASE_VER}/ README.md

find . -name 'pom.xml.versionsBackup' | xargs rm

# copy libs
mkdir -p ${TMPDIR}/lib || { echo "Cannot create output lib directory"; exit 1; }

cp  -b ./kina-agent/target/*.jar ${TMPDIR}/lib || { echo "Cannot copy kina-agent jars to output lib directory, aborting"; exit 1; }
cp  -b ./kina-commons/target/*.jar ${TMPDIR}/lib || { echo "Cannot copy kina-commons jars to output lib directory, aborting"; exit 1; }
cp  -b ./kina-cassandra/target/*.jar ${TMPDIR}/lib || { echo "Cannot copy kina-cassandra jars to output lib directory, aborting"; exit 1; }
cp  -b ./kina-cassandra/target/alternateLocation/*.jar ${TMPDIR}/lib || { echo "Cannot copy kina-cassandra alternate jars to output lib directory, aborting"; exit 1; }
cp  -b ./kina-mongodb/target/*.jar ${TMPDIR}/lib || { echo "Cannot copy kina-mongodb jars to output lib directory, aborting"; exit 1; }
cp  -b ./kina-mongodb/target/alternateLocation/*.jar ${TMPDIR}/lib || { echo "Cannot copy kina-mongodb alternate jars to output lib directory, aborting"; exit 1; }

rm -f ${TMPDIR}/lib/*.jar~

if [ "false" == $TAGVERSION ]; then
    echo "Kina ${RELEASE_VER} built successfully"
    exit 0
fi

git commit -a -m "[kina release prepare] preparing for version ${RELEASE_VER}"  || { echo "Cannot commit changes in kina-clone project"; exit 1; }

git flow init -d || { echo "Cannot initialize git flow in kina-clone project"; exit 1; }
git flow release start version-$RELEASE_VER || { echo "Cannot create $RELEASE_VER branch"; exit 1; }

echo " >>> Uploading new release branch to remote repository"
git flow release publish version-$RELEASE_VER || { echo "Cannot publish $RELEASE_VER branch"; exit 1; }

# Generating ChangeLog
git fetch --tags
latest_tag=$(git describe --tags `git rev-list --tags --max-count=1`)

touch ChangeLog.txt
git add ChangeLog.txt

echo -e "[${RELEASE_VER}]\n\n$(git log ${latest_tag}..HEAD)\n\n$(cat ChangeLog.txt)" > ChangeLog.txt

if [ -n "$LOCAL_EDITOR" ]; then
    $LOCAL_EDITOR ChangeLog.txt
fi

echo "Finishing release ${RELEASE_VER}"
mvn -q clean

git commit -a -m "[Updated ChangeLog.txt for release ${RELEASE_VER}]"

echo "Generating next SNAPSHOT version"
curr_version=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version 2>/dev/null | grep -v '\[')
major=$(echo $curr_version | cut -d "." -f 1)
minor=$(echo $curr_version | cut -d "." -f 2)
bugfix=$(echo $curr_version | cut -d "." -f 3)

next_bugfix=$(expr $bugfix + 1)

next_version="${major}.${minor}.${next_bugfix}-SNAPSHOT"

echo "Next SNAPSHOT version: ${next_version}"

echo "Finishing release"
git flow release finish -k -mFinishing_Release_$RELEASE_VER version-$RELEASE_VER || { echo "Cannot finish Kina ${next_version}"; exit 1; }
git push --tags

git checkout master
git push origin || { echo "Cannot push to master"; exit 1; }

git checkout develop

echo "Setting new snapshot version"
mvn versions:set -DnewVersion=${next_version} || { echo "Cannot set new version: ${next_version}"; exit 1; }

#fix version number for travis CI
sed -i -e s/\?branch=version-${RELEASE_VER}/\?branch=develop\)/ README.md

find . -name 'pom.xml.versionsBackup' | xargs rm

echo "Commiting version ${next_version}"
git commit -a -m "[kina release finish] next snapshot version ${next_version}" || { echo "Cannot commit new changes for ${next_version}"; exit 1; }

git push origin || { echo "Cannot push new version: ${next_version}"; exit 1; }

echo "Success: new version tagged in github as 'version-${RELEASE_VER}'"