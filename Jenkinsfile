def dockerFileContents = """
FROM centos:7
RUN yum update; yum clean all;
RUN yum -y install java-1.8.0-openjdk-devel-debug.x86_64; yum -y install java-1.8.0-openjdk-src-debug.x86_64;
RUN yum -y install maven
RUN adduser jenkins
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
""";

function getChangelistDescription() {
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

node {
	stage("Checkout") {
		checkout scm
	}

	stage('Prepare docker') {
		fileOperations([fileCreateOperation(fileContent: "${dockerFileContents}", fileName: './Dockerfile')]);
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

		stage('Create Release') {
		  def description = getChangelistDescription();
		  withCredentials([string(credentialsId: 'git-token', variable: 'token')]) {
        sh label: '', script: """
          token="${token}"
          # Get the last tag name
          tag="1.0.0"
          name="1.0.0"
          description=$(echo "${description}" | sed -z \'s/\\n/\\\\n/g\') # Escape line breaks to prevent json parsing problems
          # Create a release
          release=$(curl -XPOST -H "Authorization:token $token" --data "{\\"tag_name\\": \\"$tag\\", \\"target_commitish\\": \\"master\\", \\"name\\": \\"$name\\", \\"body\\": \\"$description\\", \\"draft\\": false, \\"prerelease\\": true}" https://api.github.com/repos/shayaantx/botdar/releases)
        """
      }
		}

		stage('Upload Release') {

		}
	}
	
  stage("Cleanup") {
      deleteDir();
  }
}