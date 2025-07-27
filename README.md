# Software Devolopment Project

**Authors :** Sadatul Islam Sadi, Irtiaz Kabir, Ninad Mahmud Nobo <br>
**Supervisor:** Sheikh Azizul Hakim, Lecturer, BUET

## Introduction

In today's fast-paced world, caring for our elderly parents, especially those battling dementia, has become one of the most challenging responsibilities we face. The heart-wrenching reality is that many of us struggle to provide the constant care and attention our loved ones need while juggling work, family, and other life commitments. The guilt of not being there enough, the worry about their safety, and the helplessness of watching them struggle with daily tasks can be overwhelming.

**MindTrace** was born from this very human struggle. We understand that every family dealing with dementia faces unique challenges - the fear of leaving a parent alone, the difficulty in tracking their daily activities, and the constant worry about their well-being. Our solution brings together cutting-edge AI technology with deep empathy for the human experience.

At its core, MindTrace is more than just an app - it's a digital companion that never sleeps, never gets tired, and never forgets. It's the gentle voice that reminds your mother to take her medication, the patient listener who engages your father in meaningful conversations, and the vigilant guardian that captures important moments from their day. But most importantly, it's the bridge that connects you to your loved one's world, even when you can't be physically present.

We believe that technology should serve humanity, not replace it. That's why MindTrace is designed to work seamlessly with your existing care network - whether it's a trusted family member, a professional caregiver, or a dedicated maid. Our multi-layered security ensures that your loved one's privacy and dignity are always protected, while our intelligent notification system keeps everyone who cares informed and connected.

In a world where dementia can make communication difficult and memories fade, MindTrace helps preserve the precious moments, maintain routines, and ensure that no important detail is lost. It's our way of saying that even in the face of cognitive challenges, every life deserves to be lived with dignity, connection, and love.

## Table of Contents
- [Features](#features)
- [Database Schema](#database-schema)
- [Backend Technologies](#backend-tech)
- [Frontend Technologies](#frontend-tech)
- [Deployment Services](#deployment-services)

## Features

### 🤖 AI-Powered Conversational Assistant
- **Intelligent Daily Companion**: Our AI assistant engages patients in meaningful conversations, providing companionship and cognitive stimulation
- **Smart Log Detection**: Automatically identifies and captures important information from conversations, ensuring no crucial details are missed
- **Context-Aware Interactions**: Understands the patient's condition and adapts communication style accordingly

### 👥 Multi-Caregiver Support System
- **Flexible Care Network**: Support for multiple caregivers per patient (family members, professional caregivers, domestic help)
- **Shared Information Access**: All caregivers can view patient logs and important updates in real-time
- **Coordinated Care**: Enables better communication and coordination among all care team members

### 🔐 Advanced Security & Privacy Protection
- **OTP-Based Caregiver Management**: Adding or removing caregivers requires OTP verification sent to the primary contact
- **Primary Contact Verification**: Ensures only authorized family members can approve caregiver changes
- **QR Code Registration**: Secure patient registration through QR scanning with short-lived secrets
- **Telegram Security**: Short-lived UUIDs prevent unauthorized access to notification channels

### ⏰ Smart Reminder System
- **Customizable Reminders**: Caregivers can set medication reminders, appointment alerts, and daily task notifications
- **Flexible Scheduling**: Support for one-time and recurring reminders
- **Reliable Delivery**: Multiple notification channels ensure reminders are never missed

### 📱 Telegram Integration
- **Direct Notifications**: Important updates delivered directly to caregivers and primary contacts via Telegram
- **Separation of Concerns**: Different notification channels for different types of information
- **Real-time Updates**: Instant alerts for critical events and daily summaries

### 🎯 QR-Based Registration & Connection
- **Secure Patient Onboarding**: Patients register by scanning a caregiver's QR code
- **Permission-Based Connection**: QR codes contain short-lived secrets that act as permission tokens
- **Primary Contact Assignment**: Automatic assignment of the scanned caregiver as the patient's primary contact

### 📊 Comprehensive Logging & Monitoring
- **Conversation Logs**: Detailed records of AI-patient interactions
- **Activity Tracking**: Monitor daily patterns and identify potential concerns
- **Caregiver Insights**: Share important information with all authorized caregivers
- **Historical Data**: Maintain long-term records for better care planning

## Database Schema

![Database Schema](assets/db_schema_updated.png)

# Backend Tech

| **Category**        | **Technology**                                                                        |
|---------------------|---------------------------------------------------------------------------------------|
| Main Backend        | Spring Boot 3                                                                         |
| Security            | Spring Security, Spring OAuth Resource Server, Azure Secret Manager, Docker Secret Manager            |
| Database and ORM    | PostgreSQL, Qdrant, Spring Data JPA, Flyway Migrations                                             |
| Caching             | Spring Data Redis                                                                     |
| Mail                | Spring Boot Starter Mail                                                              |
| Tests               | JUnit, Testcontainers                                                                 |
| Notifications       | Firebase Cloud Messaging, RabbitMQ  

## Frontend Tech

| **Category**                    | **Technology**                                                                            |
|---------------------------------|-------------------------------------------------------------------------------------------|
| Main Frontend                   | Kotlin, Android SDK                                                                  

## Deployment Services

| **Component**         | **Technology/Service**                    |
|-----------------------|-------------------------------------------|
| VM              | Azure                                 |
| Contenarization                 | Docker                  |
| Registry               | Github Container Registry                 |
| CI/CD               | Github Actions                 |
| IaC               | Ansible                 |
| Domain                | Namecheap                                 |

