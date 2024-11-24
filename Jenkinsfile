pipeline {
    agent any
    environment {
        AWS_REGION = 'us-east-1'              
        ECR_REPO_NAME = 'weather/predictor'          
        IMAGE_TAG = "weather-predictor-latest"                   
        EC2_IP = '98.81.214.56' 
        EC2_USER = 'ubuntu'      
        AWS_ACCOUNT_ID = '293359982991'   
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/kunseal/weatherpredictor-backend.git'
            }
        }
        stage('Build Project') {
            steps {
                script {
                        sh """
                        echo "Running Maven build"
                        mvn clean install
                        """
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                        sh """
                        # Verify the JAR file exists
                        if [ ! -f target/weatherpredictor-0.0.1-SNAPSHOT.jar ]; then
                            echo "ERROR: JAR file not found in the target directory!"
                            exit 1
                        fi

                        # Build the Docker image
                        docker build -t ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO_NAME}:${IMAGE_TAG} .
                        """
                }
            }
        }
        

        stage('Login to Amazon ECR') { 
            steps {
                script {
                    withCredentials([
                        string(credentialsId: 'AWS_ACCESS_KEY', variable: 'AWS_ACCESS_KEY'),
                        string(credentialsId: 'AWS_SECRET_ACCESS_KEY', variable: 'AWS_SECRET_ACCESS_KEY')
                    ]) {
                        sh """
                        aws configure set aws_access_key_id $AWS_ACCESS_KEY
                        aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY
                        aws configure set region $AWS_REGION
                        aws ecr-public get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin public.ecr.aws/y1y3z0j6
                        """
                    }
                }
            }
        }

        stage('Push to ECR') {
            steps {
                script {
                    sh """
                    docker tag ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO_NAME}:${IMAGE_TAG} public.ecr.aws/y1y3z0j6/${ECR_REPO_NAME}:${IMAGE_TAG}
                    docker push public.ecr.aws/y1y3z0j6/${ECR_REPO_NAME}:${IMAGE_TAG}
                    """
                }
            }
        }

        stage('Deploy Container') {
            steps {
                script {
                    sshagent(credentials: ['cloud-user']) {
                        sh """
                            ssh-keyscan -H ${EC2_IP} >> ~/.ssh/known_hosts
                            # Stop and remove any container using port 8081
                            ssh ${EC2_USER}@${EC2_IP} '
                                container_id=\$(docker ps -q -f "publish=8081")
                                if [ -n "\$container_id" ]; then
                                    echo "Stopping and removing container running on port 8081..."
                                    docker stop \$container_id
                                    docker rm \$container_id
                                else
                                    echo "No container running on port 8081"
                                fi
                                '
                                # Pull the Docker image and run it on port 8081
                                ssh ${EC2_USER}@${EC2_IP} '
                                docker pull public.ecr.aws/y1y3z0j6/${ECR_REPO_NAME}:${IMAGE_TAG} && \
                                docker run -d -p 8081:8080 \
                                -e WEATHER_API_KEY=d2929e9483efc82c82c32ee7e02d563e \
                                -e REDIS_KEY=5Ncxv9V4GqZ1bsBQLcGB2tgrpoANz0Ju 
                                public.ecr.aws/y1y3z0j6/${ECR_REPO_NAME}:${IMAGE_TAG}
                            '
                            """
                        }

                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline execution completed.'
        }
        success {
            echo 'Pipeline executed successfully.'
        }
        failure {
            echo 'Pipeline execution failed.'
        }
    }
}