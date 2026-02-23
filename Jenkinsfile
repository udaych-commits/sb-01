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
        HELM_RELEASE = 'sb-01'
        HELM_NAMESPACE = 'sit'
        EKS_CLUSTER = 'sb-cluster'
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
         stage('Deploy to EKS via Helm') {
                    steps {
                     sh '''
                     aws eks --region ${AWS_REGION} update-kubeconfig --name ${EKS_CLUSTER}
                     kubectl get ns ${HELM_NAMESPACE} || kubectl create ns ${HELM_NAMESPACE}
                     kubectl get secret ecr-secret -n ${HELM_NAMESPACE} || \
                     kubectl create secret docker-registry ecr-secret \
                       --docker-server=${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com \
                       --docker-username=AWS \
                       --docker-password $(aws ecr get-login-password --region ${AWS_REGION}) \
                       --namespace ${HELM_NAMESPACE}

                     helm upgrade --install ${HELM_RELEASE} ./helm/my-app \
                       --namespace ${HELM_NAMESPACE} \
                       --set image.repository=${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO} \
                       --set image.tag=${IMAGE_TAG} \
                       --set imagePullSecrets[0].name=ecr-secret
                     '''
                    }
                }

                stage('Verify Deployment') {
                    steps {
                        sh "kubectl get pods -n $HELM_NAMESPACE"
                        sh "kubectl get svc -n $HELM_NAMESPACE"
                    }
                }
    }
}