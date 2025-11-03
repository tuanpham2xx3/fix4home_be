# Hướng Dẫn Triển Khai Token Bảo Mật với Cookies

## Mục Lục
1. [Tổng Quan](#tổng-quan)
2. [Cấu Trúc Token](#cấu-trúc-token)
3. [Triển Khai Backend](#triển-khai-backend)
4. [Triển Khai Frontend](#triển-khai-frontend)
5. [Bảo Mật và Khắc Phục Rủi Ro](#bảo-mật-và-khắc-phục-rủi-ro)
6. [Monitoring và Logging](#monitoring-và-logging)

## Tổng Quan

### Flow Authentication
1. User login → nhận access token (memory) và refresh token (httpOnly cookie)
2. Access token hết hạn → dùng refresh token để lấy access token mới
3. Refresh token hết hạn → user phải login lại
4. Logout → clear cả access token và refresh token

### Thời Gian Hết Hạn
- Access Token: 15 phút
- Refresh Token: 30 ngày
- Remember Me: Được handle bởi refresh token

## Cấu Trúc Token

### Access Token
```json
{
  "sub": "username",
  "roles": ["ROLE_USER"],
  "iat": 1234567890,
  "exp": 1234567890
}
```

### Refresh Token
- Được lưu trong database
- Liên kết với user và deviceId
- Được rotate mỗi lần refresh

## Triển Khai Backend

### 1. Entity và Repository

```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    private String deviceId;
}

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserAndDeviceId(User user, String deviceId);
    void deleteByUser(User user);
}
```

### 2. Service Layer

```java
@Service
@Slf4j
public class RefreshTokenService {
    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenDurationMs;

    public RefreshToken createRefreshToken(Long userId, String deviceId) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId)));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setDeviceId(deviceId);
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired");
        }
        return token;
    }
}
```

### 3. Controller Layer

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, 
                                 @RequestHeader(value = "X-Device-Id") String deviceId,
                                 HttpServletResponse response) {
        Authentication authentication = authService.authenticate(request);
        String accessToken = tokenProvider.generateAccessToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId, deviceId);
        
        addRefreshTokenCookie(response, refreshToken.getToken());
        
        return ResponseEntity.ok(new AuthResponse(accessToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refresh_token") String refreshToken,
            @RequestHeader(value = "X-Device-Id") String deviceId) {
        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    String accessToken = tokenProvider.generateAccessToken(token.getUser());
                    return ResponseEntity.ok(new AuthResponse(accessToken));
                })
                .orElseThrow(() -> new TokenRefreshException(refreshToken, 
                    "Refresh token not found"));
    }
}
```

### 4. Security Configuration

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://your-domain.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

## Triển Khai Frontend

### 1. Auth Service

```typescript
class AuthService {
    private accessToken: string | null = null;

    constructor() {
        axios.defaults.withCredentials = true;
    }

    async login(credentials: LoginRequest): Promise<void> {
        const deviceId = this.getOrCreateDeviceId();
        const response = await axios.post('/api/v1/auth/login', credentials, {
            headers: { 'X-Device-Id': deviceId }
        });
        this.setAccessToken(response.data.accessToken);
    }

    private async refreshToken(): Promise<void> {
        const deviceId = this.getDeviceId();
        const response = await axios.post('/api/v1/auth/refresh', null, {
            headers: { 'X-Device-Id': deviceId }
        });
        this.setAccessToken(response.data.accessToken);
    }

    private setupAxiosInterceptor() {
        axios.interceptors.request.use(config => {
            if (this.accessToken) {
                config.headers.Authorization = `Bearer ${this.accessToken}`;
            }
            return config;
        });

        axios.interceptors.response.use(
            response => response,
            async error => {
                if (error.response?.status === 401) {
                    try {
                        await this.refreshToken();
                        return axios(error.config);
                    } catch {
                        this.logout();
                    }
                }
                return Promise.reject(error);
            }
        );
    }
}
```

## Bảo Mật và Khắc Phục Rủi Ro

### 1. Rate Limiting

```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final RateLimiter rateLimiter = RateLimiter.create(10.0 / 60.0);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain) {
        if (request.getRequestURI().equals("/api/v1/auth/refresh")) {
            if (!rateLimiter.tryAcquire()) {
                response.setStatus(429);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
```

### 2. Security Headers

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .headers(headers -> headers
                .frameOptions().deny()
                .xssProtection()
                .contentSecurityPolicy("default-src 'self'")
                .referrerPolicy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                .httpStrictTransportSecurity()
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
            )
            .build();
    }
}
```

### 3. Cookie Configuration

```java
private Cookie createSecureCookie(String name, String value) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/api/v1/auth/refresh");
    cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
    cookie.setAttribute("SameSite", "Strict");
    return cookie;
}
```

## Monitoring và Logging

### 1. Token Usage Monitoring

```java
@Aspect
@Component
@Slf4j
public class TokenUsageAspect {
    @Around("execution(* com.fix4home.security.RefreshTokenService.*(..))")
    public Object logTokenUsage(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        try {
            Object result = joinPoint.proceed();
            log.info("Token operation successful: {}", methodName);
            return result;
        } catch (Throwable e) {
            log.error("Token operation failed: {}", methodName, e);
            throw e;
        }
    }
}
```

### 2. Security Events Logging

```java
@Service
@Slf4j
public class SecurityEventLogger {
    public void logSecurityEvent(String username, String event, String details) {
        log.info("Security Event: {} - User: {} - Details: {}", 
                 event, username, details);
        
        // Send to monitoring system
        MetricsRegistry.counter(
            "security.events",
            "event", event,
            "username", username
        ).increment();
    }
}
```

## Cấu Hình Application Properties

```properties
# JWT Configuration
jwt.access-token.expiration=900000      # 15 minutes
jwt.refresh-token.expiration=2592000000 # 30 days
jwt.secret=${JWT_SECRET}

# Security Configuration
security.require-ssl=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict

# CORS Configuration
app.cors.allowed-origins=${CORS_ORIGINS}
app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
app.cors.allowed-headers=*
app.cors.allow-credentials=true

# Rate Limiting
app.ratelimit.enabled=true
app.ratelimit.refresh-token.limit=10
app.ratelimit.refresh-token.duration=60
```

## Khuyến Nghị Triển Khai

1. **Theo Thứ Tự**:
   - Implement entities và repositories
   - Setup security configuration
   - Implement services
   - Implement controllers
   - Setup monitoring
   - Configure properties

2. **Testing**:
   - Unit tests cho services
   - Integration tests cho flow authentication
   - Load tests cho rate limiting
   - Security tests cho cookie configuration

3. **Monitoring**:
   - Setup logging
   - Configure metrics
   - Setup alerts
   - Monitor token usage

4. **Security Checklist**:
   - HTTPS enabled
   - CORS configured
   - Rate limiting active
   - Security headers set
   - Cookie security
   - Token rotation
   - Proper error handling 