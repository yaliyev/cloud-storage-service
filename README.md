# Idempotent Cloud Storage & Event-Driven Processing Service

A production-grade, event-driven cloud storage service built with **Spring Boot 3**, **AWS S3/SQS** (emulated via **LocalStack**), **PostgreSQL**, and **Redis**. 

The system provides presigned S3 upload/download URLs and handles asynchronous file processing. It features a distributed idempotency layer powered by Redis `SETNX` locks to safely absorb SQS "at-least-once" message deliveries without duplicate database records.

---

## 🏗️ Architecture Overview

                  +-------------------------------------------------+
                  |                 User / Client                   |
                  +------------------------+------------------------+
                                           |
                           1. Request Presigned Upload URL
                                           v
                  +-------------------------------------------------+
                  |        Spring Boot REST API (:8080)             |
                  +------------------------+------------------------+
                                           |
                           2. Upload File Direct to S3
                                           v
                  +-------------------------------------------------+
                  |          LocalStack S3 (Bucket)                 |
                  +------------------------+------------------------+
                                           |
                           3. Async Event Notification
                                           v
                  +-------------------------------------------------+
                  |          LocalStack SQS (Event Queue)           |
                  +------------------------+------------------------+
                                           |
                           4. Consume Message Event
                                           v
                  +-------------------------------------------------+
                  |            SQS Event Listener                   |
                  +------------+-----------------------+------------+
                               |                       |
                     5. Check / Set Lock               | 6. Persist Metadata
                               v                       v
                  +------------------------+  +---------------------+
                  |   Redis (:6379)        |  | PostgreSQL (:5432)  |
                  |   (Idempotency Guard)  |  | (Metadata DB)       |
                  +------------------------+  +---------------------+



Event Flow

Presigned Upload: Client requests a presigned PUT URL from /api/storage/presigned-upload.

Direct Storage Upload: Client uploads the file directly to S3 via the presigned URL, bypassing application bandwidth constraints.

Event Notification: S3 automatically emits an ObjectCreated:Put event notification directly into an SQS queue (file-events-queue).

Asynchronous Ingestion: Spring Boot's @SqsListener consumes the event payload.

Idempotency Guard: A composite key (event:s3:{bucket}:{key}:{eTag}) is written to Redis using SETNX with a 24-hour TTL.

If the key exists (duplicate delivery), processing skips immediately.

If the key is new, execution continues.

Persistence: Metadata (bucket name, key, size, file status) is safely upserted into PostgreSQL.

🛠️ Tech Stack

Framework: Spring Boot 3 / Java 21

Build Tool: Gradle

Cloud Infrastructure Emulation: LocalStack (AWS S3 & SQS)

SDK: AWS SDK for Java v2 (software.amazon.awssdk)

Caching & Idempotency: Redis 7 (Lettuce client)

Database: PostgreSQL 16 (Spring Data JPA)

Containerization: Docker & Docker Compose (Multi-Stage JDK 21 build)

🚀 Getting Started

Prerequisites

Docker Desktop (includes Docker Compose)

cURL or Postman

Java 21 / Gradle 8+ (optional, for local non-containerized execution)



⚡ Quick Start with Docker Compose

1. Clone the Repository:
git clone [https://github.com/yourusername/cloud-storage-service.git](https://github.com/yourusername/cloud-storage-service.git)
cd cloud-storage-service

2. Start the Complete Environment:Build the multi-stage application container and launch all services:
docker compose up -d --build

3. Verify Container Health: 
docker compose ps
Ensure cloud_storage_app, localstack_main, postgres_db, and local-redis are all reporting healthy.

4. Initialize Infrastructure (S3 Bucket & SQS Queue Link):
curl -i -X POST "http://localhost:8080/api/storage/init"

🧪 End-to-End Verification Guide

Step 1: Request a Presigned Upload URL
curl -i -X GET "http://localhost:8080/api/storage/presigned-upload?fileName=portfolio-test.txt"
Returns a raw presigned URL pointing to LocalStack S3.

Step 2: Upload File Directly to S3Create a test file and perform a PUT request using the presigned URL:
echo "Testing Event-Driven Cloud Storage" > portfolio-test.txt
curl -i -X PUT "<PASTE_PRESIGNED_URL_HERE>" \
  -H "Content-Type: text/plain" \
  --data-binary "@portfolio-test.txt"


Step 3: Inspect Consumer Processing
Check your application logs to verify that SQS received the event and written metadata to PostgreSQL:
docker logs cloud_storage_app -f

Expected Output:
📩 [MAIN QUEUE] Received raw SQS payload
Processing S3 event 'ObjectCreated:Put' for Bucket: my-test-bucket, Key: portfolio-test.txt, Size: 35 bytes
Successfully persisted FileMetadata record to PostgreSQL with ID: 1

Step 4: Verify Redis Idempotency KeyVerify that the idempotency key was registered in Redis:
docker exec -it local-redis redis-cli KEYS "event:s3:*"
Output:
1) "event:s3:my-test-bucket:portfolio-test.txt:0cdd34e885f0a6e87edc15a30895fbe9"
Step 5: Test Duplicate Event Suppression
Simulate SQS at-least-once re-delivery by executing an upload with the same filename or manually sending a duplicate payload via LocalStack:
docker exec -it localstack_main awslocal sqs send-message \
  --queue-url http://localhost:4566/000000000000/file-events-queue \
  --message-body '{"Records":[{"eventName":"ObjectCreated:Put","s3":{"bucket":{"name":"my-test-bucket"},"object":{"key":"portfolio-test.txt","size":35,"eTag":"0cdd34e885f0a6e87edc15a30895fbe9"}}}]}'

Application Log Output:

📩 [MAIN QUEUE] Received raw SQS payload
Skipping duplicate S3 event [Key: event:s3:my-test-bucket:portfolio-test.txt:0cdd34e885f0a6e87edc15a30895fbe9] already processed in Redis.

📡 API Endpoints Summary

POST     /api/storage/initNoneProvisions 
S3 bucket, SQS queue, and S3-to-SQS notifications

GET      /api/storage/presigned-upload
fileName (string)Generates a presigned S3 URL for client uploading

GET      /api/storage/presigned-download
fileName (string)Generates a presigned S3 URL for downloading

DELETE   /api/storage/delete
fileName (string)Removes file from S3 bucket

🔒 Security & Best Practices
Non-Root Container Runtime: The production Dockerfile drops root permissions and executes under an unprivileged appuser.
Multi-Stage Build Isolation: Build tools (Gradle) and source files are discarded in Stage 1, leaving only eclipse-temurin:21-jre-alpine in the runtime layer.
Resilient Parsing: SQS listeners parse raw payloads using S3EventNotification.fromJson(), dynamically ignoring non-record events such as s3:TestEvent.

📄 License
This project is licensed under the MIT License.
---

