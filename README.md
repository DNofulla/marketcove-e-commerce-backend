# MarketCove E-Commerce Backend API

<div align="center">
	<img src="./src/main/resources/static/MarketCoveFullVertical.png" alt="MarketCove E-Commerce Platform" />	
</div>

A comprehensive e-commerce backend API built with Spring Boot, featuring JWT authentication, role-based authorization, and full storefront management capabilities.

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

### 🏪 Storefront Management
- **🏬 Multi-tenant Storefronts** - Both business owners and sellers can create multiple storefronts
- **🎨 Customizable Storefronts** with logos, banners, descriptions, and social media links
- **🔗 SEO-friendly URLs** with custom slugs for each storefront
- **📊 Storefront Analytics** including ratings, reviews, sales statistics
- **🔍 Public Storefront Discovery** with search, featured, and top-rated listings
- **📝 Store Policies** management (return policy, shipping policy)

### 📦 Item Management
- **📋 Complete Product Catalog** with detailed item information
- **💰 Flexible Pricing** with sale prices and discount calculations
- **📦 Inventory Management** with stock tracking and low-stock alerts
- **🏷️ Product Organization** with categories, tags, and SKUs
- **🖼️ Image Management** with multiple images per item and AWS S3 integration
- **🔍 Advanced Search & Filtering** by category, price range, ratings
- **📈 Product Analytics** with view counts, sales tracking, and ratings

### ☁️ AWS Integration (Optional)
- **🖼️ S3 Image Storage** for storefront logos, banners, and product images
- **🔧 Configurable Cloud Storage** - easily disabled for development
- **📁 Organized Storage** with folder structure for different image types

## 🛠️ Technology Stack

- **Java 22**
- **Spring Boot 3.3.2**
- **Spring Security** with JWT
- **Spring Data JPA** with Hibernate
- **PostgreSQL 14** database
- **AWS SDK** for S3 integration
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

### 🏪 Storefront Management (`/api/storefronts`)

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/` | ✅ Business/Seller | Create new storefront |
| PUT | `/{id}` | ✅ Owner | Update storefront details |
| GET | `/{id}` | ❌ Public | Get storefront by ID |
| GET | `/slug/{slug}` | ❌ Public | Get storefront by URL slug |
| GET | `/my-storefronts` | ✅ Owner | Get user's storefronts |
| GET | `/` | ❌ Public | List active storefronts (paginated) |
| GET | `/search?query=` | ❌ Public | Search storefronts by name |
| GET | `/featured` | ❌ Public | Get featured storefronts |
| GET | `/top-rated` | ❌ Public | Get top-rated storefronts |
| DELETE | `/{id}` | ✅ Owner | Delete storefront (soft delete) |
| POST | `/{id}/logo` | ✅ Owner | Upload storefront logo |
| POST | `/{id}/banner` | ✅ Owner | Upload storefront banner |

### 📦 Item Management (`/api/items`)

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/storefront/{id}` | ✅ Business/Seller | Create item in storefront |
| PUT | `/{id}` | ✅ Owner | Update item details |
| GET | `/{id}` | ❌ Public | Get item by ID (increments views) |
| GET | `/sku/{sku}` | ❌ Public | Get item by SKU |
| GET | `/storefront/{id}` | ❌ Public | Get items in storefront |
| GET | `/my-items` | ✅ Owner | Get user's items |
| GET | `/search?query=` | ❌ Public | Search items by name/description |
| GET | `/category/{category}` | ❌ Public | Get items by category |
| GET | `/price-range?min=&max=` | ❌ Public | Get items by price range |
| GET | `/featured` | ❌ Public | Get featured items |
| GET | `/on-sale` | ❌ Public | Get items on sale |
| GET | `/best-selling` | ❌ Public | Get best-selling items |
| GET | `/recent` | ❌ Public | Get recently added items |
| GET | `/low-stock` | ✅ Owner | Get low-stock items |
| DELETE | `/{id}` | ✅ Owner | Delete item (soft delete) |
| POST | `/{id}/images` | ✅ Owner | Upload item images |
| DELETE | `/{id}/images?imageUrl=` | ✅ Owner | Remove item image |
| PATCH | `/{id}/stock?quantity=` | ✅ Owner | Update stock quantity |

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
- AWS Account (optional, for image storage)

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

3. **Configure AWS (Optional)**
   ```bash
   # Set environment variables for AWS credentials
   export AWS_ACCESS_KEY_ID=your_access_key
   export AWS_SECRET_ACCESS_KEY=your_secret_key
   
   # Or configure application.properties
   aws.s3.enabled=true
   aws.s3.bucket-name=your-bucket-name
   aws.s3.region=your-region
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

The API will be available at `http://localhost:8080`

## 📖 API Documentation

This project includes comprehensive API documentation using **Swagger/OpenAPI 3.0**. The documentation is **publicly accessible** and provides interactive testing capabilities.

### 🌐 Access URLs

Once the application is running, you can access the API documentation at:

- **📋 Swagger UI**: `http://localhost:8080/swagger-ui.html`
  - Interactive API documentation with testing capabilities
  - No authentication required to browse endpoints
  
- **📄 OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
  - Raw OpenAPI specification in JSON format
  
- **📄 OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`
  - Raw OpenAPI specification in YAML format

### ✨ Documentation Features

- **🔓 Public Access** - Browse all API endpoints without authentication
- **🧪 Interactive Testing** - Test public endpoints directly from the UI
- **🔐 JWT Authentication Support** - Use the "Authorize" button to enter JWT tokens for protected endpoints
- **📚 Comprehensive Details** - Complete request/response schemas, examples, and descriptions
- **🏷️ Organized by Tags** - Endpoints grouped logically (Authentication, Storefront Management, Item Management, etc.)
- **📝 Example Requests** - Pre-filled example data for all endpoints

### 🔑 Using Authentication in Swagger

1. First, register or login using the public authentication endpoints
2. Copy the `accessToken` from the response
3. Click the **"Authorize"** button in Swagger UI
4. Enter your token (without the "Bearer " prefix)
5. Now you can test protected endpoints

## ⚙️ Configuration

Key configuration in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/marketcove_db
spring.datasource.username=postgres
spring.datasource.password=changeit

# JWT Configuration  
jwt.secret=your-secret-key
jwt.expiration=86400000
jwt.refresh-expiration=604800000

# AWS S3 Configuration (disabled by default)
aws.s3.enabled=false
aws.s3.bucket-name=marketcove-images
aws.s3.region=us-east-1
```

## 📝 Usage Examples

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

### 🏪 Creating a Storefront
```json
{
  "storeName": "Tech Paradise",
  "storeDescription": "Your one-stop shop for the latest technology",
  "storeUrlSlug": "tech-paradise",
  "contactEmail": "info@techparadise.com",
  "contactPhone": "+1-555-0123",
  "returnPolicy": "30-day return policy on all items",
  "shippingPolicy": "Free shipping on orders over $50",
  "websiteUrl": "https://techparadise.com",
  "facebookUrl": "https://facebook.com/techparadise",
  "instagramUrl": "https://instagram.com/techparadise"
}
```

### 📦 Creating an Item
```json
{
  "itemName": "Wireless Bluetooth Headphones",
  "itemDescription": "High-quality wireless headphones with noise cancellation",
  "sku": "WBH-001",
  "price": 149.99,
  "compareAtPrice": 199.99,
  "stockQuantity": 50,
  "lowStockThreshold": 10,
  "weight": 0.3,
  "weightUnit": "kg",
  "category": "Electronics",
  "tags": "wireless, bluetooth, headphones, audio",
  "requiresShipping": true,
  "isDigital": false,
  "seoTitle": "Best Wireless Bluetooth Headphones - Tech Paradise",
  "seoDescription": "Premium wireless headphones with superior sound quality and noise cancellation. Free shipping available.",
  "imageUrls": []
}
```

## 🔑 Authentication Response
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

## 🛡️ Security Features

- **🔒 Password encryption** using BCrypt
- **🎫 JWT token validation** on each request
- **🔐 Role-based access control** with Spring Security
- **🚫 Account lockout** after 5 failed login attempts
- **⏰ Token expiration** with refresh token support
- **🌐 CORS configuration** for frontend integration
- **🏪 Ownership validation** for storefronts and items
- **🔍 Input validation** and sanitization for all endpoints

## 🏬 Storefront Features

### Multi-tenant Architecture
- **Business owners** and **sellers** can each create multiple storefronts
- Each storefront operates independently with its own items and settings
- Flexible ownership model supporting different business types

### Customization Options
- **Visual Branding**: Custom logos and banner images
- **SEO Optimization**: Custom URL slugs and meta tags
- **Social Media Integration**: Links to Facebook, Twitter, Instagram
- **Store Policies**: Customizable return and shipping policies

### Analytics & Insights
- **Performance Tracking**: Total sales, orders, and revenue
- **Customer Feedback**: Average ratings and review counts
- **Inventory Insights**: Item counts and stock levels

## 📦 Item Management Features

### Comprehensive Product Information
- **Detailed Descriptions**: Rich text descriptions with SEO optimization
- **Pricing Flexibility**: Regular prices, sale prices, and automatic discount calculations
- **Inventory Tracking**: Real-time stock levels with low-stock alerts
- **Physical Properties**: Weight, dimensions, shipping requirements

### Advanced Organization
- **Categorization**: Hierarchical category system
- **Tagging**: Flexible tag system for enhanced searchability
- **SKU Management**: Unique product identifiers with automatic generation

### Media Management
- **Multiple Images**: Support for multiple product images per item
- **AWS S3 Integration**: Scalable cloud storage for images
- **Automatic Optimization**: Primary image selection and URL management

## 🧪 Development

### Running Tests
```bash
./mvnw test
```

### Test Coverage

#### 📊 Test Coverage Summary
- **Total Tests**: 36 comprehensive tests
- **Success Rate**: 100% passing tests

#### 🏗️ Test Architecture Features
- **🔧 @WebMvcTest** - Focused web layer testing
- **🎭 @MockBean** - Service layer mocking for isolation
- **📁 @Nested classes** - Well-organized test structure
- **📋 @DisplayName** - Descriptive test documentation
- **🔍 MockMvc** - HTTP request simulation
- **🎯 Mockito** - Behavior verification and mocking
- **✅ JSON Path validation** - Response content verification
- **🔐 Security disabled** - No CSRF/auth interference in unit tests

#### 📝 Test Categories Covered
- **✅ Success Scenarios** - All happy path flows
- **❌ Error Handling** - Service exceptions, validation failures
- **🔍 Input Validation** - Missing fields, invalid formats, constraints
- **🛡️ Security** - Token validation, authorization headers
- **📊 Response Verification** - Status codes, JSON structure, error messages
- **🔄 Service Integration** - Mocked service behavior verification

#### 🎯 Test Suite Breakdown

| Test Suite | Tests | Coverage |
|------------|-------|----------|
| Health Endpoint Tests | 1 | ✅ Health check functionality |
| Registration Endpoint Tests | 9 | ✅ Customer/Seller/Business registration, validation, error handling |
| Login Endpoint Tests | 5 | ✅ Valid/invalid credentials, validation, error handling |
| Refresh Token Endpoint Tests | 5 | ✅ Token refresh, header validation, error handling |
| Forgot Password Endpoint Tests | 4 | ✅ Password reset request, validation, error handling |
| Reset Password Endpoint Tests | 4 | ✅ Password reset confirmation, validation, error handling |
| Email Verification Endpoint Tests | 3 | ✅ Email verification, token validation, error handling |
| Check Email Endpoint Tests | 3 | ✅ Email existence check, error handling |
| User Stats Endpoint Tests | 2 | ✅ Statistics retrieval, error handling |

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
- `storefronts` - Multi-tenant storefronts with ownership tracking
- `items` - Product catalog with comprehensive metadata
- `item_images` - Multiple images per item support

## 🔧 Development Features

### Code Quality
- **Clean Architecture**: Separation of concerns with DTOs, Services, and Controllers
- **Validation**: Comprehensive input validation with custom error messages
- **Security**: Role-based access control and ownership validation
- **Documentation**: Extensive inline documentation and Swagger specifications

### Testing & Development
- **Mock S3 Service**: Development-friendly image service when AWS is disabled
- **Database Seeding**: Easy setup with sample data
- **Error Handling**: Comprehensive exception handling with meaningful error messages

## 🚀 Production Considerations

### Security
- All sensitive operations require proper authentication and authorization
- User ownership validation ensures users can only access their own resources
- Input validation and sanitization prevent common security vulnerabilities

### Performance
- **Database Optimization**: Efficient queries with proper indexing
- **Pagination**: All list endpoints support pagination to handle large datasets
- **Lazy Loading**: Optimized JPA relationships to minimize database queries

### Scalability
- **Cloud-ready**: AWS S3 integration for scalable image storage
- **Microservice-ready**: Clean separation of concerns for future service extraction
- **Database Performance**: Optimized queries and proper relationship management

## 📊 API Usage Statistics

The platform provides comprehensive analytics for:
- **Storefront Performance**: Sales, views, ratings
- **Item Analytics**: View counts, sales, inventory levels
- **User Engagement**: Registration trends, active storefronts
- **Platform Health**: API usage, error rates, response times

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
