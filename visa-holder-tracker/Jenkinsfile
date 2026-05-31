pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh './mvnw clean compile'
            }
        }

        stage('Test') {
            steps {
                sh './mvnw test'
            }
        }

        stage('Coverage') {
            steps {
                sh './mvnw jacoco:report'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t visa-holder-tracker .'
            }
        }
    }
}
