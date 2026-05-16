# 📄 Smart Document Hub

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![AWS](https://img.shields.io/badge/AWS-Integrated-orange.svg)](https://aws.amazon.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 🚀 Overview
**Smart Document Hub** is a scalable microservice-based platform designed to manage enterprise documents efficiently across distributed cloud environments. The system supports intelligent document ingestion, metadata extraction, asynchronous processing, advanced search, analytics, and secure access management.

The platform leverages multiple AWS managed services to deliver high availability, performance, and cloud-native scalability.
### ✨ Key Features

- 📤 **Document Management** - Upload, store, and manage documents in multiple formats (PDF, DOCX, TXT)
- 🔍 **Intelligent Search** - Full-text search with OpenSearch/Elasticsearch integration
- 🤖 **Auto Text Extraction** - Automatic text extraction from PDFs and DOCX files
- ⚡ **Async Processing** - Background document processing using AWS SQS
- 💾 **Multi-Platform Storage** - AWS S3 for files, PostgreSQL for metadata, DynamoDB for analytics
- 🚀 **High Performance** - Redis caching for frequently accessed documents
- 🔒 **Enterprise Security** - JWT authentication, OAuth2, role-based access control
- 📊 **Real-time Analytics** - Document tracking, view counts, and user metrics
- 📧 **Smart Notifications** - Email alerts via AWS SES and SNS

## 🏗️ Architecture
```
                +----------------------+
                |     Client Apps      |
                | Web / Mobile / APIs  |
                +----------+-----------+
                           |
                           v
                +----------------------+
                |   Spring Boot APIs   |
                | Smart Document Hub   |
                +----------+-----------+
                           |
        +------------------+------------------+
        |                  |                  |
        v                  v                  v
+---------------+  +---------------+  +---------------+
| Amazon S3     |  | DynamoDB      |  | Redis Cache   |
| Document Store|  | Metadata DB   |  | Fast Access   |
+---------------+  +---------------+  +---------------+
                            |
                            v
                    +-------------------+
                    | OpenSearch Engine |
                    | Full Text Search  |
                    +-------------------+
                    |
                    v
                    +-------------------+
                    | AWS SQS / SNS     |
                    | Async Processing  |
                    +-------------------+
```
## 🛠️ Technologies Used
- **Backend**: Spring Boot, Java 21
- **Storage**: AWS S3, PostgreSQL, DynamoDB
- **Search**: OpenSearch / Elasticsearch
- **Caching**: Redis
- **Messaging**: AWS SQS, SNS
- **Authentication**: JWT, OAuth2
- **Email**: AWS SES
- **Monitoring**: AWS CloudWatch, Prometheus, Grafana