# Fix4Home Backend - Testing & Documentation Suite

## 📋 Overview

This repository contains comprehensive testing and documentation resources for the **Fix4Home Backend API**. All resources have been consolidated and organized for easy access and usage.

## 🎯 What's Included

### ✅ **Complete API Test Suite**
- **`complete_api_test_suite.ps1`** - Comprehensive PowerShell test script covering all APIs
- **`Fix4Home_Complete_Postman_Collection.json`** - Complete Postman collection with all endpoints
- Individual component test scripts (service, customer, technician, etc.)

### ✅ **Documentation**
- **`CODEBASE_READING_GUIDE.md`** - Complete guide for understanding the codebase
- **`API_DOCUMENTATION_SUMMARY.md`** - Comprehensive API documentation
- **`.document/fullin4.md`** - Original detailed project specification

### ✅ **Legacy Resources**
- Original Postman collection: `Fix4Home_Postman_Collection.json`
- Browser testing: `browser_api_test.html`
- Curl examples: `curl_examples.sh`

## 🚀 Quick Start

### 1. **Test All APIs**
```bash
# Run comprehensive test suite
.\complete_api_test_suite.ps1

# Test with custom URL
.\complete_api_test_suite.ps1 -BaseUrl "http://192.168.1.100:8080"

# Show help
.\complete_api_test_suite.ps1 -Help
```

### 2. **Test Individual Components**
```bash
# Service API tests
.\service_api_test.ps1

# Customer API tests  
.\customer_api_test.ps1

# Technician API tests
.\technician_api_test.ps1

# Service Request workflow tests
.\service_request_api_test.ps1

# Notification system tests
.\notification_api_test.ps1

# Admin functionality tests
.\admin_api_test.ps1
```

### 3. **Use Postman Collection**
1. Import `Fix4Home_Complete_Postman_Collection.json` into Postman
2. Set environment variables (baseUrl, tokens)
3. Run individual requests or entire collection

### 4. **Read Documentation**
- **Start here**: `CODEBASE_READING_GUIDE.md`
- **API Reference**: `API_DOCUMENTATION_SUMMARY.md`
- **Project Spec**: `.document/fullin4.md`

## 📁 File Structure

```
Fix4Home_BE/
├── 📄 README_TESTING_DOCUMENTATION.md    # This file
├── 🧪 Testing Scripts
│   ├── complete_api_test_suite.ps1       # ⭐ Main test suite
│   ├── service_api_test.ps1              # Service API tests
│   ├── customer_api_test.ps1             # Customer API tests
│   ├── technician_api_test.ps1           # Technician API tests
│   ├── service_request_api_test.ps1      # Service request tests
│   ├── notification_api_test.ps1         # Notification tests
│   ├── admin_api_test.ps1                # Admin tests
│   └── api_test_guide.ps1                # Basic test guide
├── 📚 Documentation
│   ├── CODEBASE_READING_GUIDE.md         # ⭐ Complete codebase guide
│   ├── API_DOCUMENTATION_SUMMARY.md      # ⭐ API documentation
│   └── .document/fullin4.md              # Original specification
├── 📮 Postman Collections
│   ├── Fix4Home_Complete_Postman_Collection.json  # ⭐ Complete collection
│   └── Fix4Home_Postman_Collection.json           # Legacy collection
├── 🌐 Web Testing
│   └── browser_api_test.html             # Browser-based testing
├── 💻 Shell Examples
│   └── curl_examples.sh                  # Curl command examples
└── ☕ Source Code
    └── src/                              # Java Spring Boot application
```

## 🎯 Testing Workflow

### **Step 1: Environment Setup**
```bash
# 1. Start the Spring Boot application
./mvnw spring-boot:run

# 2. Verify health check
curl http://localhost:8080/api/v1/test/health
```

### **Step 2: Run Comprehensive Tests**
```bash
# Run all tests with detailed output
.\complete_api_test_suite.ps1

# Expected output:
# ================================================================================
#  FIX4HOME COMPLETE API TEST SUITE
# ================================================================================
# 
# Base URL: http://localhost:8080
# Mode: FULL
# 
# ================================================================================
#  HEALTH CHECK TESTS
# ================================================================================
# ... test results ...
# 
# 📊 TEST RESULTS:
#    Total Tests: 45
#    ✅ Passed: 42
#    ❌ Failed: 3
# 
# 🎯 SUCCESS RATE: 93.33%
```

### **Step 3: Review Results**
- ✅ **All Green**: API is ready for production
- ⚠️ **Some Failures**: Check error messages and fix issues
- ❌ **Many Failures**: Verify server is running and database is connected

## 🔧 Component Testing

### **Service API Testing**
```bash
.\service_api_test.ps1
# Tests: Create, Read, Update, Delete services
# Security: Admin-only operations
# Public: Service browsing endpoints
```

### **Customer API Testing**
```bash
.\customer_api_test.ps1
# Tests: Profile management, address CRUD
# Security: Customer-specific data access
# Admin: Customer management operations
```

### **Technician API Testing**
```bash
.\technician_api_test.ps1
# Tests: Profile management, skills, approval workflow
# Public: Technician browsing
# Admin: Technician management
```

### **Service Request Testing**
```bash
.\service_request_api_test.ps1
# Tests: Complete booking workflow
# Workflow: PENDING → ASSIGNED → IN_PROGRESS → DONE
# Roles: Customer creates, Technician executes, Admin manages
```

### **Notification Testing**
```bash
.\notification_api_test.ps1
# Tests: Create, read, mark notifications
# Features: Unread count, bulk operations
# Admin: System notifications
```

### **Admin Testing**
```bash
.\admin_api_test.ps1
# Tests: System overview, user management
# Features: Bulk operations, statistics
# Security: Admin-only access
```

## 📊 Test Categories

### 🔍 **Health & Connectivity**
- Application health check
- Database connectivity
- Basic API responsiveness

### 🔐 **Authentication & Security**
- User registration (Customer, Technician, Admin)
- Login/logout functionality
- JWT token validation
- Role-based access control
- Cross-role security testing

### 📋 **CRUD Operations**
- **Services**: Create, read, update, delete
- **Customers**: Profile and address management
- **Technicians**: Profile, skills, approval
- **Service Requests**: Complete workflow
- **Payments**: Payment processing
- **Feedback**: Rating and review system
- **Notifications**: Real-time notifications

### 🔄 **Workflow Testing**
- Complete service booking flow
- Payment processing
- Feedback submission
- Admin management operations

### ⚠️ **Error Handling**
- Invalid data validation
- Unauthorized access attempts
- Non-existent resource requests
- Cross-user data access prevention

## 📈 Success Metrics

### **API Readiness Indicators**
- ✅ **Health Check**: 100% pass rate
- ✅ **Authentication**: All roles working
- ✅ **CRUD Operations**: All entities functional
- ✅ **Workflow**: Complete booking process working
- ✅ **Security**: Unauthorized access blocked
- ✅ **Error Handling**: Proper error responses

### **Performance Indicators**
- Response time < 2 seconds for most endpoints
- Database connections stable
- No memory leaks during testing
- Concurrent user handling

## 🛠️ Troubleshooting

### **Common Issues**

#### ❌ **Connection Refused**
```bash
# Check if server is running
curl http://localhost:8080/api/v1/test/health

# Start server if needed
./mvnw spring-boot:run
```

#### ❌ **Database Errors**
```sql
-- Verify database exists
SHOW DATABASES;
USE fix4home_db;
SHOW TABLES;
```

#### ❌ **Authentication Failures**
```bash
# Check JWT token format
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

# Verify token hasn't expired (24 hours default)
```

#### ❌ **CORS Issues**
- Check frontend domain configuration
- Verify CORS settings in SecurityConfig.java

### **Debug Mode**
```bash
# Run tests with verbose output
.\complete_api_test_suite.ps1 -Verbose

# Check application logs
tail -f logs/application.log
```

## 📚 Documentation Guide

### **For Developers**
1. **Start with**: `CODEBASE_READING_GUIDE.md`
2. **Understand architecture**: Project structure, layers, patterns
3. **Learn APIs**: `API_DOCUMENTATION_SUMMARY.md`
4. **Run tests**: `complete_api_test_suite.ps1`

### **For Testers**
1. **Use Postman**: Import complete collection
2. **Run automated tests**: PowerShell scripts
3. **Manual testing**: Browser testing with `browser_api_test.html`
4. **Report issues**: Document failing test cases

### **For DevOps**
1. **Health monitoring**: `/api/v1/test/health`
2. **Database check**: `/api/v1/test/database`
3. **Performance testing**: Load testing with test scripts
4. **Deployment verification**: Run test suite post-deployment

## 🎯 Next Steps

### **Frontend Integration**
1. Use API documentation to integrate frontend
2. Implement authentication flow
3. Connect service booking workflow
4. Add real-time notifications

### **Production Deployment**
1. Run full test suite
2. Verify all security measures
3. Setup monitoring and logging
4. Configure production database
5. Implement CI/CD pipeline

### **Performance Optimization**
1. Add caching where appropriate
2. Optimize database queries
3. Implement rate limiting
4. Add application monitoring

## 📞 Support

### **Resources**
- **Live API Documentation**: http://localhost:8080/swagger-ui.html
- **Database Schema**: `.document/sql_ver0.2.sql`
- **Project Specification**: `.document/fullin4.md`

### **Testing Support**
- **Test Suite Issues**: Check PowerShell execution policy
- **API Issues**: Verify server configuration
- **Database Issues**: Check MySQL connection settings

---

## 🏆 Summary

✅ **Complete API Test Suite** - Comprehensive testing for all endpoints  
✅ **Updated Postman Collection** - All endpoints with proper authentication  
✅ **Codebase Reading Guide** - Complete development documentation  
✅ **API Documentation** - Consolidated endpoint reference  
✅ **Organized File Structure** - Clean, maintainable test and documentation files  

**The Fix4Home Backend is fully tested and documented, ready for frontend integration and production deployment! 🚀**

---

**Version**: 1.0  
**Last Updated**: 2024-12-23  
**Status**: ✅ Complete  
**Maintained by**: Fix4Home Development Team 