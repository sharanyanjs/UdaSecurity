# 🐱 Catpoint Security System  
**A Modular Java Security Application with AWS Integration & Automated Testing**

[![Java](https://img.shields.io/badge/Java-11+-orange.svg)](https://java.com)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org)
[![JUnit5](https://img.shields.io/badge/JUnit-5-green.svg)](https://junit.org/junit5/)
[![Mockito](https://img.shields.io/badge/Mockito-4.0+-red.svg)](https://site.mockito.org)
[![AWS](https://img.shields.io/badge/AWS-Rekognition-yellow.svg)](https://aws.amazon.com/rekognition/)

## 📋 Project Overview

Catpoint Security System is a professional security monitoring application I architected from scratch using **modular Java principles**, featuring **real-time sensor monitoring**, **AWS-powered image analysis**, and a **comprehensive testing suite** with 100% code coverage. This project demonstrates my expertise in modern Java development, cloud integration, and software quality assurance.

## ✨ Key Features

- **🔒 Real-time Security Monitoring** – Three alarm states (Disarmed/Armed Home/Armed Away) with sensor integration
- **🖼️ AWS Rekognition Integration** – Cloud-based image analysis for cat detection in security footage
- **🧪 Comprehensive Testing Suite** – 25+ unit tests with 100% method coverage using JUnit5 & Mockito
- **📦 Modular Architecture** – Clean separation into `security-service` and `image-service` modules
- **📊 Static Code Analysis** – Integrated SpotBugs with zero High priority issues
- **🚀 Production-Ready Packaging** – Executable JAR with all dependencies bundled

## 🏗️ Architecture
catpoint-parent/ (Multi-module Maven Project)
│
├── 📁 image-service/ # AWS Image Analysis Module
│ ├── src/main/java/com/udacity/catpoint/service/
│ │ ├── AwsImageService.java # Production AWS implementation
│ │ ├── FakeImageService.java # Development/test implementation
│ │ └── ImageService.java # Interface (Strategy Pattern)
│ └── module-info.java # JPMS module descriptor
│
├── 📁 security-service/ # Main Application Module
│ ├── src/main/java/com/udacity/catpoint/
│ │ ├── application/ # JavaFX GUI components
│ │ ├── data/ # Data models & repository pattern
│ │ └── service/ # Business logic layer
│ │ ├── SecurityService.java # Core business logic
│ │ └── StyleService.java # UI styling service
│ ├── src/test/java/com/udacity/catpoint/service/
│ │ └── SecurityServiceTest.java # 25+ comprehensive unit tests
│ └── module-info.java # JPMS module descriptor
│
└── pom.xml # Parent build configuration

## 🛠️ Technical Implementation

### **Java Platform Module System (JPMS)**
```java
// Security Module - Business Logic & UI
module com.udacity.catpoint.security {
    requires com.udacity.catpoint.image;  // Dependency on image module
    requires java.desktop;
    requires miglayout;
    requires com.google.gson;
    exports com.udacity.catpoint.application;
    opens com.udacity.catpoint.data to com.google.gson;  // For JSON serialization
}

// Image Module - AWS Integration
module com.udacity.catpoint.image {
    requires software.amazon.awssdk.rekognition;
    requires software.amazon.awssdk.core;
    requires java.desktop;
    requires org.slf4j;
    exports com.udacity.catpoint.service;  // Export service interface
}
