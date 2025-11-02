# FIX4HOME REACT NATIVE MOBILE REQUIREMENTS

## Tổng quan dự án

Fix4Home Mobile App là ứng dụng di động được phát triển bằng React Native để cung cấp trải nghiệm tối ưu cho người dùng trên thiết bị di động. App kết nối với cùng backend Spring Boot như web version, đảm bảo tính nhất quán về dữ liệu và tính năng.

## Mục lục

1. [Tổng quan hệ thống](#tổng-quan-hệ-thống)
2. [Vai trò người dùng](#vai-trò-người-dùng)
3. [Tech Stack Requirements](#tech-stack-requirements)
4. [Cấu trúc dự án](#cấu-trúc-dự-án)
5. [Navigation & App Architecture](#navigation--app-architecture)
6. [Tính năng chính](#tính-năng-chính)
7. [Mobile-Specific Features](#mobile-specific-features)
8. [API Integration](#api-integration)
9. [UI/UX Mobile Design](#uiux-mobile-design)
10. [Bảo mật](#bảo-mật)
11. [Performance & Optimization](#performance--optimization)
12. [Testing](#testing)
13. [Deployment](#deployment)

---

## Tổng quan hệ thống

### Backend Integration
- **Base URL**: `http://localhost:8100/api/v1` (Development)
- **Authentication**: JWT Bearer Token với refresh token
- **Response Format**: JSON với cấu trúc chuẩn
- **Real-time**: WebSocket cho chat và notifications
- **Total Endpoints**: 113+ API endpoints

### Platform Support
- **iOS**: iOS 12.0+ (React Native 0.72+)
- **Android**: Android 6.0+ (API level 23+)
- **Development**: Expo CLI hoặc React Native CLI

### Cấu trúc Response chuẩn
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## Vai trò người dùng

### 1. CUSTOMER (Khách hàng) - Mobile Priority
**Tính năng chính:**
- Đăng ký/đăng nhập với biometric authentication
- Quản lý profile với avatar upload từ camera
- Tạo Service Request với location picker
- Tạo Service Post với photo upload
- Chat real-time với push notifications
- Thanh toán in-app
- Rating & review với photo upload
- Track service status real-time
- Receive location-based notifications

### 2. TECHNICIAN (Thợ sửa chữa) - Mobile Priority  
**Tính năng chính:**
- Profile management với portfolio photos
- Browse available jobs với location filter
- Accept/decline jobs với quick actions
- Navigation integration đến địa chỉ khách hàng
- Update job status on-the-go
- Chat với khách hàng
- Photo documentation của công việc
- Earnings tracking và withdrawal
- Receive job notifications

### 3. ADMIN (Quản trị viên) - Limited Mobile Support
**Tính năng cơ bản:**
- Dashboard overview với key metrics
- User management cơ bản
- Approve/reject technician registrations
- View reports và analytics
- System notifications management

---

## Tech Stack Requirements

### Core Framework
```json
{
  "framework": "React Native 0.72+",
  "language": "TypeScript",
  "cli": "React Native CLI hoặc Expo CLI",
  "package_manager": "npm hoặc yarn"
}
```

### Essential Libraries

#### Navigation
- **@react-navigation/native** - Navigation framework
- **@react-navigation/stack** - Stack navigation
- **@react-navigation/bottom-tabs** - Bottom tab navigation
- **@react-navigation/drawer** - Drawer navigation
- **react-native-screens** - Native screen optimization

#### State Management
- **@reduxjs/toolkit** - State management
- **react-redux** - React bindings for Redux
- **@tanstack/react-query** - Server state management
- **redux-persist** - Persist Redux state

#### HTTP Client & Real-time
- **axios** - HTTP requests
- **socket.io-client** - Real-time communication
- **@react-native-async-storage/async-storage** - Local storage

#### UI Components & Styling
- **react-native-elements** hoặc **NativeBase** - UI component library
- **react-native-vector-icons** - Icon library
- **react-native-svg** - SVG support
- **styled-components** - CSS-in-JS styling
- **react-native-super-grid** - Grid layouts

#### Form Handling
- **react-hook-form** - Form management
- **yup** - Schema validation

#### Device Features
- **react-native-permissions** - Permission management
- **@react-native-camera/camera** - Camera functionality
- **react-native-image-picker** - Image selection
- **react-native-image-crop-picker** - Image cropping
- **@react-native-geolocation/geolocation** - Location services
- **react-native-maps** - Maps integration

#### Push Notifications
- **@react-native-firebase/messaging** - Firebase Cloud Messaging
- **@react-native-firebase/app** - Firebase core
- **react-native-push-notification** - Local notifications

#### Authentication & Security
- **react-native-keychain** - Secure storage
- **react-native-touch-id** hoặc **react-native-biometrics** - Biometric authentication
- **react-native-crypto** - Encryption utilities

#### Date & Time
- **date-fns** - Date manipulation
- **react-native-date-picker** - Date picker component

#### File Upload & Media
- **react-native-fs** - File system access
- **react-native-document-picker** - Document picker
- **react-native-video** - Video player (if needed)

#### Development Tools
- **flipper** - Debugging tool
- **react-native-debugger** - Development debugger
- **@react-native-community/eslint-config** - ESLint configuration

#### Testing
- **@testing-library/react-native** - Testing utilities
- **jest** - Testing framework
- **detox** - E2E testing for React Native

---

## Cấu trúc dự án

```
Fix4HomeMobile/
├── android/                    # Android native code
├── ios/                       # iOS native code
├── src/
│   ├── api/                   # API services
│   │   ├── client.ts          # Axios configuration
│   │   ├── auth.ts           # Authentication APIs
│   │   ├── customer.ts       # Customer APIs
│   │   ├── technician.ts     # Technician APIs
│   │   ├── services.ts       # Service APIs
│   │   ├── chat.ts           # Chat APIs
│   │   └── index.ts
│   ├── components/            # Reusable components
│   │   ├── common/
│   │   │   ├── Header.tsx
│   │   │   ├── Loading.tsx
│   │   │   ├── ErrorBoundary.tsx
│   │   │   ├── EmptyState.tsx
│   │   │   └── RefreshControl.tsx
│   │   ├── forms/
│   │   │   ├── LoginForm.tsx
│   │   │   ├── RegisterForm.tsx
│   │   │   ├── ServiceRequestForm.tsx
│   │   │   └── ProfileForm.tsx
│   │   ├── ui/                # Base UI components
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Card.tsx
│   │   │   ├── Modal.tsx
│   │   │   ├── Avatar.tsx
│   │   │   └── Badge.tsx
│   │   ├── lists/             # List components
│   │   │   ├── ServiceList.tsx
│   │   │   ├── JobList.tsx
│   │   │   ├── ChatList.tsx
│   │   │   └── NotificationList.tsx
│   │   └── media/
│   │       ├── ImagePicker.tsx
│   │       ├── CameraView.tsx
│   │       └── ImageGallery.tsx
│   ├── screens/               # Screen components
│   │   ├── auth/
│   │   │   ├── LoginScreen.tsx
│   │   │   ├── RegisterScreen.tsx
│   │   │   ├── ForgotPasswordScreen.tsx
│   │   │   └── BiometricSetupScreen.tsx
│   │   ├── customer/
│   │   │   ├── DashboardScreen.tsx
│   │   │   ├── ServicesScreen.tsx
│   │   │   ├── CreateRequestScreen.tsx
│   │   │   ├── CreatePostScreen.tsx
│   │   │   ├── RequestHistoryScreen.tsx
│   │   │   ├── ProfileScreen.tsx
│   │   │   └── AddressManagementScreen.tsx
│   │   ├── technician/
│   │   │   ├── DashboardScreen.tsx
│   │   │   ├── AvailableJobsScreen.tsx
│   │   │   ├── MyJobsScreen.tsx
│   │   │   ├── JobDetailScreen.tsx
│   │   │   ├── NavigationScreen.tsx
│   │   │   ├── ProfileScreen.tsx
│   │   │   └── EarningsScreen.tsx
│   │   ├── chat/
│   │   │   ├── ChatListScreen.tsx
│   │   │   └── ChatRoomScreen.tsx
│   │   ├── shared/
│   │   │   ├── NotificationsScreen.tsx
│   │   │   ├── SettingsScreen.tsx
│   │   │   ├── MapScreen.tsx
│   │   │   └── PaymentScreen.tsx
│   │   └── admin/             # Limited admin screens
│   │       ├── DashboardScreen.tsx
│   │       └── UserManagementScreen.tsx
│   ├── navigation/            # Navigation configuration
│   │   ├── AppNavigator.tsx
│   │   ├── AuthNavigator.tsx
│   │   ├── CustomerNavigator.tsx
│   │   ├── TechnicianNavigator.tsx
│   │   ├── AdminNavigator.tsx
│   │   └── types.ts
│   ├── hooks/                 # Custom hooks
│   │   ├── useAuth.ts
│   │   ├── useLocation.ts
│   │   ├── useCamera.ts
│   │   ├── usePushNotifications.ts
│   │   ├── useSocket.ts
│   │   ├── useBiometric.ts
│   │   └── usePermissions.ts
│   ├── store/                 # Redux store
│   │   ├── auth/
│   │   │   ├── authSlice.ts
│   │   │   └── authTypes.ts
│   │   ├── user/
│   │   │   ├── userSlice.ts
│   │   │   └── userTypes.ts
│   │   ├── services/
│   │   │   ├── servicesSlice.ts
│   │   │   └── servicesTypes.ts
│   │   ├── chat/
│   │   │   ├── chatSlice.ts
│   │   │   └── chatTypes.ts
│   │   ├── notifications/
│   │   │   ├── notificationsSlice.ts
│   │   │   └── notificationsTypes.ts
│   │   └── store.ts
│   ├── types/                 # TypeScript types
│   │   ├── api.ts
│   │   ├── user.ts
│   │   ├── service.ts
│   │   ├── navigation.ts
│   │   └── index.ts
│   ├── utils/                 # Utility functions
│   │   ├── auth.ts
│   │   ├── formatters.ts
│   │   ├── validators.ts
│   │   ├── permissions.ts
│   │   ├── location.ts
│   │   ├── storage.ts
│   │   └── constants.ts
│   ├── services/              # Device services
│   │   ├── LocationService.ts
│   │   ├── NotificationService.ts
│   │   ├── BiometricService.ts
│   │   ├── CameraService.ts
│   │   └── StorageService.ts
│   ├── assets/                # Static assets
│   │   ├── images/
│   │   ├── icons/
│   │   ├── fonts/
│   │   └── animations/        # Lottie animations
│   ├── config/                # Configuration
│   │   ├── api.ts
│   │   ├── navigation.ts
│   │   ├── theme.ts
│   │   └── env.ts
│   └── App.tsx
├── __tests__/                 # Test files
├── package.json
├── tsconfig.json
├── metro.config.js
├── react-native.config.js
├── babel.config.js
└── README.md
```

---

## Navigation & App Architecture

### Navigation Structure

```typescript
// navigation/AppNavigator.tsx
const AppNavigator = () => {
  const { isAuthenticated, user } = useAuth();

  return (
    <NavigationContainer>
      {!isAuthenticated ? (
        <AuthNavigator />
      ) : (
        <MainNavigator userRole={user.role} />
      )}
    </NavigationContainer>
  );
};

// Role-based navigation
const MainNavigator = ({ userRole }: { userRole: UserRole }) => {
  switch (userRole) {
    case 'CUSTOMER':
      return <CustomerNavigator />;
    case 'TECHNICIAN':
      return <TechnicianNavigator />;
    case 'ADMIN':
      return <AdminNavigator />;
    default:
      return <AuthNavigator />;
  }
};
```

### Customer Navigation
```typescript
const CustomerNavigator = () => (
  <Tab.Navigator
    screenOptions={({ route }) => ({
      tabBarIcon: ({ focused, color, size }) => {
        let iconName: string;
        switch (route.name) {
          case 'Dashboard':
            iconName = focused ? 'home' : 'home-outline';
            break;
          case 'Services':
            iconName = focused ? 'construct' : 'construct-outline';
            break;
          case 'Requests':
            iconName = focused ? 'list' : 'list-outline';
            break;
          case 'Chat':
            iconName = focused ? 'chatbubbles' : 'chatbubbles-outline';
            break;
          case 'Profile':
            iconName = focused ? 'person' : 'person-outline';
            break;
        }
        return <Ionicons name={iconName} size={size} color={color} />;
      },
    })}
  >
    <Tab.Screen name="Dashboard" component={CustomerDashboardScreen} />
    <Tab.Screen name="Services" component={ServicesScreen} />
    <Tab.Screen name="Requests" component={RequestHistoryScreen} />
    <Tab.Screen name="Chat" component={ChatListScreen} />
    <Tab.Screen name="Profile" component={ProfileScreen} />
  </Tab.Navigator>
);
```

### Technician Navigation
```typescript
const TechnicianNavigator = () => (
  <Tab.Navigator>
    <Tab.Screen name="Dashboard" component={TechnicianDashboardScreen} />
    <Tab.Screen name="Jobs" component={AvailableJobsScreen} />
    <Tab.Screen name="MyWork" component={MyJobsScreen} />
    <Tab.Screen name="Chat" component={ChatListScreen} />
    <Tab.Screen name="Earnings" component={EarningsScreen} />
    <Tab.Screen name="Profile" component={TechnicianProfileScreen} />
  </Tab.Navigator>
);
```

---

## Tính năng chính

### 1. Authentication & Biometric Security

#### Components:
- **BiometricLogin**: Đăng nhập bằng vân tay/Face ID
- **PinSetup**: Thiết lập PIN backup
- **BiometricPrompt**: Xác thực sinh trắc học

#### Features:
```typescript
// hooks/useBiometric.ts
export const useBiometric = () => {
  const checkBiometricSupport = async () => {
    const biometryType = await TouchID.isSupported();
    return biometryType !== false;
  };

  const authenticateWithBiometric = async () => {
    try {
      const isAuthenticated = await TouchID.authenticate(
        'Xác thực để đăng nhập vào Fix4Home',
        {
          fallbackLabel: 'Sử dụng mật khẩu',
          unifiedErrors: false,
        }
      );
      return isAuthenticated;
    } catch (error) {
      throw error;
    }
  };

  return { checkBiometricSupport, authenticateWithBiometric };
};
```

### 2. Location-Based Services

#### Components:
- **LocationPicker**: Chọn địa chỉ trên bản đồ
- **NearbyTechnicians**: Hiển thị thợ gần nhất
- **NavigationView**: Dẫn đường đến địa chỉ khách hàng

#### Features:
```typescript
// hooks/useLocation.ts
export const useLocation = () => {
  const getCurrentLocation = async () => {
    const hasPermission = await requestLocationPermission();
    if (!hasPermission) throw new Error('Location permission denied');

    return new Promise((resolve, reject) => {
      Geolocation.getCurrentPosition(
        position => resolve(position),
        error => reject(error),
        { enableHighAccuracy: true, timeout: 15000, maximumAge: 10000 }
      );
    });
  };

  const watchLocation = (callback: (position: any) => void) => {
    return Geolocation.watchPosition(
      callback,
      error => console.error(error),
      { enableHighAccuracy: true, distanceFilter: 10 }
    );
  };

  return { getCurrentLocation, watchLocation };
};
```

### 3. Camera & Media Features

#### Components:
- **CameraCapture**: Chụp ảnh công việc
- **ImageGallery**: Xem gallery ảnh
- **MediaUpload**: Upload multiple files

#### Implementation:
```typescript
// components/media/CameraView.tsx
const CameraView: React.FC<CameraViewProps> = ({ onCapture, onClose }) => {
  const [hasPermission, setHasPermission] = useState<boolean | null>(null);
  const cameraRef = useRef<RNCamera>(null);

  useEffect(() => {
    (async () => {
      const { status } = await Camera.requestCameraPermissionsAsync();
      setHasPermission(status === 'granted');
    })();
  }, []);

  const takePicture = async () => {
    if (cameraRef.current) {
      const options = { quality: 0.8, base64: false };
      const data = await cameraRef.current.takePictureAsync(options);
      onCapture(data);
    }
  };

  if (hasPermission === null) return <Loading />;
  if (hasPermission === false) return <Text>No camera permission</Text>;

  return (
    <View style={styles.container}>
      <RNCamera
        ref={cameraRef}
        style={styles.preview}
        type={RNCamera.Constants.Type.back}
        flashMode={RNCamera.Constants.FlashMode.off}
      />
      <View style={styles.controls}>
        <TouchableOpacity style={styles.capture} onPress={takePicture}>
          <Text style={styles.captureText}>CHỤP</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};
```

### 4. Push Notifications

#### Setup:
```typescript
// services/NotificationService.ts
class NotificationService {
  async initialize() {
    const authStatus = await messaging().requestPermission();
    const enabled =
      authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
      authStatus === messaging.AuthorizationStatus.PROVISIONAL;

    if (enabled) {
      const fcmToken = await messaging().getToken();
      await this.registerToken(fcmToken);
    }
  }

  async registerToken(token: string) {
    // Send token to backend
    await apiClient.post('/users/fcm-token', { token });
  }

  setupForegroundHandler() {
    messaging().onMessage(async remoteMessage => {
      // Handle foreground notifications
      this.showLocalNotification(remoteMessage);
    });
  }

  setupBackgroundHandler() {
    messaging().setBackgroundMessageHandler(async remoteMessage => {
      // Handle background notifications
      console.log('Background message:', remoteMessage);
    });
  }

  showLocalNotification(remoteMessage: any) {
    PushNotification.localNotification({
      title: remoteMessage.notification?.title,
      message: remoteMessage.notification?.body,
      data: remoteMessage.data,
    });
  }
}
```

### 5. Real-time Chat với File Sharing

#### Chat Screen:
```typescript
// screens/chat/ChatRoomScreen.tsx
const ChatRoomScreen: React.FC<ChatRoomProps> = ({ route }) => {
  const { conversationId } = route.params;
  const [messages, setMessages] = useState<Message[]>([]);
  const { socket } = useSocket();

  useEffect(() => {
    socket.emit('join_conversation', conversationId);
    
    socket.on('new_message', (message: Message) => {
      setMessages(prev => [...prev, message]);
    });

    return () => {
      socket.off('new_message');
    };
  }, [conversationId]);

  const sendMessage = (text: string, attachments?: Attachment[]) => {
    const message: Message = {
      id: Date.now().toString(),
      text,
      attachments,
      senderId: user.id,
      timestamp: new Date(),
    };

    socket.emit('send_message', {
      conversationId,
      message,
    });
  };

  const sendImage = async () => {
    const result = await ImagePicker.launchImageLibrary({
      mediaType: 'photo',
      quality: 0.8,
    });

    if (result.assets && result.assets[0]) {
      const attachment: Attachment = {
        type: 'image',
        uri: result.assets[0].uri!,
        fileName: result.assets[0].fileName!,
      };
      
      sendMessage('', [attachment]);
    }
  };

  return (
    <View style={styles.container}>
      <FlatList
        data={messages}
        renderItem={({ item }) => <MessageBubble message={item} />}
        keyExtractor={item => item.id}
      />
      <ChatInput 
        onSendMessage={sendMessage}
        onSendImage={sendImage}
      />
    </View>
  );
};
```

---

## Mobile-Specific Features

### 1. Offline Support

```typescript
// hooks/useNetworkStatus.ts
export const useNetworkStatus = () => {
  const [isConnected, setIsConnected] = useState(true);

  useEffect(() => {
    const unsubscribe = NetInfo.addEventListener(state => {
      setIsConnected(state.isConnected ?? false);
    });

    return () => unsubscribe();
  }, []);

  return { isConnected };
};

// Offline queue for API calls
export const useOfflineQueue = () => {
  const { isConnected } = useNetworkStatus();
  const [queue, setQueue] = useState<QueuedRequest[]>([]);

  const addToQueue = (request: QueuedRequest) => {
    setQueue(prev => [...prev, request]);
  };

  useEffect(() => {
    if (isConnected && queue.length > 0) {
      // Process queued requests
      processQueue();
    }
  }, [isConnected]);

  const processQueue = async () => {
    for (const request of queue) {
      try {
        await apiClient.request(request);
        setQueue(prev => prev.filter(r => r.id !== request.id));
      } catch (error) {
        console.error('Failed to process queued request:', error);
      }
    }
  };

  return { addToQueue, queueLength: queue.length };
};
```

### 2. Background Tasks

```typescript
// services/BackgroundTaskService.ts
import BackgroundJob from 'react-native-background-job';

class BackgroundTaskService {
  startLocationTracking() {
    BackgroundJob.start({
      jobKey: 'locationTracking',
      period: 30000, // 30 seconds
      requiredNetworkType: 'any',
    });
  }

  stopLocationTracking() {
    BackgroundJob.stop({
      jobKey: 'locationTracking',
    });
  }

  // Background sync for offline data
  startDataSync() {
    BackgroundJob.start({
      jobKey: 'dataSync',
      period: 60000, // 1 minute
      requiredNetworkType: 'connected',
    });
  }
}
```

### 3. Deep Linking

```typescript
// navigation/LinkingConfiguration.ts
const LinkingConfiguration = {
  prefixes: ['fix4home://', 'https://fix4home.app'],
  config: {
    screens: {
      Auth: {
        screens: {
          Login: 'login',
          Register: 'register',
        },
      },
      Customer: {
        screens: {
          Dashboard: 'dashboard',
          ServiceDetail: 'service/:serviceId',
          RequestDetail: 'request/:requestId',
          Chat: 'chat/:conversationId',
        },
      },
      Technician: {
        screens: {
          JobDetail: 'job/:jobId',
          Navigation: 'navigate/:jobId',
        },
      },
    },
  },
};

// Handle deep links
const useDeepLinking = () => {
  useEffect(() => {
    const handleDeepLink = (url: string) => {
      // Parse URL and navigate accordingly
      const route = Linking.parse(url);
      // Navigate to appropriate screen
    };

    Linking.addEventListener('url', handleDeepLink);
    
    // Handle app launch from deep link
    Linking.getInitialURL().then(url => {
      if (url) handleDeepLink(url);
    });

    return () => {
      Linking.removeEventListener('url', handleDeepLink);
    };
  }, []);
};
```

### 4. App State Management

```typescript
// hooks/useAppState.ts
export const useAppState = () => {
  const [appState, setAppState] = useState(AppState.currentState);

  useEffect(() => {
    const handleAppStateChange = (nextAppState: AppStateStatus) => {
      if (appState.match(/inactive|background/) && nextAppState === 'active') {
        // App has come to the foreground
        onAppForeground();
      } else if (nextAppState.match(/inactive|background/)) {
        // App has gone to the background
        onAppBackground();
      }
      
      setAppState(nextAppState);
    };

    AppState.addEventListener('change', handleAppStateChange);
    
    return () => {
      AppState.removeEventListener('change', handleAppStateChange);
    };
  }, [appState]);

  const onAppForeground = () => {
    // Refresh data, check for new notifications, etc.
  };

  const onAppBackground = () => {
    // Save state, pause timers, etc.
  };

  return { appState };
};
```

---

## API Integration

### HTTP Client Setup cho Mobile

```typescript
// api/client.ts
import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import NetInfo from '@react-native-community/netinfo';

const API_BASE_URL = __DEV__ 
  ? 'http://10.0.2.2:8100/api/v1'  // Android emulator
  : 'https://api.fix4home.app/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
apiClient.interceptors.request.use(
  async (config) => {
    // Check network connectivity
    const netInfo = await NetInfo.fetch();
    if (!netInfo.isConnected) {
      throw new Error('No internet connection');
    }

    // Add auth token
    const token = await AsyncStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor
apiClient.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    if (error.response?.status === 401) {
      // Handle token refresh
      await handleTokenRefresh();
    }
    return Promise.reject(error);
  }
);

const handleTokenRefresh = async () => {
  try {
    const refreshToken = await AsyncStorage.getItem('refreshToken');
    if (!refreshToken) throw new Error('No refresh token');

    const response = await axios.post(`${API_BASE_URL}/auth/refresh-token`, {
      refreshToken,
    });

    const { accessToken, refreshToken: newRefreshToken } = response.data.data;
    
    await AsyncStorage.setItem('accessToken', accessToken);
    await AsyncStorage.setItem('refreshToken', newRefreshToken);
    
    return accessToken;
  } catch (error) {
    // Redirect to login
    await AsyncStorage.multiRemove(['accessToken', 'refreshToken']);
    throw error;
  }
};
```

### File Upload cho Mobile

```typescript
// api/upload.ts
export const uploadFile = async (file: ImagePickerResponse) => {
  const formData = new FormData();
  
  formData.append('file', {
    uri: file.uri,
    type: file.type,
    name: file.fileName || 'image.jpg',
  } as any);

  return apiClient.post('/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress: (progressEvent) => {
      const percentCompleted = Math.round(
        (progressEvent.loaded * 100) / progressEvent.total
      );
      // Update upload progress
    },
  });
};
```

---

## UI/UX Mobile Design

### Design System cho Mobile

#### Color Palette
```typescript
// config/theme.ts
export const colors = {
  primary: {
    50: '#eff6ff',
    500: '#3b82f6',
    600: '#2563eb',
    700: '#1d4ed8',
  },
  secondary: {
    500: '#f59e0b',
    600: '#d97706',
  },
  success: '#10b981',
  error: '#ef4444',
  warning: '#f59e0b',
  info: '#3b82f6',
  gray: {
    50: '#f9fafb',
    100: '#f3f4f6',
    200: '#e5e7eb',
    300: '#d1d5db',
    400: '#9ca3af',
    500: '#6b7280',
    600: '#4b5563',
    700: '#374151',
    800: '#1f2937',
    900: '#111827',
  },
  background: '#ffffff',
  surface: '#f8fafc',
  text: {
    primary: '#111827',
    secondary: '#6b7280',
    disabled: '#9ca3af',
  },
};
```

#### Typography Scale
```typescript
export const typography = {
  fontFamily: {
    regular: 'Inter-Regular',
    medium: 'Inter-Medium',
    semiBold: 'Inter-SemiBold',
    bold: 'Inter-Bold',
  },
  fontSize: {
    xs: 12,
    sm: 14,
    base: 16,
    lg: 18,
    xl: 20,
    '2xl': 24,
    '3xl': 30,
    '4xl': 36,
  },
  lineHeight: {
    tight: 1.25,
    normal: 1.5,
    relaxed: 1.75,
  },
};
```

#### Spacing System
```typescript
export const spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  '2xl': 24,
  '3xl': 32,
  '4xl': 40,
  '5xl': 48,
  '6xl': 64,
};
```

### Responsive Components

#### Button Component
```typescript
// components/ui/Button.tsx
interface ButtonProps {
  variant: 'primary' | 'secondary' | 'outline' | 'ghost';
  size: 'sm' | 'md' | 'lg';
  disabled?: boolean;
  loading?: boolean;
  icon?: string;
  onPress: () => void;
  children: React.ReactNode;
}

const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  disabled = false,
  loading = false,
  icon,
  onPress,
  children,
}) => {
  const buttonStyles = [
    styles.base,
    styles[variant],
    styles[size],
    disabled && styles.disabled,
  ];

  return (
    <TouchableOpacity
      style={buttonStyles}
      onPress={onPress}
      disabled={disabled || loading}
      activeOpacity={0.8}
    >
      {loading ? (
        <ActivityIndicator size="small" color={getTextColor(variant)} />
      ) : (
        <View style={styles.content}>
          {icon && (
            <Ionicons 
              name={icon} 
              size={getIconSize(size)} 
              color={getTextColor(variant)}
              style={styles.icon}
            />
          )}
          <Text style={[styles.text, styles[`${variant}Text`]]}>
            {children}
          </Text>
        </View>
      )}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  base: {
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
  },
  primary: {
    backgroundColor: colors.primary[500],
  },
  secondary: {
    backgroundColor: colors.secondary[500],
  },
  outline: {
    backgroundColor: 'transparent',
    borderWidth: 1,
    borderColor: colors.primary[500],
  },
  ghost: {
    backgroundColor: 'transparent',
  },
  sm: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    minHeight: 36,
  },
  md: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    minHeight: 44,
  },
  lg: {
    paddingHorizontal: 20,
    paddingVertical: 16,
    minHeight: 52,
  },
  disabled: {
    opacity: 0.6,
  },
  content: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  icon: {
    marginRight: 8,
  },
  text: {
    fontSize: typography.fontSize.base,
    fontFamily: typography.fontFamily.medium,
  },
  primaryText: {
    color: '#ffffff',
  },
  secondaryText: {
    color: '#ffffff',
  },
  outlineText: {
    color: colors.primary[500],
  },
  ghostText: {
    color: colors.primary[500],
  },
});
```

### Mobile-First Responsive Design

#### Screen Dimensions
```typescript
// utils/dimensions.ts
import { Dimensions, Platform } from 'react-native';

const { width, height } = Dimensions.get('window');

export const screenDimensions = {
  width,
  height,
  isSmallDevice: width < 375,
  isMediumDevice: width >= 375 && width < 414,
  isLargeDevice: width >= 414,
  isTablet: width >= 768,
};

export const getResponsiveSize = (size: number) => {
  if (screenDimensions.isSmallDevice) return size * 0.9;
  if (screenDimensions.isLargeDevice) return size * 1.1;
  return size;
};

export const isIOS = Platform.OS === 'ios';
export const isAndroid = Platform.OS === 'android';

export const getStatusBarHeight = () => {
  if (isIOS) {
    if (height >= 812) return 44; // iPhone X and newer
    return 20; // Older iPhones
  }
  return 24; // Android
};
```

### Accessibility (a11y)

```typescript
// components/ui/AccessibleButton.tsx
const AccessibleButton: React.FC<ButtonProps> = ({ 
  children, 
  onPress, 
  disabled,
  accessibilityLabel,
  accessibilityHint,
}) => {
  return (
    <TouchableOpacity
      onPress={onPress}
      disabled={disabled}
      accessible={true}
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel}
      accessibilityHint={accessibilityHint}
      accessibilityState={{ disabled }}
    >
      {children}
    </TouchableOpacity>
  );
};
```

---

## Bảo mật

### Secure Storage

```typescript
// services/StorageService.ts
import * as Keychain from 'react-native-keychain';
import AsyncStorage from '@react-native-async-storage/async-storage';

class StorageService {
  // Secure storage for sensitive data
  async setSecureItem(key: string, value: string) {
    await Keychain.setInternetCredentials(key, key, value);
  }

  async getSecureItem(key: string): Promise<string | null> {
    try {
      const credentials = await Keychain.getInternetCredentials(key);
      return credentials ? credentials.password : null;
    } catch (error) {
      return null;
    }
  }

  async removeSecureItem(key: string) {
    await Keychain.resetInternetCredentials(key);
  }

  // Regular storage for non-sensitive data
  async setItem(key: string, value: any) {
    await AsyncStorage.setItem(key, JSON.stringify(value));
  }

  async getItem(key: string) {
    const value = await AsyncStorage.getItem(key);
    return value ? JSON.parse(value) : null;
  }

  async removeItem(key: string) {
    await AsyncStorage.removeItem(key);
  }

  async clear() {
    await AsyncStorage.clear();
    await Keychain.resetGenericPassword();
  }
}

export const storageService = new StorageService();
```

### Certificate Pinning

```typescript
// api/certificatePinning.ts
import { NetworkingModule } from 'react-native';

const setupCertificatePinning = () => {
  if (Platform.OS === 'ios') {
    // iOS certificate pinning setup
    NetworkingModule.addCertificatePinner({
      hostname: 'api.fix4home.app',
      pin: 'SHA256:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=',
    });
  }
  // Android setup would be in native code
};
```

### App Integrity Check

```typescript
// security/AppIntegrityCheck.ts
import JailMonkey from 'jail-monkey';

class AppIntegrityService {
  checkDeviceIntegrity() {
    const checks = {
      isJailBroken: JailMonkey.isJailBroken(),
      canMockLocation: JailMonkey.canMockLocation(),
      isOnExternalStorage: JailMonkey.isOnExternalStorage(),
      isDebugged: JailMonkey.isDebugged(),
    };

    const hasSecurityIssues = Object.values(checks).some(check => check);
    
    if (hasSecurityIssues) {
      this.handleSecurityIssue(checks);
    }

    return !hasSecurityIssues;
  }

  private handleSecurityIssue(checks: any) {
    // Log security issue
    // Show warning to user
    // Optionally restrict app functionality
  }
}
```

---

## Performance & Optimization

### Image Optimization

```typescript
// components/ui/OptimizedImage.tsx
import FastImage from 'react-native-fast-image';

interface OptimizedImageProps {
  source: { uri: string };
  style?: any;
  resizeMode?: 'contain' | 'cover' | 'stretch' | 'center';
  placeholder?: string;
}

const OptimizedImage: React.FC<OptimizedImageProps> = ({
  source,
  style,
  resizeMode = 'cover',
  placeholder,
}) => {
  return (
    <FastImage
      source={source}
      style={style}
      resizeMode={FastImage.resizeMode[resizeMode]}
      defaultSource={placeholder ? { uri: placeholder } : undefined}
      onError={() => {
        // Handle image load error
      }}
    />
  );
};
```

### List Performance

```typescript
// components/lists/OptimizedList.tsx
const OptimizedList: React.FC<ListProps> = ({ 
  data, 
  renderItem, 
  keyExtractor 
}) => {
  const getItemLayout = useCallback((data: any, index: number) => ({
    length: ITEM_HEIGHT,
    offset: ITEM_HEIGHT * index,
    index,
  }), []);

  return (
    <FlatList
      data={data}
      renderItem={renderItem}
      keyExtractor={keyExtractor}
      getItemLayout={getItemLayout}
      removeClippedSubviews={true}
      maxToRenderPerBatch={10}
      windowSize={10}
      initialNumToRender={10}
      updateCellsBatchingPeriod={50}
    />
  );
};
```

### Memory Management

```typescript
// hooks/useMemoryManagement.ts
export const useMemoryManagement = () => {
  useEffect(() => {
    const memoryWarningListener = DeviceEventEmitter.addListener(
      'memoryWarning',
      () => {
        // Clear caches, reduce memory usage
        clearImageCache();
        clearDataCache();
      }
    );

    return () => {
      memoryWarningListener.remove();
    };
  }, []);

  const clearImageCache = () => {
    FastImage.clearMemoryCache();
    FastImage.clearDiskCache();
  };

  const clearDataCache = () => {
    // Clear React Query cache
    queryClient.clear();
  };
};
```

### Bundle Size Optimization

```javascript
// metro.config.js
module.exports = {
  transformer: {
    minifierConfig: {
      keep_classnames: true,
      keep_fnames: true,
      mangle: {
        keep_classnames: true,
        keep_fnames: true,
      },
    },
  },
  resolver: {
    alias: {
      '@': './src',
      '@components': './src/components',
      '@screens': './src/screens',
      '@utils': './src/utils',
    },
  },
};
```

---

## Testing

### Unit Testing với Jest

```typescript
// __tests__/components/Button.test.tsx
import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { Button } from '@/components/ui/Button';

describe('Button Component', () => {
  test('renders correctly', () => {
    const { getByText } = render(
      <Button onPress={() => {}}>Test Button</Button>
    );
    
    expect(getByText('Test Button')).toBeTruthy();
  });

  test('calls onPress when pressed', () => {
    const mockPress = jest.fn();
    const { getByText } = render(
      <Button onPress={mockPress}>Test Button</Button>
    );
    
    fireEvent.press(getByText('Test Button'));
    expect(mockPress).toHaveBeenCalledTimes(1);
  });

  test('shows loading state', () => {
    const { getByTestId } = render(
      <Button loading onPress={() => {}}>Test Button</Button>
    );
    
    expect(getByTestId('loading-indicator')).toBeTruthy();
  });
});
```

### Integration Testing

```typescript
// __tests__/screens/LoginScreen.test.tsx
import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import { Provider } from 'react-redux';
import { LoginScreen } from '@/screens/auth/LoginScreen';
import { store } from '@/store/store';

const renderWithProvider = (component: React.ReactElement) => {
  return render(
    <Provider store={store}>
      {component}
    </Provider>
  );
};

describe('LoginScreen', () => {
  test('successful login flow', async () => {
    const { getByPlaceholderText, getByText } = renderWithProvider(
      <LoginScreen />
    );

    fireEvent.changeText(getByPlaceholderText('Email'), 'test@example.com');
    fireEvent.changeText(getByPlaceholderText('Mật khẩu'), 'password123');
    fireEvent.press(getByText('Đăng nhập'));

    await waitFor(() => {
      // Assert navigation to dashboard or success state
    });
  });
});
```

### E2E Testing với Detox

```typescript
// e2e/loginFlow.e2e.js
describe('Login Flow', () => {
  beforeAll(async () => {
    await device.launchApp();
  });

  beforeEach(async () => {
    await device.reloadReactNative();
  });

  it('should login successfully', async () => {
    await element(by.id('email-input')).typeText('test@example.com');
    await element(by.id('password-input')).typeText('password123');
    await element(by.id('login-button')).tap();
    
    await expect(element(by.id('dashboard-screen'))).toBeVisible();
  });

  it('should show error for invalid credentials', async () => {
    await element(by.id('email-input')).typeText('invalid@example.com');
    await element(by.id('password-input')).typeText('wrongpassword');
    await element(by.id('login-button')).tap();
    
    await expect(element(by.text('Thông tin đăng nhập không đúng'))).toBeVisible();
  });
});
```

---

## Deployment

### Environment Configuration

```typescript
// config/env.ts
interface Config {
  API_BASE_URL: string;
  SOCKET_URL: string;
  GOOGLE_MAPS_API_KEY: string;
  ENVIRONMENT: 'development' | 'staging' | 'production';
  VERSION: string;
  BUILD_NUMBER: string;
}

const config: Config = {
  API_BASE_URL: __DEV__ 
    ? 'http://10.0.2.2:8100/api/v1'
    : 'https://api.fix4home.app/api/v1',
  SOCKET_URL: __DEV__
    ? 'http://10.0.2.2:8100'
    : 'https://api.fix4home.app',
  GOOGLE_MAPS_API_KEY: 'YOUR_GOOGLE_MAPS_API_KEY',
  ENVIRONMENT: __DEV__ ? 'development' : 'production',
  VERSION: '1.0.0',
  BUILD_NUMBER: '1',
};

export default config;
```

### Build Scripts

```json
{
  "scripts": {
    "android": "react-native run-android",
    "ios": "react-native run-ios",
    "start": "react-native start",
    "test": "jest",
    "lint": "eslint . --ext .js,.jsx,.ts,.tsx",
    "build:android:debug": "cd android && ./gradlew assembleDebug",
    "build:android:release": "cd android && ./gradlew assembleRelease",
    "build:ios:debug": "react-native run-ios --configuration Debug",
    "build:ios:release": "react-native run-ios --configuration Release",
    "bundle:android": "react-native bundle --platform android --dev false --entry-file index.js --bundle-output android/app/src/main/assets/index.android.bundle",
    "bundle:ios": "react-native bundle --platform ios --dev false --entry-file index.js --bundle-output ios/main.jsbundle"
  }
}
```

### Android Release Configuration

```gradle
// android/app/build.gradle
android {
    compileSdkVersion rootProject.ext.compileSdkVersion

    defaultConfig {
        applicationId "com.fix4home.mobile"
        minSdkVersion rootProject.ext.minSdkVersion
        targetSdkVersion rootProject.ext.targetSdkVersion
        versionCode 1
        versionName "1.0.0"
        multiDexEnabled true
    }

    signingConfigs {
        release {
            if (project.hasProperty('MYAPP_UPLOAD_STORE_FILE')) {
                storeFile file(MYAPP_UPLOAD_STORE_FILE)
                storePassword MYAPP_UPLOAD_STORE_PASSWORD
                keyAlias MYAPP_UPLOAD_KEY_ALIAS
                keyPassword MYAPP_UPLOAD_KEY_PASSWORD
            }
        }
    }

    buildTypes {
        debug {
            signingConfig signingConfigs.debug
        }
        release {
            signingConfig signingConfigs.release
            minifyEnabled enableProguardInReleaseBuilds
            proguardFiles getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"
        }
    }
}
```

### iOS Release Configuration

```xml
<!-- ios/Fix4HomeMobile/Info.plist -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleDisplayName</key>
    <string>Fix4Home</string>
    <key>CFBundleIdentifier</key>
    <string>com.fix4home.mobile</string>
    <key>CFBundleVersion</key>
    <string>1</string>
    <key>CFBundleShortVersionString</key>
    <string>1.0.0</string>
    
    <!-- Permissions -->
    <key>NSCameraUsageDescription</key>
    <string>Fix4Home cần truy cập camera để chụp ảnh công việc</string>
    <key>NSLocationWhenInUseUsageDescription</key>
    <string>Fix4Home cần truy cập vị trí để tìm thợ sửa chữa gần bạn</string>
    <key>NSPhotoLibraryUsageDescription</key>
    <string>Fix4Home cần truy cập thư viện ảnh để upload hình ảnh</string>
    <key>NSMicrophoneUsageDescription</key>
    <string>Fix4Home cần truy cập microphone cho tính năng gọi điện</string>
</dict>
</plist>
```

### CI/CD Pipeline

```yaml
# .github/workflows/mobile-deploy.yml
name: Mobile App Deployment

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'yarn'
      
      - run: yarn install --frozen-lockfile
      - run: yarn lint
      - run: yarn test --coverage
      - run: yarn type-check

  build-android:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'yarn'
      
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
      
      - run: yarn install --frozen-lockfile
      - name: Build Android APK
        run: |
          cd android
          ./gradlew assembleRelease
      
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-release.apk
          path: android/app/build/outputs/apk/release/

  build-ios:
    needs: test
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'yarn'
      
      - run: yarn install --frozen-lockfile
      - name: Install CocoaPods
        run: |
          cd ios
          pod install
      
      - name: Build iOS
        run: |
          xcodebuild -workspace ios/Fix4HomeMobile.xcworkspace \
                     -scheme Fix4HomeMobile \
                     -configuration Release \
                     -destination generic/platform=iOS \
                     archive -archivePath ios/Fix4HomeMobile.xcarchive

  deploy-staging:
    needs: [build-android, build-ios]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/develop'
    steps:
      - name: Deploy to Firebase App Distribution
        run: |
          # Deploy to Firebase App Distribution for testing
          echo "Deploying to staging..."

  deploy-production:
    needs: [build-android, build-ios]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Deploy to App Stores
        run: |
          # Deploy to Google Play Store and Apple App Store
          echo "Deploying to production..."
```

---

## Kết luận

Tài liệu này cung cấp roadmap chi tiết để phát triển ứng dụng mobile React Native cho hệ thống Fix4Home. Các yêu cầu đã được thiết kế để:

1. **Tối ưu cho mobile**: Tận dụng các tính năng native như camera, location, push notifications
2. **Hiệu suất cao**: Optimized cho performance trên thiết bị di động
3. **Trải nghiệm người dùng tốt**: UI/UX được thiết kế riêng cho mobile
4. **Bảo mật**: Implement các best practices bảo mật cho mobile
5. **Offline support**: Hoạt động tốt kể cả khi không có internet
6. **Cross-platform**: Chạy tốt trên cả iOS và Android

### Development Phases

#### Phase 1: Foundation (2-3 tuần)
- Setup project structure và dependencies
- Implement authentication với biometric
- Basic navigation và routing
- Core UI components

#### Phase 2: Core Features (4-5 tuần)
- Customer dashboard và service request
- Technician dashboard và job management
- Location services và maps integration
- Camera và image upload

#### Phase 3: Real-time Features (3-4 tuần)
- Chat system với file sharing
- Push notifications
- Real-time job tracking
- Socket.io integration

#### Phase 4: Advanced Features (3-4 tuần)
- Payment integration
- Offline support
- Background tasks
- Performance optimization

#### Phase 5: Polish & Deploy (2-3 tuần)
- Testing (unit, integration, E2E)
- Performance tuning
- App store preparation
- CI/CD setup

### Priority Features for Mobile

1. **High Priority**:
   - Authentication với biometric
   - Location-based services
   - Push notifications
   - Camera integration
   - Real-time chat

2. **Medium Priority**:
   - Offline support
   - Background sync
   - Deep linking
   - Performance optimization

3. **Low Priority**:
   - Advanced admin features
   - Complex analytics
   - Video calling (future enhancement)

---

**Last Updated**: January 2024  
**Version**: 1.0  
**Author**: Fix4Home Development Team  
**Platform**: React Native Mobile App
