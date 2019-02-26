pipeline {
    agent {
        docker {
            image 'maven:3.6-jdk-8'
            args '-v /data/jenkins/.gnupg:/.gnupg -v /data/docker/maven:/.m2 -e MAVEN_OPTS="-Dmaven.repo.local=/.m2/repository -Duser.home=/"'
        }
    }
    stages {
        stage('Build') {
            steps {
                sh '''
                    BRANCH_NO_SLASHES=$(echo $BRANCH_NAME |sed -e "s#/#-#g")
                    POM_BUILD_NUMBER=${BUILD_NUMBER}
                    if [ "$BRANCH_NAME" != "master" ]; then POM_BUILD_NUMBER="${BRANCH_NO_SLASHES}-${BUILD_NUMBER}"; fi

                    ORIGINAL_VERSION=$(mvn -q -DforceStdout help:evaluate -Dexpression='project.version')
                    NEW_VERSION=$(echo ${ORIGINAL_VERSION} | sed -e "s/SNAPSHOT/${POM_BUILD_NUMBER}/")

                    mvn -B versions:set -DnewVersion=${NEW_VERSION}
                '''
                sh 'mvn -B -DskipTests clean package'
            }
        }
        stage('Test') { 
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml' 
                }
            }
        }
        stage('Deploy') {
            when {
                branch 'master'
            }
            steps {
                sh 'mvn -DskipTests clean install deploy -DautoReleaseAfterClose=true'
            }
        }
    }
}
