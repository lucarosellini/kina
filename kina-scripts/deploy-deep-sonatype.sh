#!/bin/bash
# deploy kina jars to oss.sonatype.org staging repository

cd ..

git checkout master
git pull

RELEASE_VER=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version 2>/dev/null | grep -v '\[' | sed s/\-SNAPSHOT//) || { echo "Cannot generate next version number"; exit 1; }

if [ -z "$RELEASE_VER" ]; then
    echo "Cannot parse version number";
    exit 1;
fi

echo "Version number: ${RELEASE_VER}"
echo "Compiling branch master"
mvn -q clean package -DskipTests

read -p "Insert GPG passphrase: " passphrase

if [ -z "$passphrase" ]; then
    echo "GPG passphrase is mandatory";
    exit 1;
fi

mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml -Dfile=pom.xml -Dpackaging=pom -Dgpg.passphrase=$passphrase

cd ./kina-commons
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml -Dfile=target/kina-commons-${RELEASE_VER}.jar -Dgpg.passphrase=$passphrase
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml -Dfile=target/kina-commons-${RELEASE_VER}-sources.jar -Dclassifier=sources -Dgpg.passphrase=$passphrase
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml -Dfile=target/kina-commons-${RELEASE_VER}-javadoc.jar -Dclassifier=javadoc -Dgpg.passphrase=$passphrase

cd ../kina-agent
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml -Dfile=target/kina-agent-${RELEASE_VER}.jar -Dgpg.passphrase=$passphrase
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml -Dfile=target/kina-agent-${RELEASE_VER}-sources.jar -Dclassifier=sources -Dgpg.passphrase=$passphrase
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml -Dfile=target/kina-agent-${RELEASE_VER}-javadoc.jar -Dclassifier=javadoc -Dgpg.passphrase=$passphrase


cd ../kina-cassandra
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml -Dfile=target/kina-cassandra-${RELEASE_VER}.jar -Dgpg.passphrase=$passphrase
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml -Dfile=target/kina-cassandra-${RELEASE_VER}-sources.jar -Dclassifier=sources -Dgpg.passphrase=$passphrase
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml -Dfile=target/kina-cassandra-${RELEASE_VER}-javadoc.jar -Dclassifier=javadoc -Dgpg.passphrase=$passphrase

cd ../kina-mongodb
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml -Dfile=target/kina-mongodb-${RELEASE_VER}.jar -Dgpg.passphrase=$passphrase
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml -Dfile=target/kina-mongodb-${RELEASE_VER}-sources.jar -Dclassifier=sources -Dgpg.passphrase=$passphrase
mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=pom.xml -Dfile=target/kina-mongodb-${RELEASE_VER}-javadoc.jar -Dclassifier=javadoc -Dgpg.passphrase=$passphrase
