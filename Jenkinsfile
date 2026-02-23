pipeline {
    agent any

    tools {
        maven 'Maven-3'
    }

    environment {
        AWS_ACCOUNT_ID = "315164662006"
        AWS_REGION = "us-east-1"
        ECR_REPO = "sb-01"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Build Maven') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh """
                docker build -t ${ECR_REPO}:${IMAGE_TAG} .
                """
            }
        }

        stage('Login to ECR') {
            steps {
                sh """
                # Check if repo exists, create if not
                aws ecr describe-repositories \
                    --repository-names ${ECR_REPO} \
                    --region ${AWS_REGION} > /dev/null 2>&1 || \
                aws ecr create-repository \
                    --repository-name ${ECR_REPO} \
                    --region ${AWS_REGION}

                # Login to ECR
                aws ecr get-login-password --region ${AWS_REGION} | \
                docker login --username AWS --password-stdin \
                ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
                """
            }
        }

        stage('Tag Image') {
            steps {
                sh """
                docker tag ${ECR_REPO}:${IMAGE_TAG} \
                ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}:${IMAGE_TAG}
                """
            }
        }

        stage('Push to ECR') {
            steps {
                sh """
                docker push \
                ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}:${IMAGE_TAG}
                """
            }
        }
    }
}