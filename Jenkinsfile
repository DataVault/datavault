pipeline {
    agent {
        dockerfile {
            filename 'build.Dockerfile'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
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
                sh 'mvn --batch-mode clean test -U'
            }
        }

        stage('build images') {
            steps {
                sh 'docker build -t datavault/maven-build:latest -f maven.Dockerfile .'
                sh 'docker build -t datavault/webapp:latest -f webapp.Dockerfile .'
                sh 'docker build -t datavault/broker:latest -f broker.Dockerfile .'
                sh 'docker build -t datavault/worker:latest -f worker.Dockerfile .'
            }
        }
    }
}
