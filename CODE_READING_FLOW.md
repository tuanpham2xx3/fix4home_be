# 📖 Flow Đọc Code Fix4Home Backend

## 🎯 Mục tiêu
Hiểu architecture, business logic và cách implement của Fix4Home backend một cách có hệ thống.

## 📋 Flow Đọc Code (Top-Down Approach)

### 🏗️ **LEVEL 1: ARCHITECTURE OVERVIEW**

#### 1.1 Bắt đầu với Entry Point
```
📁 src/main/java/com/fix4home/fix4home/
├── Fix4homeApplication.java ← BẮT ĐẦU TỪ ĐÂY
```

**Đọc thứ tự:**
1. `@SpringBootApplication` annotation
2. `main()` method 
3. Configuration imports

#### 1.2 Package Structure Overview
```
📦 com.fix4home.fix4home
├── 📁 config/           ← Security & Configuration
├── 📁 controller/       ← API Endpoints (REST Layer)
├── 📁 service/          ← Business Logic Layer  
├── 📁 repository/       ← Data Access Layer
├── 📁 model/           ← Data Models & DTOs
├── 📁 security/        ← JWT & Authentication
└── 📁 exception/       ← Error Handling
```

### 🔐 **LEVEL 2: SECURITY & CONFIG**

#### 2.1 Security Configuration (QUAN TRỌNG!)
```
📁 config/SecurityConfig.java
📁 security/
├── JwtTokenProvider.java      ← JWT logic
├── JwtAuthenticationFilter.java ← Request filtering
├── JwtAuthenticationEntryPoint.java ← Error handling
└── CustomUserDetailsService.java ← User loading
```

**Flow đọc Security:**
1. `SecurityConfig.java` → Cấu hình security rules
2. `JwtTokenProvider.java` → Cách tạo/validate JWT
3. `JwtAuthenticationFilter.java` → Filter mỗi request
4. `CustomUserDetailsService.java` → Load user từ DB

### 📊 **LEVEL 3: DATA MODEL**

#### 3.1 Domain Entities (Core Business Objects)
```
📁 model/entity/
├── User.java ← ROOT ENTITY (bắt đầu từ đây)
├── CustomerProfile.java
├── TechnicianProfile.java
├── Service.java
├── ServiceRequest.java
└── ...
```

**Flow đọc Entities:**
1. `User.java` → Base user với roles
2. `CustomerProfile.java` + `TechnicianProfile.java` → User extensions
3. `Service.java` → Services offered
4. `ServiceRequest.java` → Business workflow
5. Relationships: `@OneToMany`, `@ManyToOne`, etc.

#### 3.2 Enums (Business Rules)
```
📁 model/enums/
├── Role.java ← User roles
├── UserStatus.java ← User states
├── ServiceRequestStatus.java ← Workflow states
└── ...
```

#### 3.3 DTOs (API Contracts)
```
📁 model/dto/
├── auth/ ← Authentication DTOs
└── common/ ← Shared DTOs
```

### 🗄️ **LEVEL 4: DATA LAYER**

#### 4.1 Repositories (Database Access)
```
📁 repository/
├── UserRepository.java ← User CRUD + custom queries
├── CustomerProfileRepository.java
├── TechnicianProfileRepository.java
└── ...
```

**Đọc pattern:**
1. `extends JpaRepository<Entity, ID>`
2. Custom query methods
3. `@Query` annotations cho complex queries

### 💼 **LEVEL 5: BUSINESS LOGIC**

#### 5.1 Services (Core Logic)
```
📁 service/
└── AuthService.java ← QUAN TRỌNG! Authentication logic
```

**Flow đọc AuthService:**
1. `registerUser()` → Registration logic
2. `login()` → Authentication logic  
3. Profile creation logic
4. Password encryption
5. JWT token generation

### 🌐 **LEVEL 6: API LAYER**

#### 6.1 Controllers (REST Endpoints)
```
📁 controller/
├── AuthController.java ← Authentication APIs
└── TestController.java ← Test endpoints
```

**Flow đọc Controllers:**
1. `@RestController` + `@RequestMapping`
2. HTTP methods: `@PostMapping`, `@GetMapping`
3. Request/Response DTOs
4. Service injection và calls
5. Error handling

### ⚠️ **LEVEL 7: ERROR HANDLING**

#### 7.1 Exception Management
```
📁 exception/
├── GlobalExceptionHandler.java ← Centralized error handling
├── BadRequestException.java
└── ResourceAlreadyExistsException.java
```

## 🔄 **FLOW ĐỌC THEO USE CASE**

### 🎯 Use Case 1: User Registration Flow

**Step-by-step code reading:**

1. **API Entry Point**
```java
// AuthController.java
@PostMapping("/register")
public ResponseEntity<ApiResponse<AuthResponse>> register(
    @Valid @RequestBody RegisterRequest request
)
```

2. **DTO Validation**
```java
// model/dto/auth/RegisterRequest.java
// Xem validation rules: @NotBlank, @Email, etc.
```

3. **Business Logic**
```java
// AuthService.java → registerUser()
// 1. Check if user exists
// 2. Create User entity
// 3. Create profile based on role
// 4. Save to database
// 5. Generate JWT token
```

4. **Data Persistence**
```java
// UserRepository.java
// TechnicianProfileRepository.java / CustomerProfileRepository.java
```

5. **Response Formation**
```java
// model/dto/auth/AuthResponse.java
// model/dto/common/ApiResponse.java
```

### 🎯 Use Case 2: Authentication Flow

1. **Login Request**
```java
// AuthController.java → login()
```

2. **Credential Validation**
```java
// AuthService.java → login()
// 1. Find user by username/email
// 2. Check password with BCrypt
// 3. Verify user status
```

3. **JWT Generation**
```java
// JwtTokenProvider.java → generateToken()
```

4. **Response with Token**
```java
// AuthResponse.java with JWT token
```

### 🎯 Use Case 3: Protected Endpoint Access

1. **Request Filtering**
```java
// JwtAuthenticationFilter.java → doFilterInternal()
// 1. Extract JWT from header
// 2. Validate token
// 3. Load user details
// 4. Set authentication context
```

2. **Authorization Check**
```java
// TestController.java
@PreAuthorize("hasRole('CUSTOMER')")
// Spring Security checks role
```

3. **Business Logic Execution**
```java
// Controller method executes if authorized
```

## 🧭 **NAVIGATION TIPS**

### 🔍 Cách Follow Code Flow

1. **Từ Controller xuống:**
   - Controller → Service → Repository → Entity

2. **Từ Entity lên:**
   - Entity → Repository → Service → Controller

3. **Follow Dependencies:**
   - `@Autowired` / `@RequiredArgsConstructor`
   - Method calls giữa layers

4. **Trace Annotations:**
   - `@Entity` → Database mapping
   - `@RestController` → API endpoints
   - `@Service` → Business logic
   - `@Repository` → Data access

### 🎯 Key Patterns to Understand

1. **Dependency Injection:**
```java
@RequiredArgsConstructor // Lombok
private final UserRepository userRepository;
```

2. **Entity Relationships:**
```java
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
private List<ServiceRequest> serviceRequests;
```

3. **DTO Mapping:**
```java
// Entity → DTO conversion in services
```

4. **Exception Handling:**
```java
@ExceptionHandler(BadRequestException.class)
public ResponseEntity<ApiResponse<Object>> handleBadRequest(...)
```

## 📚 **READING ORDER RECOMMENDATIONS**

### 🚀 For Beginners:
1. `Fix4homeApplication.java` (entry point)
2. `User.java` (core entity)
3. `Role.java` (understand roles)
4. `AuthController.java` (API structure)
5. `AuthService.java` (business logic)

### 🔥 For Intermediate:
1. Security package (authentication flow)
2. Entity relationships (data model)
3. Repository custom queries
4. Exception handling strategy
5. Configuration classes

### ⚡ For Advanced:
1. JWT implementation details
2. Security filter chain
3. Custom validation logic
4. Transaction management
5. Performance optimizations

## 🛠️ **TOOLS FOR CODE READING**

### 📱 IDE Features:
- **Go to Definition** (Ctrl+Click)
- **Find Usages** (Alt+F7)  
- **Call Hierarchy** (Ctrl+Alt+H)
- **Type Hierarchy** (Ctrl+H)

### 🔎 Search Patterns:
- Search for `@RestController` → Find all APIs
- Search for `@Entity` → Find all entities
- Search for `@Service` → Find all services
- Search for `@Repository` → Find all repositories

## 🎯 **UNDERSTANDING BUSINESS LOGIC**

### 👥 User Management:
```
Registration → User Creation → Profile Setup → JWT Token
```

### 🔐 Authentication:
```
Login Request → Credential Check → Token Generation → Response
```

### 🛡️ Authorization:
```
Request → JWT Filter → Role Check → Access Grant/Deny
```

### 📊 Data Flow:
```
Controller → Service → Repository → Database
Database → Repository → Service → Controller → Response
```

## 🎉 **SUCCESS METRICS**

Bạn hiểu code tốt khi có thể:

✅ Trace một request từ controller đến database và ngược lại  
✅ Hiểu entity relationships và business rules  
✅ Explain authentication và authorization flow  
✅ Identify where to add new features  
✅ Debug issues by following code path  

## 💡 **PRO TIPS**

1. **Start with tests** → Hiểu expected behavior
2. **Draw diagrams** → Visualize relationships
3. **Run debugger** → Step through code
4. **Read git history** → Understand evolution
5. **Ask "Why?"** → Understand design decisions

**🚀 Happy Code Reading!** 