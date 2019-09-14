node {
	stage("Checkout") {
		checkout scm
	}

	stage("Package") {
	  sh './mvnw package'
	}
}