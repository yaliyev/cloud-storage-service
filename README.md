# Cloud Storage \& Event Processing Service



A production-ready Spring Boot 3 microservice integrating AWS S3 for object storage and AWS SQS for event-driven messaging using Spring Cloud AWS v4. The entire stack is containerized with Docker Compose and emulated locally via LocalStack.



---



## 🏗️ Architecture Overview



```text

&#x20;                                        +-----------------------+

&#x20;                                        |     LocalStack S3     |

&#x20;                                        +-----------------------+

&#x20;                                                    ^

&#x20;                                                    | (Upload File)

+-----------------------+                    +-------+---------------+

|      HTTP Client      | -- (POST File) --> |   Spring Boot App     |

+-----------------------+                    +-------+---------------+

&#x20;                                                    |

&#x20;                                                    | (Publish Event)

&#x20;                                                    v

&#x20;                                        +-----------------------+

&#x20;                                        |     LocalStack SQS    |

&#x20;                                        +-----------+-----------+

&#x20;                                                    |

&#x20;                                                    | (@SqsListener)

&#x20;                                                    v

&#x20;                                        +-----------------------+

&#x20;                                        |  Background Consumer  |

&#x20;                                        +-----------------------+



Spring Boot 3 (Java 21): Exposes REST APIs and consumes asynchronous background events.



AWS S3 (S3Client): Handles object uploads and downloads.



AWS SQS (@SqsListener): Processes decoupled file-upload notification events asynchronously.



Spring Cloud AWS (4.0.0): Manages reactive SQS listener container lifecycle and polling.



LocalStack & Docker Compose: Containerizes AWS cloud dependencies locally.



🛠️ Tech Stack

Java 21 / Spring Boot 3.x



AWS SDK v2 \& Spring Cloud AWS 4.0.0



Docker & Docker Compose



LocalStack (S3 + SQS)



🚀 Quick Start

1. Launch Stack

docker compose up --build -d



2. Initialize S3 \& SQS Infrastructure

curl -X POST http://localhost:8080/api/storage/init



3. Upload File \& Trigger Asynchronous SQS Event

curl -X POST "http://localhost:8080/api/storage/upload?fileName=demo-event.txt" \\

&#x20;    -H "Content-Type: text/plain" \\

&#x20;    -d "Hello from Event-Driven Spring Boot!"



4. Verify Event Consumption

Stream logs to watch @SqsListener process the message in real time:



docker logs -f cloud\_storage\_app



📄 License

This project is open-source and available under the MIT License.

---

