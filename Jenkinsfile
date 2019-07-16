pipeline {
    agent {
        dockerfile {
            filename 'build.Dockerfile'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    environment {
        IMAGE_CREDS_JENKINS_ID = 'aks-datavault'
        IMAGE_REGISTRY = 'datavault.azurecr.io'
        IMAGE_REPOSITORY_WEBAPP = 'webapp'
        IMAGE_REPOSITORY_BROKER = 'broker'
        IMAGE_REPOSITORY_WORKER = 'worker'
        DEPLOYMENT_JOB = '../datavault-infra/PR-1'
        DEPLOYMENT_ENV = 'dev'
    }

    options {
        ansiColor('xterm')
        timestamps()
    }

    stages {
        stage('general linting') {
            steps {
                sh 'pre-commit install'
                sh 'pre-commit run --all-files --verbose || true'
            }
        }

        stage('dockerfile linting') {
            agent {
                docker {
                    image 'hadolint/hadolint:latest-debian'
                }
            }
            steps {
                sh 'hadolint *.Dockerfile | tee -a hadolint_lint.txt'
            }
            post {
                always {
                    archiveArtifacts 'hadolint_lint.txt'
                }
            }
        }

        stage('test') {
            steps {
                sh 'mvn clean test -U --batch-mode'
            }
        }

        stage('build images') {
            steps {
                sh 'docker build -t datavault/maven-build:latest -f maven.Dockerfile .'
                sh 'docker build -t $IMAGE_REPOSITORY_WEBAPP:latest -f webapp.Dockerfile .'
                sh 'docker build -t $IMAGE_REPOSITORY_BROKER:latest -f broker.Dockerfile .'
                sh 'docker build -t $IMAGE_REPOSITORY_WORKER:latest -f worker.Dockerfile .'
            }
        }

        stage('push images') {
            //when {
            //    branch "master"
            //}

            steps {
                withCredentials([usernamePassword(credentialsId: "$IMAGE_CREDS_JENKINS_ID", usernameVariable: 'IMAGE_REGISTRY_USERNAME', passwordVariable: 'IMAGE_REGISTRY_PASSWORD')]) {
                    sh 'docker login $IMAGE_REGISTRY --username $IMAGE_REGISTRY_USERNAME --password $IMAGE_REGISTRY_PASSWORD'
                }

                script {
                    def properties = readProperties(file: 'version.properties')
                    version = "${properties.version}-${currentBuild.startTimeInMillis}.${currentBuild.number}"

                    def webappImages = [
                        "\$IMAGE_REGISTRY/\$IMAGE_REPOSITORY_WEBAPP:$version",
                        "\$IMAGE_REGISTRY/\$IMAGE_REPOSITORY_WEBAPP:latest"
                    ]

                    for (String webappImage : webappImages) {
                        sh "docker tag \$IMAGE_REPOSITORY_WEBAPP:latest $webappImage"
                        sh "docker push $webappImage"
                    }

                    def workerImages = [
                        "\$IMAGE_REGISTRY/\$IMAGE_REPOSITORY_WORKER:$version",
                        "\$IMAGE_REGISTRY/\$IMAGE_REPOSITORY_WORKER:latest"
                    ]

                    for (String workerImage : workerImages) {
                        sh "docker tag \$IMAGE_REPOSITORY_WORKER:latest $workerImage"
                        sh "docker push $workerImage"
                    }

                    def brokerImages = [
                        "\$IMAGE_REGISTRY/\$IMAGE_REPOSITORY_BROKER:$version",
                        "\$IMAGE_REGISTRY/\$IMAGE_REPOSITORY_BROKER:latest",
                    ]

                    for (String brokerImage : brokerImages) {
                        sh "docker tag \$IMAGE_REPOSITORY_BROKER:latest $brokerImage"
                        sh "docker push $brokerImage"
                    }
                }
            }
        }

        stage('deploy images') {
            //when {
            //    branch "master"
            //}

            steps {
                build job: "$DEPLOYMENT_JOB",
                      parameters:  [
                          [$class: 'StringParameterValue', name: 'ENVIRONMENT', value: "$DEPLOYMENT_ENV"],
                          [$class: 'StringParameterValue', name: 'WEBAPP_IMAGE_TAG', value: "${version}"],
                          [$class: 'StringParameterValue', name: 'WORKER_IMAGE_TAG', value: "${version}"],
                          [$class: 'StringParameterValue', name: 'BROKER_IMAGE_TAG', value: "${version}"]
                      ],
                       propagate: true
            }
        }
    }
}
