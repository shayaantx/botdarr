node {
	stage("Checkout") {
		checkout scm
	}

	stage("Package") {
	  sh 'chmod 700 mvnw'
	  sh './mvnw package'
	}
}