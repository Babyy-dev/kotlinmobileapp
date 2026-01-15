# Kappa Android App - Phase 1

## Overview

This is Phase 1 of the Kappa Android application - a foundational implementation focused on establishing a scalable architecture and technical infrastructure. **This phase establishes the complete system structure** with feature skeletons, mock data flows, and technical demonstration, ready for Phase 2 feature development.

## Architecture

The project follows **Clean Architecture** principles with **MVVM (Model-View-ViewModel)** pattern.

### Architecture Layers

1. **Presentation Layer** (`presentation`)
   - Activities, Fragments, ViewModels
   - UI components and user interactions

2. **Domain Layer** (`domain`)
   - Business logic entities
   - Use cases (to be added in future phases)
   - Domain models

3. **Data Layer** (`data`)
   - Repository implementations
   - API services
   - Local storage (DataStore)

### Package Structure

```
com.kappa.app
 ├─ core
 │   ├─ di              # Dependency Injection modules
 │   ├─ network         # Retrofit, OkHttp, API interfaces
 │   ├─ storage         # DataStore, PreferencesManager
 │   ├─ config          # AppConfig, Environment configuration
 │   ├─ utils           # Utility classes
 │   └─ base            # BaseActivity, BaseFragment, ViewState pattern
 ├─ auth
 │   ├─ presentation    # AuthViewModel, AuthViewState
 │   ├─ domain          # AuthRepository interface, LoginUseCase
 │   └─ data            # FakeAuthRepository (mock implementation)
 ├─ user
 │   ├─ presentation    # UserViewModel, UserViewState
 │   ├─ domain          # UserRepository interface, GetUserUseCase
 │   └─ data            # FakeUserRepository (mock implementation)
 ├─ economy
 │   ├─ presentation    # EconomyViewModel, EconomyViewState
 │   ├─ domain          # EconomyRepository interface, GetCoinBalanceUseCase
 │   └─ data            # FakeEconomyRepository (mock implementation)
 ├─ audio
 │   ├─ presentation    # AudioViewModel, AudioViewState
 │   ├─ domain          # AudioRepository interface, GetAudioRoomsUseCase
 │   └─ data            # FakeAudioRepository (placeholder)
 ├─ domain              # Shared domain models (User, CoinBalance, etc.)
 └─ main                # MainActivity, SplashFragment, LoginFragment, HomeFragment
```

## Technical Stack

### Core Technologies
- **Language**: Kotlin
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36
- **Architecture**: Clean Architecture + MVVM

### Dependencies

#### Dependency Injection
- **Hilt** (v2.51.1) - Dependency injection framework

#### Networking
- **Retrofit** (v2.11.0) - HTTP client
- **OkHttp** (v4.12.0) - HTTP client with logging interceptor
- **Gson** - JSON serialization/deserialization

#### Storage
- **DataStore Preferences** (v1.1.1) - Modern data storage solution

#### UI & Navigation
- **Navigation Component** (v2.8.4) - Navigation between screens
- **Material Design** (v1.7.0) - Material Design components
- **Lifecycle Components** - ViewModel, LiveData

#### Coroutines
- **Kotlin Coroutines** (v1.9.0) - Asynchronous programming

#### Logging
- **Timber** (v5.0.1) - Logging utility

## Project Setup

### Prerequisites
- Android Studio latest stable version
- JDK 11 or higher
- Android SDK with API 24+

### Build Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run the app

The app will launch on an emulator or connected device running Android 7.0 (API 24) or higher.

### Build Variants

- **debug**: Development build with logging enabled
- **release**: Production build with ProGuard/R8 enabled (rules to be added)

## Key Components

### 1. Dependency Injection (Hilt)

The app uses Hilt for dependency injection. DI modules:
- `core/di/AppModule.kt` - Provides app-level singletons (Retrofit, ApiService)
- `core/di/RepositoryModule.kt` - Binds repository interfaces to fake implementations (Phase 1)

**Repository Bindings (Phase 1):**
- `AuthRepository` → `FakeAuthRepository`
- `UserRepository` → `FakeUserRepository`
- `EconomyRepository` → `FakeEconomyRepository`
- `AudioRepository` → `FakeAudioRepository`

In Phase 2, fake repositories will be replaced with real implementations.

### 2. Network Layer

**Location**: `core/network/`

- `ApiService.kt` - Base API interface (placeholder endpoints)
- `ApiResult.kt` - Sealed class for API results (Success/Error/Loading)
- `BaseApiResponse.kt` - Base response wrapper
- `NetworkError.kt` - Network error types
- `ErrorMapper.kt` - Centralized error mapping to UI-safe messages
- `NetworkModule.kt` - Network configuration (OkHttp, Retrofit setup)

**Environment Configuration**:
- Configured via `core/config/AppConfig.kt`
- Dev: `https://api-dev.kappa.app/`
- Prod: `https://api.kappa.app/`
- Automatically switches based on build variant

### 3. Local Storage

**Location**: `core/storage/`

- `PreferencesManager.kt` - DataStore wrapper for:
  - Session token storage
  - User token storage
  - Token clearing operations

### 4. Base Classes

**Location**: `core/base/`

- `BaseActivity.kt` - Base activity with ViewState pattern
- `BaseFragment.kt` - Base fragment with ViewState pattern
- `ViewState` - Interface for view state management

### 5. Domain Models

**Location**: `domain/`

- `user/User.kt` - User model with Role and Permission enums
- `economy/CoinBalance.kt` - CoinBalance, Transaction, RewardRequest models
- `audio/AudioRoom.kt` - AudioRoom and AudioService interfaces (placeholders)

### 6. Navigation

**Navigation Graph**: `res/navigation/mobile_navigation.xml`

**Screens**:
- Splash (`SplashFragment`) - Simple placeholder
- Login (`LoginFragment`) - Empty screen (authentication coming in Phase 2)
- Home (`HomeFragment`) - **Technical dashboard showing:**
  - App version, build type, environment
  - User role and coin balance (from mock data)
  - Future feature placeholders (Audio Rooms, Economy, Admin Tools)

### 7. Design System

**Theme**: Material Design with custom colors
- Light mode colors in `res/values/colors.xml`
- Dark mode colors in `res/values-night/colors.xml`
- Typography system in `res/values/typography.xml`
- Shape system in `res/values/shapes.xml`

## Feature Skeletons (Phase 1)

All core domains have complete skeletons ready for Phase 2:

### Auth Module
- ✅ `AuthViewModel` - ViewModel with ViewState pattern
- ✅ `LoginUseCase` - UseCase placeholder
- ✅ `AuthRepository` interface - Defined contract
- ✅ `FakeAuthRepository` - Mock implementation returning fake user

### User Module
- ✅ `UserViewModel` - ViewModel with ViewState pattern
- ✅ `GetUserUseCase` - UseCase placeholder
- ✅ `UserRepository` interface - Defined contract
- ✅ `FakeUserRepository` - Mock implementation with demo users

### Economy Module
- ✅ `EconomyViewModel` - ViewModel with ViewState pattern
- ✅ `GetCoinBalanceUseCase` - UseCase placeholder
- ✅ `EconomyRepository` interface - Defined contract
- ✅ `FakeEconomyRepository` - Mock implementation returning 1250 coins

### Audio Module
- ✅ `AudioViewModel` - ViewModel with ViewState pattern
- ✅ `GetAudioRoomsUseCase` - UseCase placeholder
- ✅ `AudioRepository` interface - Defined contract
- ✅ `FakeAudioRepository` - Placeholder implementation

**All ViewModels are injectable via Hilt and ready to use.**

## How Next Phases Plug In

### Phase 2: Authentication & Real Data
- Replace `FakeAuthRepository` with real API implementation
- Implement `LoginUseCase` with real authentication logic
- Connect to backend authentication endpoints
- Add real login/logout UI

### Phase 3: Features
- Replace `FakeUserRepository` with real implementation
- Replace `FakeEconomyRepository` with real transactions
- Replace `FakeAudioRepository` with SDK integration
- All ViewModels already work - just swap repositories!

### Adding New Features

1. Create feature package structure:
   ```
   featurename/
     ├─ presentation/  # UI layer
     ├─ domain/        # Business logic
     └─ data/          # Data sources
   ```

2. Create feature module in `core/di/` (when needed)

3. Add navigation destinations in `mobile_navigation.xml`

4. Implement repository pattern:
   - Interface in `domain/`
   - Implementation in `data/`

## Explicit Exclusions (Phase 1)

The following are **intentionally NOT implemented** in Phase 1:

- ❌ Payment processing
- ❌ Audio SDK integration
- ❌ Minigames
- ❌ Admin UI
- ❌ Real backend connections
- ❌ Complete business logic
- ❌ UI polish/styling
- ❌ Comprehensive error handling (only foundation)
- ❌ Unit tests (to be added in future phases)
- ❌ Integration tests

These will be implemented in subsequent phases.

## Code Quality Standards

- No TODO comments in production code
- No commented-out dead code
- Meaningful naming conventions
- Consistent code formatting
- No shortcuts or quick hacks

## Success Criteria

Phase 1 is considered successful if:

✅ Any senior Android developer can onboard within 1 day  
✅ No technical debt exists  
✅ Future features slot naturally into the architecture  
✅ The project builds without warnings  
✅ The app runs on Android 7.0+ devices  

## Troubleshooting

### Build Issues
- Ensure JDK 11+ is configured
- Clean and rebuild the project
- Invalidate caches: File → Invalidate Caches / Restart

### Runtime Issues
- Check logcat for Timber logs (only in debug builds)
- Verify device/emulator is running API 24+

## Contact & Support

For questions about the architecture or implementation, refer to this README or contact the development team.

---

**Phase 1 Status**: ✅ Complete  
**Last Updated**: Phase 1 implementation  
**Next Phase**: Authentication & User Features
