# 🏰 VirusArena Backend

**VirusArena Backend** is the core multiplayer game server that handles game logic, user authentication, and real-time communication using WebSockets.

<p align="center">
  <img src="src/main/resources/images/VirusArena.png" alt="VirusArena Logo">
</p>

---

## 🌟 Features

- 🔒 **Secure Authentication** with JWT and Spring Security
- ⚡ **Real-time Communication** via STOMP over WebSockets
- 🎮 **Match Management** with advanced game logic
- 🗄️ **Data Persistence** using MariaDB and Flyway
- 📈 **Scalability** designed for multiple EC2 instances
- 📊 **Monitoring** enabled with Spring Boot Actuator

---

## 🏗️ Architecture Overview

The system is hosted on AWS with a decoupled frontend and backend. It uses load balancing and caching for optimal performance and scalability.

<p align="center">
  <img src="src/main/resources/images/ArquitecturaAWS.jpg" alt="VirusArena AWS Architecture">
</p>


### Frontend
- Hosted on Amazon S3
- Uses a custom authentication service that:
  - Generates and verifies JWT tokens
  - Manages user sessions

### Backend
- Requests are routed via **Elastic Load Balancer** to multiple EC2 instances running Spring Boot
- Backend communicates with **ElastiCache for Redis** for performance
- Data is persisted in **MariaDB**
- Real-time communication enabled via WebSockets

---

## 🧩 Project Structure

```bash
VirusArena-ARSW-2025-1-Backend/
├── src/
│   ├── main/
│   │   ├── java/edu/eci/arsw/
│   │   │   ├── config/              # Config classes
│   │   │   ├── controller/          # REST & WebSocket controllers
│   │   │   ├── exception/           # Custom exceptions
│   │   │   ├── model/               # Domain models and game logic
│   │   │   ├── repository/          # JPA repositories
│   │   │   ├── security/            # JWT security
│   │   │   ├── service/             # Business logic
│   │   │   └── VirusArenaApplication.java
│   └── resources/
│       ├── db/migration/            # Flyway DB migrations
│       └── application.properties
└── pom.xml                          # Maven dependencies
```

---

## 🚀 Technologies Used

- **Spring Boot 3.2.5**
- **Spring Security**
- **Spring WebSockets**
- **JWT Authentication**
- **MariaDB**
- **Flyway** (for DB migrations)
- **Lombok**
- **STOMP Protocol**

---

## ⚙️ Installation & Setup

### Prerequisites

- Java 21 ☕
- MariaDB 10.6+ 🐬
- Maven 3.8+ 🛠️

### Steps

1. **Clone the repository:**

```bash
git clone https://github.com/your-username/VirusArena-ARSW-2025-1-Backend.git
cd VirusArena-ARSW-2025-1-Backend
```

2. **Configure the database:**

```sql
CREATE DATABASE virusarena;
CREATE USER 'virusarena_user'@'localhost' IDENTIFIED BY 'root123';
GRANT ALL PRIVILEGES ON virusarena.* TO 'virusarena_user'@'localhost';
```

3. **Edit `application.properties`:**

Update the file `src/main/resources/application.properties` with your database credentials.

4. **Run the application:**

```bash
mvn clean install
mvn spring-boot:run
```

---

## 🌐 API Endpoints

| Method | Endpoint             | Description               |
|--------|----------------------|---------------------------|
| POST   | `/api/auth/login`    | Authenticate user         |
| POST   | `/api/auth/register` | Register a new user       |
| WS     | `/ws`                | WebSocket game connection |

---

## 🖥️ Frontend Integration

The backend is designed to work with the **VirusArena Frontend**, exposing:

- REST API for authentication
- WebSockets for real-time gameplay
- CORS configured for frontend domains

---

## 📜 License

This project is licensed under the [MIT License](./LICENSE).

---

## ✨ Authors
### **VirusArena Project – ARSW 2025**
#### Developed by DuskBorn Devs
* Manuel Suarez / [@ManuelSuarez07](https://github.com/ManuelSuarez07)
* Yeltzyn Sierra / [@YeltzynS](https://github.com/YeltzynS)

