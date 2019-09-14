def dockerFileContents = """
FROM centos:7
RUN yum update; yum clean all; yum -y install nano; yum -y install less;
RUN yum -y install java-1.8.0-openjdk-devel-debug.x86_64; yum -y install java-1.8.0-openjdk-src-debug.x86_64;
RUN yum -y install dos2unix;
RUN yum -y install net-tools;
RUN yum -y install openssh;
RUN yum -y install maven
RUN adduser jenkins
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
""";

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
			archive 'target/botdar-release.jar'
		}
	}
	
    stage("Cleanup") {
        deleteDir();
    }
}