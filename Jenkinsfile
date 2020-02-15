def dockerFileContents = """
FROM centos:7
RUN yum update clean all
RUN yum -y install java-1.8.0-openjdk-devel.x86_64
RUN yum -y install maven
RUN adduser jenkins
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
""";

def getChangelistDescription() {
  def description = "";
  def changeLogSets = currentBuild.changeSets;
  for (int i = 0; i < changeLogSets.size(); i++) {
    def entries = changeLogSets[i].items;
    for (int j = 0; j < entries.length; j++) {
      def entry = entries[j]
      description += "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}\n";
    }
  }
  return description;
}

def getNextVersion(scope) {
  def latestVersion = sh returnStdout: true, script: 'git tag | sort -V | tail -1';
  print "version=" + latestVersion;
  def (major, minor, patch) = latestVersion.tokenize('.').collect { it.toInteger() };
  print "major=" + major + ",minor=" + minor + ",patch=" + patch;
  if (scope == 'release') {
    def newMinor = minor + 1;
    def newMajor = major;
    if (newMinor > 10) {
      newMinor = 0;
      newMajor = major + 1;
    }
    return "${newMinor}.${newMinor}.0";
  } else {
    return "${major}.${minor}.${patch + 1}";
  }
}

node {
  try {
    stage("Checkout") {
      checkout([$class: 'GitSCM', branches: [[name: '**']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'github', url: 'https://github.com/shayaantx/botdar.git']]])
    }

    stage('Prepare docker') {
      fileOperations([fileCreateOperation(fileContent: "${dockerFileContents}", fileName: './Dockerfile')]);
    }

    def tag = getNextVersion('development');
    if (env.BRANCH_NAME == "master") {
      tag = getNextVersion('release');
    }
    def image = docker.build("botdarr-image", "-f ./Dockerfile .");
    image.inside('-u root') {
      stage('Build') {
        sh './mvnw --no-transfer-progress compile'
      }

      stage("Test") {
        sh './mvnw --no-transfer-progress test'
      }

      stage("Package") {
        fileOperations([fileCreateOperation(fileContent: "version=${tag}", fileName: './src/main/resources/version.txt')]);
        sh './mvnw --no-transfer-progress package -DskipTests'
      }

      stage("Archive") {
        archiveArtifacts 'target/botdarr-release.jar'
      }

      if (env.BRANCH_NAME == "master" || env.BRANCH_NAME == "development") {
        //don't upload for PR's
        stage('Create/Upload Release') {
          withCredentials([string(credentialsId: 'git-token', variable: 'token')]) {
            def description = getChangelistDescription();
            print "description=" + description;
            print "branch name=" + env.BRANCH_NAME;
            print "tag=" + tag;
            sh 'chmod 700 upload-release.sh'
            sh "./upload-release.sh ${token} ${tag} \"${description}\""
          }
        }
      }
    }

    if (env.BRANCH_NAME == "master" || env.BRANCH_NAME == "development") {
      //don't upload for PR's
      stage('Upload to dockerhub') {
        def dockerFileImage = """
        FROM centos:7
        RUN yum update; yum clean all; yum -y install java-1.8.0-openjdk-devel.x86_64;
        ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
        ENV PATH=$PATH:$JAVA_HOME/bin

        RUN mkdir -p /home/botdarr
        ADD target/botdarr-release.jar /home/botdarr

        WORKDIR /home/botdarr
        RUN java -version

        ENTRYPOINT ["java", "-jar", "botdarr-release.jar"]
        """;
        fileOperations([fileCreateOperation(fileContent: "${dockerFileImage}", fileName: './DockerfileUpload')]);
        def releaseTag = env.BRANCH_NAME == "master" ? "stable" : "latest";
        def imageWithReleaseTag = docker.build("shayaantx/botdarr:${releaseTag}", "-f ./DockerfileUpload .");
        withDockerRegistry(credentialsId: 'docker-credentials') {
          imageWithReleaseTag.push();
        }
      }
    }
  } finally {
    stage("Cleanup") {
      deleteDir();
    }
  }
}