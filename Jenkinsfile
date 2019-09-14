node {
	stage("Checkout") {
		checkout scm
	}

	stage("Package") {
	  sh 'mvwn package'
	}
}