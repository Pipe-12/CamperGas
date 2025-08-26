# CamperGas Android Application

CamperGas is an Android application built with Kotlin and Jetpack Compose for managing gas cylinders in campers/RVs. The app connects to BLE (Bluetooth Low Energy) sensors to monitor gas consumption and cylinder inclination in real-time.

**Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

## Quick Start Commands

**Essential setup (run once):**
```bash
# Verify Java 17 and setup Android SDK (see Environment Setup section)
chmod +x ./gradlew
```

**Daily development workflow:**
```bash
# Build (NEVER CANCEL - 4-6 min first time, 15-30s incremental)
./gradlew assembleDebug

# Test (NEVER CANCEL - 30-60s)  
./gradlew testDebugUnitTest

# Lint (NEVER CANCEL - 1 min)
./gradlew lintDebug
```

**Always use timeouts of 10+ minutes for builds, 5+ minutes for tests.**

## Critical Environment Setup Requirements

### Android SDK Installation (MANDATORY)
**You MUST install Android SDK before any Gradle commands will work:**

```bash
# Download and setup Android Command Line Tools
cd /tmp
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip commandlinetools-linux-9477386_latest.zip
sudo mkdir -p /usr/local/lib/android/sdk/cmdline-tools
sudo mv cmdline-tools /usr/local/lib/android/sdk/cmdline-tools/latest
sudo chown -R $USER:$USER /usr/local/lib/android/sdk

# Set environment variables (add to ~/.bashrc for persistence)
export ANDROID_HOME=/usr/local/lib/android/sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Install required SDK components
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
```

**If Android SDK installation fails, ALL build commands will fail with plugin resolution errors.**

## Working Effectively

### Prerequisites Check
Always verify your environment setup:
```bash
java -version  # Must be Java 17
echo $ANDROID_HOME  # Must point to Android SDK
echo $PATH | grep android  # Must include Android tools
```

### Build Commands (VALIDATED - Based on CI)
```bash
# Bootstrap - Make gradlew executable
chmod +x ./gradlew

# Build debug APK - takes approximately 4-6 minutes on first run
./gradlew assembleDebug
# NEVER CANCEL: Build takes 4-6 minutes. Set timeout to 10+ minutes.

# Run unit tests - takes approximately 30-60 seconds  
./gradlew testDebugUnitTest
# NEVER CANCEL: Tests take 30-60 seconds. Set timeout to 5+ minutes.

# Run lint checks - takes approximately 1 minute
./gradlew lintDebug
# NEVER CANCEL: Lint takes 1 minute. Set timeout to 5+ minutes.

# Full build (includes all above) - takes approximately 2-3 minutes
./gradlew build
# NEVER CANCEL: Full build takes 2-3 minutes. Set timeout to 10+ minutes.

# Clean build when needed
./gradlew clean
```

### Testing Commands
```bash
# Run all unit tests (196 tests across all test classes)
./gradlew testDebugUnitTest

# Run specific test class (CORRECT SYNTAX)
./gradlew testDebugUnitTest --tests "com.example.campergas.ui.screens.settings.SettingsViewModelTest"

# Run test suite with all tests
./gradlew testDebugUnitTest --tests "com.example.campergas.CamperGasTestSuite"

# Available test classes include:
# - All ViewModel tests (BleConnect, Consumption, Inclination, Settings, Home, Weight, CaravanConfig)
# - Domain model tests (GasCylinder, FuelMeasurement) 
# - Use case tests (AddGasCylinder, GetActiveCylinder, SaveFuelMeasurement)
# - UI component tests (GasCylinderViewModel)

# Run instrumented tests (requires Android emulator or device)
./gradlew connectedAndroidTest
```

### Test Coverage Status
**Current test implementation:**
- ✅ ViewModel tests for all screens (BLE, Consumption, Inclination, Settings, Home, Weight, CaravanConfig)
- ✅ Domain model tests (GasCylinderTest, FuelMeasurementTest)
- ✅ Use case tests (AddGasCylinderUseCaseTest, GetActiveCylinderUseCaseTest, SaveFuelMeasurementUseCaseTest)
- ✅ UI component tests (GasCylinderViewModelTest)
- ✅ Basic instrumented test (ExampleInstrumentedTest)
- **Total: 196 tests running successfully**

**When adding new features, follow the established test patterns for comprehensive coverage.**

## Application Architecture

### Technology Stack
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose (BOM 2025.08.00)
- **Architecture**: Clean Architecture (Domain/Data/Presentation layers)
- **Dependency Injection**: Hilt 2.56.2
- **Database**: Room 2.7.2 with SQLite
- **Bluetooth**: Nordic BLE library 2.10.2
- **Testing**: JUnit 4, MockK, Coroutines Test, Robolectric
- **Build**: Gradle 8.11.1, Android Gradle Plugin 8.10.1

### Key Project Structure
```
app/src/main/java/com/example/campergas/
├── CamperGasApplication.kt          # Hilt application class
├── MainActivity.kt                  # Main Compose activity
├── data/                           # Data layer
│   ├── ble/                        # BLE sensor communication
│   ├── local/                      # Room database, preferences
│   └── repository/                 # Repository implementations
├── domain/                         # Business logic layer
│   ├── model/                      # Domain models
│   └── usecase/                    # Use cases
├── ui/                            # Presentation layer
│   ├── components/                 # Reusable UI components
│   ├── navigation/                 # Navigation setup
│   ├── screens/                    # Screen implementations
│   │   ├── bleconnect/            # BLE device connection
│   │   ├── consumption/           # Gas consumption tracking
│   │   ├── inclination/           # Cylinder inclination monitoring
│   │   ├── settings/              # App settings
│   │   └── weight/                # Weight measurements
│   └── theme/                     # Compose theming
└── utils/                         # Utility classes
```

### Key Features
- **Gas Cylinder Management**: Add, configure, and monitor multiple gas cylinders
- **BLE Sensor Integration**: Real-time connection to weight and inclination sensors  
- **Consumption Tracking**: Monitor gas usage over time with historical data
- **Vehicle Configuration**: Configure caravan/RV geometry for accurate calculations
- **Settings Management**: Theme selection, notification preferences, sensor intervals

## Validation Requirements

### Manual Validation After Changes
**Always perform these validation steps after making changes:**

1. **Build Validation**: Always run `./gradlew assembleDebug` to ensure the app compiles
2. **Test Validation**: Always run `./gradlew testDebugUnitTest` to ensure tests pass
3. **Lint Validation**: Always run `./gradlew lintDebug` to check code quality
4. **UI Testing**: If changing UI components, manually verify Compose previews compile

### Application Testing Scenarios
**For functional changes, test these core workflows:**

1. **BLE Connection Flow**: 
   - Navigate to BLE Connect screen
   - Verify permission dialogs display correctly
   - Test device scanning UI (will show empty if no BLE devices available)

2. **Gas Cylinder Management**:
   - Add new cylinder with name, tare weight, capacity
   - Set cylinder as active
   - Verify data persistence between app restarts

3. **Settings Management**:
   - Change theme (Light/Dark/System)
   - Modify sensor reading intervals
   - Toggle notification preferences

**Note**: Full BLE functionality testing requires physical BLE hardware and cannot be fully validated in development environment.

## Frequently Visited Files

### Critical Configuration Files
- `gradle/libs.versions.toml` - Dependency versions (update carefully)
- `app/build.gradle.kts` - Main build configuration
- `app/src/main/AndroidManifest.xml` - App permissions and configuration

### Core Application Files  
- `CamperGasApplication.kt` - Hilt application entry point
- `MainActivity.kt` - Main Compose activity with navigation setup
- `ui/navigation/NavGraph.kt` - Navigation configuration
- `ui/theme/` - Compose theming and colors

### Key Business Logic
- `domain/model/GasCylinder.kt` - Core gas cylinder model with calculations
- `domain/model/CamperGasUuids.kt` - BLE hardware compatibility definitions
- `data/repository/BleRepository.kt` - BLE sensor communication interface
- `data/repository/GasCylinderRepository.kt` - Gas cylinder data management

### Important ViewModels
- `ui/screens/settings/SettingsViewModel.kt` - App settings management
- `ui/screens/bleconnect/BleConnectViewModel.kt` - BLE device connection
- `ui/screens/consumption/ConsumptionViewModel.kt` - Gas consumption tracking

### Testing Entry Points
- `CamperGasTestSuite.kt` - Main test suite (many tests commented out)
- `ui/screens/settings/SettingsViewModelTest.kt` - Example of working ViewModel test


### Adding New Features
1. **Domain First**: Create models and use cases in `domain/` package
2. **Data Layer**: Implement repository interfaces in `data/` package  
3. **UI Layer**: Create screens and view models in `ui/` package
4. **Testing**: Add unit tests for business logic in `src/test/`
5. **Integration**: Wire up with Hilt dependency injection

### BLE Development
- **Core BLE Implementation**: `data/ble/` package contains all BLE logic
- **Main Service**: `CamperGasBleService` handles weight and inclination sensor communication
- **Device Scanning**: `BleDeviceScanner` manages device discovery
- **Connection Management**: `BleManager` handles Bluetooth state and permissions
- **Repository Pattern**: `BleRepository` provides clean interface for UI layer
- **Permissions**: `BluetoothPermissionManager` handles complex Android BLE permissions
- **Sensor UUIDs**: Defined in `CamperGasUuids.kt` for specific hardware compatibility

**BLE Testing Limitations**: Full BLE functionality requires physical hardware with specific UUIDs. UI and permission flows can be tested without hardware.

### Database Changes
- Room entities in `data/local/` packages
- Always create migration scripts for schema changes
- Test migrations with `@Database` annotation updates

### UI Development
- Use Compose previews for rapid UI development
- Follow Material 3 design system
- Implement proper state management with ViewModels
- Handle configuration changes appropriately

## Build Times and Expectations

### Typical Build Times (First Run)
- **Clean Build**: 2-3 minutes (includes dependency download)
- **Incremental Build**: 15-30 seconds
- **Unit Tests**: 30-60 seconds  
- **Lint Check**: 1 minute
- **Full CI Pipeline**: 5-8 minutes

### Subsequent Builds (Gradle Daemon Active)
- **Incremental Build**: 10-20 seconds
- **Unit Tests**: 20-40 seconds
- **Lint Check**: 30-45 seconds

**CRITICAL**: Always set timeouts of 10+ minutes for full builds and 5+ minutes for test runs to prevent premature cancellation.

## Troubleshooting

### Build Failures
- **Plugin Resolution Errors**: Ensure Android SDK is properly installed
- **Memory Issues**: Increase Gradle heap size: `./gradlew -Xmx4g build`
- **Permission Errors**: Ensure `gradlew` has execute permissions: `chmod +x gradlew`

### Test Failures  
- **MockK Issues**: Clear MockK state between tests with `clearAllMocks()`
- **Coroutine Tests**: Use `UnconfinedTestDispatcher` for immediate execution
- **Room Tests**: Use in-memory database for fast, isolated tests

### BLE Development
- **Permissions**: BLE requires location and Bluetooth permissions
- **Android Version**: Different permission models for Android 12+ vs older versions
- **Hardware**: Physical device or emulator with BLE support required for full testing

## CI/CD Integration

The project uses GitHub Actions (`.github/workflows/android-ci.yml`) with these validated steps:
1. **Setup**: JDK 17 (Temurin distribution)
2. **Permissions**: `chmod +x gradlew`
3. **Build**: `./gradlew assembleDebug`
4. **Test**: `./gradlew testDebugUnitTest`  
5. **Lint**: `./gradlew lintDebug`
6. **Artifacts**: Upload APK and reports

## Comprehensive Validation Checklist

### Before Making Any Changes
1. **Environment Verification**:
   ```bash
   java -version  # Verify Java 17
   echo $ANDROID_HOME  # Verify Android SDK path
   ./gradlew --version  # Verify Gradle 8.11.1
   ```

2. **Project Health Check**:
   ```bash
   ./gradlew assembleDebug  # Verify project builds (NEVER CANCEL - 4-6 min)
   ./gradlew testDebugUnitTest  # Verify tests pass (NEVER CANCEL - 30-60s)
   ./gradlew lintDebug  # Verify code quality (NEVER CANCEL - 1 min)
   ```

### After Making Changes
1. **Build Validation**:
   ```bash
   ./gradlew clean assembleDebug  # Clean build (NEVER CANCEL - 2-3 min)
   ```

2. **Test Validation**:
   ```bash
   ./gradlew testDebugUnitTest  # All unit tests (196 tests)
   ./gradlew testDebugUnitTest --tests "*ViewModel*"  # Focus on ViewModel tests
   ```

3. **Code Quality**:
   ```bash
   ./gradlew lintDebug  # Android lint checks
   ```

4. **Dependency Verification**:
   ```bash
   ./gradlew app:dependencies --configuration debugRuntimeClasspath  # Check dependency tree
   ```

### Manual Feature Testing
**After UI changes, verify these user scenarios:**

1. **App Launch**: App starts without crashes, permissions dialog appears
2. **Navigation**: All bottom navigation tabs work (Home, BLE, Consumption, etc.)
3. **Settings**: Theme changes apply immediately, preferences persist
4. **Gas Cylinder**: Can add new cylinders, set active cylinder, data persists
5. **BLE Screen**: Permission requests work, scanning UI displays correctly

### Performance Verification
- **Memory Usage**: Monitor for memory leaks in long-running operations
- **Battery**: BLE operations should handle background/foreground transitions
- **UI Responsiveness**: Compose UIs should render without stuttering

**CRITICAL REMINDER**: Always allow builds and tests to complete fully. This project's build process is reliable but takes time. Canceling prematurely will lead to incomplete validation.