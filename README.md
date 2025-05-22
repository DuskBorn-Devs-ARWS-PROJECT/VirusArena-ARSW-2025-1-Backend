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

## 🎯 Quality Attributes Implementation

### 🔒 Security
1. **Authentication**
   - Implemented JWT token-based authentication system
   - Secure password storage using BCrypt encryption
   - Token validation and expiration handling

2. **Authorization**
   - Role-based access control (ROLE_HOST and ROLE_USER)
   - Protected endpoints with Spring Security
   - Secure WebSocket communication with token validation

### ⚡ Availability
1. **Horizontal Scaling**
   - Deployed on 2+ EC2 instances for high availability
   - Stateless architecture for easy scaling

2. **Load Balancing**
   - Configured AWS Elastic Load Balancer (ELB)
   - Traffic distribution across backend instances

3. **Caching**
   - Implemented Redis caching layer for performance
   - Reduced database load for frequent operations

### 🛠️ Maintainability
1. **Code Quality**
   - Integrated SonarCloud for static code analysis
   - Achieved "A" rating for both frontend and backend
   - Over 40% unit test coverage

2. **CI/CD Pipeline**
   - Automated code quality checks
   - Continuous inspection with SonarQube integration

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
- **Redis** (for caching)
- **SonarCloud** (for code quality)

---

## ⚙️ Installation & Setup

### Prerequisites

- Java 21 ☕
- MariaDB 10.6+ 🐬
- Maven 3.8+ 🛠️
- Redis 🧠

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

Update the file `src/main/resources/application.properties` with your database and Redis credentials.

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
