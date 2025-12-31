# Digia UI Compose

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/digia/digia-ui-compose)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

**Pure Android Library Module** - Server-driven UI SDK for Jetpack Compose.

## 📦 Library Module

This repository contains only the `digia-ui` library module for integration into your Android projects.

### Building the Library

**Build the library:**
```bash
./gradlew :digia-ui:build
```

**Publish locally:**
```bash
./gradlew :digia-ui:publishToMavenLocal
```

**Use in your app:**
```kotlin
dependencies {
    implementation("com.digia:digia-ui:1.0.0")
}
```

## 🚀 Quick Setup

### 1. Add to Your Project
```kotlin
// In your app's settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenLocal()  // If published locally
        google()
        mavenCentral()
    }
}

// OR for active development
includeBuild("/path/to/digia_ui_compose")
```

### 2. Add Dependency
```kotlin
dependencies {
    implementation("com.digia:digia-ui:1.0.0")
}
```

### 3. Initialize SDK
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        lifecycleScope.launch {
            val digiaUI = DigiaUI.initialize(
                DigiaUIOptions(
                    context = applicationContext,
                    flavor = Flavor.DASHBOARD,
                    accessKey = "your_access_key",
                    developerConfig = DeveloperConfig(
                        baseUrl = "https://app.digia.tech/api/v1"
                    )
                )
            )
            
            DigiaUIManager.initialize(digiaUI)
        }
    }
}
```

### 4. Use SDK Features
```kotlin
// Access anywhere in your app
val manager = DigiaUIManager.getInstance()
val config = manager.config
val networkClient = manager.networkClient
val inspector = manager.inspector
```

## 📁 Project Structure

```
digia_ui_compose/
├── digia-ui/                           # 📚 Android Library Module
│   ├── src/main/java/com/digia/digiaui/
│   │   ├── init/                       # SDK Initialization
│   │   │   ├── DigiaUI.kt             # Main SDK class
│   │   │   ├── DigiaUIManager.kt      # Singleton manager
│   │   │   ├── DigiaUIOptions.kt      # Configuration
│   │   │   ├── Flavor.kt              # Environment flavors
│   │   │   └── Environment.kt         # Environment config
│   │   ├── config/                     # Configuration System
│   │   │   ├── ConfigResolver.kt      # Config loading
│   │   │   ├── ConfigProvider.kt      # Provider interface
│   │   │   ├── DUIConfig.kt           # Config model
│   │   │   └── source/                # Config sources
│   │   ├── network/                    # HTTP Client
│   │   │   ├── NetworkClient.kt       # OkHttp client
│   │   │   ├── types.kt               # Network types
│   │   │   └── Response.kt            # Response models
│   │   ├── framework/                  # Framework Core
│   │   │   ├── DUIFactory.kt          # Widget factory
│   │   │   ├── actions/               # Action system
│   │   │   ├── widgets/               # Widget system
│   │   │   ├── page/                  # Page management
│   │   │   └── ...                    # More framework code
│   │   ├── utils/                      # Utilities
│   │   │   ├── FileOperations.kt      # File ops
│   │   │   ├── DownloadOperations.kt  # Downloads
│   │   │   ├── Logger.kt              # Logging
│   │   │   └── ...                    # More utilities
│   │   └── version/
│   │       └── DigiaUIVersion.kt      # SDK version
│   ├── build.gradle.kts               # Library build config
│   └── src/main/AndroidManifest.xml   # Library manifest
│
├── gradle/                            # Gradle wrapper & config
├── build.gradle.kts                   # Root build file
├── settings.gradle.kts                # Module definitions (only :digia-ui)
├── README.md                          # This file
└── CHANGELOG.md                       # Version history
```

## 🛠️ Development

### Prerequisites
- Android Studio Hedgehog or later
- Kotlin 1.8.10+
- JDK 17
- Android API 24+ (minSdk)
- Android API 34+ (compileSdk)

### Building
```bash
# Clean build
./gradlew clean build

# Build library only
./gradlew :digia-ui:assembleDebug
./gradlew :digia-ui:assembleRelease

# Run tests
./gradlew :digia-ui:test

# Check for errors
./gradlew :digia-ui:lint
```

### Publishing
```bash
# Publish to Maven Local (for testing)
./gradlew :digia-ui:publishToMavenLocal

# Published to: ~/.m2/repository/com/digia/digia-ui/1.0.0/

# Verify publication
ls -la ~/.m2/repository/com/digia/digia-ui/1.0.0/
```

## 🔧 API Reference

### Core Classes

#### `DigiaUI`
Main SDK entry point for initialization.

```kotlin
suspend fun DigiaUI.initialize(options: DigiaUIOptions): DigiaUI
```

#### `DigiaUIManager`
Singleton for global SDK access.

```kotlin
val manager = DigiaUIManager.getInstance()
manager.accessKey: String
manager.config: DUIConfig
manager.networkClient: NetworkClient
manager.inspector: DigiaInspector?
manager.environmentVariables: Map<String, Any>
```

#### `DigiaUIOptions`
Configuration for SDK initialization.

```kotlin
DigiaUIOptions(
    context: Context,
    flavor: Flavor,
    accessKey: String,
    developerConfig: DeveloperConfig? = null,
    networkConfiguration: NetworkConfiguration? = null
)
```

#### `Flavor`
Environment flavors for different build configurations.

```kotlin
Flavor.DASHBOARD              // Production dashboard
Flavor.ASSET                  // Bundled assets
Flavor.ASSET_WITH_UPDATE     // Assets with background update
Flavor.NETWORK_FILE          // Network with file download
```

### Key Features
- ✅ **Server-Driven UI** - Dynamic UI from backend configuration
- ✅ **Network-First Loading** - Latest config from CDN (<100ms)
- ✅ **Fallback Sources** - Asset/cached fallbacks for offline
- ✅ **Multi-Environment** - Debug/Staging/Production support
- ✅ **Developer Tools** - Inspector, proxy, logging
- ✅ **Coroutines** - Full Kotlin Coroutines support
- ✅ **Type-Safe** - Kotlin type system throughout

## 📚 Documentation

- **[CHANGELOG.md](CHANGELOG.md)** - Version history

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing`)
3. Make your changes
4. Add tests if applicable
5. Commit (`git commit -m 'Add amazing feature'`)
6. Push (`git push origin feature/amazing`)
7. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For issues and questions:
- **GitHub Issues**: [Create an issue](https://github.com/digia/digia-ui-compose/issues)
- **Email**: support@digia.tech
- **Documentation**: [https://docs.digia.tech](https://docs.digia.tech)

