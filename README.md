# Cloud-computing-technology-project

# Cloud-Based Student Attendance System

A multi-platform attendance management system featuring an **Electron.js** desktop interface for teachers, a **Native Android** mobile application for students, and a **Java Spring Boot** cloud backend connecting to a **PostgreSQL** database.

---

## System Architecture

```text
  ┌──────────────────────────┐        ┌──────────────────────────┐
  │   ELECTRON DESKTOP APP   │        │    ANDROID MOBILE APP    │
  │     (Teacher View)       │        │     (Student View)       │
  └─────────────┬────────────┘        └─────────────┬────────────┘
                │                                   │
                │        HTTPS REST / WebSockets    │
                └─────────────────┬─────────────────┘
                                  ▼
                     ┌──────────────────────────┐
                     │    HOSTED JAVA BACKEND   │
                     │  (Spring Boot / Cloud)   │
                     └────────────┬─────────────┘
                                  │
                                  ▼
                     ┌──────────────────────────┐
                     │     CLOUD DATABASE       │
                     │       (PostgreSQL)       │
                     └──────────────────────────┘

