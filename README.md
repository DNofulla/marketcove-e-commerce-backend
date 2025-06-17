# MarketCove E-Commerce Backend API

<div align="center">
	<img src="./src/main/resources/static/MarketCoveFullVertical.png" alt="MarketCove E-Commerce Platform" />	
</div>

A comprehensive e-commerce backend API built with Spring Boot, featuring JWT authentication and role-based authorization.

## ✨ Features

### 🔐 Authentication & Authorization
- **JWT-based Authentication** with access and refresh tokens
- **Role-based Authorization** supporting:
  - 👥 Customers
  - 🏪 Sellers
  - 🏢 Business Owners  
  - 👑 Administrators
- **📧 Email Verification** for new accounts
- **🔑 Password Reset** functionality
- **🛡️ Account Security** with failed login attempt tracking and account locking

### 👤 User Management
- **Multi-role User System** with profile-specific data
- **🏢 Business Profiles** for business owners with verification system
- **🏪 Seller Profiles** for individual sellers with ratings and commission tracking
- **📊 Comprehensive User Statistics** for admin dashboards

## 🛠️ Technology Stack

- **Java 22**
- **Spring Boot 3.3.2**
- **Spring Security** with JWT
- **Spring Data JPA** with Hibernate
- **PostgreSQL 14** database
- **Maven** for dependency management
- **Docker** for containerization
- **Lombok** for code generation

## 🚀 API Endpoints

### Authentication Endpoints (`/api/auth`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/register` | Register new user with role-specific profiles |
| POST | `/login` | Authenticate user and return JWT tokens |
| POST | `/refresh-token` | Refresh access token using refresh token |
| POST | `/forgot-password` | Request password reset token |
| POST | `/reset-password` | Reset password using token |
| GET | `/verify-email` | Verify email address with token |
| GET | `/check-email` | Check if email exists |
| GET | `/stats` | Get user statistics (admin only) |
| GET | `/health` | Health check endpoint |

### Role-based Endpoints

- `/api/customer/**` - 👥 Customer-only endpoints
- `/api/seller/**` - 🏪 Seller-only endpoints  
- `/api/business/**` - 🏢 Business owner endpoints
- `/api/admin/**` - 👑 Administrator endpoints
- `/api/user/**` - 🔓 Common authenticated user endpoints

## 🏃‍♂️ Getting Started

### Prerequisites
- Java 22 or later
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL 14

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd marketcove-e-commerce-backend
   ```

2. **Start the database**
   ```bash
   docker-compose up -d
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

The API will be available at `http://localhost:8080`

### ⚙️ Configuration

Key configuration in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/marketcove_db
spring.datasource.username=daniel
spring.datasource.password=changeit

# JWT Configuration  
jwt.secret=your-secret-key
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

## 📝 User Registration Examples

### 👥 Customer Registration
```json
{
  "firstName": "John",
  "lastName": "Doe", 
  "email": "john.doe@example.com",
  "password": "password123",
  "confirmPassword": "password123",
  "phoneNumber": "+1234567890",
  "role": "CUSTOMER"
}
```

### 🏪 Seller Registration
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@example.com", 
  "password": "password123",
  "confirmPassword": "password123",
  "role": "SELLER",
  "shopName": "Jane's Electronics",
  "shopDescription": "Quality electronics at great prices",
  "contactEmail": "shop@janeelectronics.com",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "postalCode": "10001",
  "country": "USA"
}
```

### 🏢 Business Owner Registration
```json
{
  "firstName": "Bob",
  "lastName": "Johnson",
  "email": "bob@acmecorp.com",
  "password": "password123", 
  "confirmPassword": "password123",
  "role": "BUSINESS_OWNER",
  "businessName": "ACME Corporation",
  "businessDescription": "Industrial solutions provider",
  "businessEmail": "info@acmecorp.com",
  "businessRegistrationNumber": "BN123456789",
  "taxId": "TAX123456",
  "businessAddress": "456 Industrial Blvd",
  "businessCity": "Detroit",
  "businessState": "MI", 
  "businessPostalCode": "48201",
  "businessCountry": "USA",
  "websiteUrl": "https://acmecorp.com"
}
```

## 🔑 Authentication Response

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "refreshExpiresIn": 604800000,
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "role": "CUSTOMER",
    "isEmailVerified": false,
    "isAccountLocked": false,
    "profileId": null,
    "profileName": null,
    "isProfileVerified": false
  }
}
```

## 🛡️ Security Features

- **🔒 Password encryption** using BCrypt
- **🎫 JWT token validation** on each request
- **🔐 Role-based access control** with Spring Security
- **🚫 Account lockout** after 5 failed login attempts
- **⏰ Token expiration** with refresh token support
- **🌐 CORS configuration** for frontend integration

## 🧪 Development

### Running Tests
```bash
./mvnw test
```

### Building for Production
```bash
./mvnw clean package
```

### Docker Build
```bash
docker build -t marketcove-backend .
```

## 🗄️ Database Schema

The application automatically creates the following main tables:
- `users` - Core user information
- `business_profiles` - Business owner profiles  
- `seller_profiles` - Seller profiles with ratings and metrics

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License.
