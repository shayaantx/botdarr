def dockerFileContents = """
FROM centos:7
RUN yum update; yum clean all;
RUN yum -y install java-1.8.0-openjdk-devel-debug.x86_64; yum -y install java-1.8.0-openjdk-src-debug.x86_64;
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
          description += "${entry.commitId} by ${entry.author} on ${new Date(entry.timestamp)}: ${entry.msg}";
      }
  }
  return description;
}

def getNextVersion(scope) {
  sh 'chmod 700 get-next-version.sh';
  sh 'git describe --tags';
  def latestVersion = sh returnStdout: true, script: './get-next-version.sh';
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
      checkout([$class: 'GitSCM', branches: [[name: '**']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'git-user', url: 'https://github.com/shayaantx/botdar.git']]])
    }

    stage('Prepare docker') {
      fileOperations([fileCreateOperation(fileContent: "${dockerFileContents}", fileName: './Dockerfile')]);
    }

    def tag = getNextVersion('development');
    if (env.BRANCH_NAME == "master") {
      tag = getNextVersion('release');
    }
    def image = docker.build("botdar-image", "-f ./Dockerfile .");
    image.inside('-u root') {
      stage('Build') {
        sh 'mvn -version'
        sh 'mvn compile'
      }

      stage("Package") {
        sh 'mvn package'
      }

      stage("Archive") {
        archiveArtifacts 'target/botdar-release.jar'
      }

      stage('Create/Upload Release') {
        withCredentials([string(credentialsId: 'git-token', variable: 'token')]) {
          def description = getChangelistDescription();
          print "branch name=" + env.BRANCH_NAME;
          print "tag=" + tag;
          sh 'chmod 700 upload-release.sh'
          sh "./upload-release.sh ${token} ${tag} ${description}"
        }
      }
    }
	} finally {
    stage("Cleanup") {
      deleteDir();
    }
	}
}