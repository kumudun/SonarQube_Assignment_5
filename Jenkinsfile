pipeline {
    agent any

    tools {

        maven 'Maven 3'

    }
    environment {
        PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"
        JAVA_HOME =  "C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.10.7-hotspot"
        SONARQUBE_SERVER = 'SonarQubeServer'
        SONAR_TOKEN = credentials('SONAR_TOKEN')
        DOCKERHUB_CREDENTIALS_ID = 'Docker-Hub'
        DOCKERHUB_REPO = 'kumudun/sonarqube_assignment_5'
        DOCKER_IMAGE_TAG = 'latest'


    }
    stages{
        stage('check'){
            steps {
                git branch: 'main', url: 'https://github.com/kumudun/SonarQube_Assignment_5.git'
            }
        }

        stage('build job: '){
            steps {
                bat  'mvn clean install'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeServer') {
                    bat """
                ${tool 'SonarScanner'}\\bin\\sonar-scanner ^
                -Dsonar.projectKey=assignment5 ^
                -Dsonar.projectName=Assignment_5 ^
                -Dsonar.projectVersion=1.0 ^
                -Dsonar.sources=src/main/java ^
                -Dsonar.tests=src/test/java ^
                -Dsonar.java.binaries=target/classes ^
                -Dsonar.java.test.binaries=target/test-classes ^
                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
            """
                }
            }
        }

        stage('test'){
            steps {
                bat 'mvn test'
            }
        }
        stage('Report'){
            steps {
                bat 'mvn jacoco:report'
            }
        }

        stage('Publish Test Results') {
            steps {
                junit '**/target/surefire-reports/*.xml'
            }
        }
        stage('Publish Coverage Report') {
            steps {
                jacoco()
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}")
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKERHUB_CREDENTIALS_ID) {
                        docker.image("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}").push()
                    }
                }
            }
        }



    }
}