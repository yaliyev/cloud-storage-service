\# Cloud Storage Service



A production-ready Spring Boot 3 microservice that integrates with AWS S3 using AWS SDK v2, fully containerized with Docker Compose and emulated locally using LocalStack.



\---



\## 🏗️ Architecture Overview

+-------------------------------------------------------+

|                    Docker Compose                     |

|                                                       |

|   +--------------------+     +--------------------+   |

|   |  Spring Boot App   | --> |     LocalStack     |   |

|   |   (Port 8080)      |     |  (AWS S3 - 4566)   |   |

|   +--------------------+     +--------------------+   |

+-------------------------------------------------------+


Spring Boot 3 (Java 21): Exposes REST APIs for cloud storage operations.



AWS SDK v2 (S3Client): Manages object storage operations targeting AWS S3.



LocalStack: Local cloud stack emulator running S3 in Docker.



Docker \& Docker Compose: Containerizes and orchestrates the application and services environment.



🛠️ Tech Stack \& Prerequisites

Java 21 / Spring Boot 3.x



Gradle



AWS SDK for Java v2 (software.amazon.awssdk:s3)



Docker Desktop (WSL2 backend on Windows)



LocalStack

🚀 Quick Start

1\. Clone the Repository

Bash

git clone \[https://github.com/yaliyev/cloud-storage-service.git](https://github.com/yaliyev/cloud-storage-service.git)

cd cloud-storage-service

2. Configure Environment Variables

Create a .env file in the project root directory:



LOCALSTACK\_AUTH\_TOKEN=your-localstack-auth-token-here

3. Build \& Run with Docker Compose

docker compose up --build -d

🧪 Verification \& Testing



1\. Initialize S3 Bucket

curl -X POST http://localhost:8080/api/storage/init



2\. Upload a File



curl -X POST "http://localhost:8080/api/storage/upload?fileName=demo.txt" \\

&#x20;    -H "Content-Type: text/plain" \\

&#x20;    -d "Hello from Spring Boot and AWS S3 inside Docker!"



3\. Download a File



curl "http://localhost:8080/api/storage/download?fileName=demo.txt"



4\. Inspect Bucket Content via AWS CLI inside Docker



docker exec -it localstack\_main awslocal s3 ls s3://my-test-bucket





📄 License

This project is open-source and available under the MIT License.

\---


