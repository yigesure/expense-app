# 密码管理器应用部署指南

## 🚀 构建和发布流程

### 1. 环境准备

#### 开发环境要求
- **Android Studio**: Arctic Fox 2020.3.1 或更高版本
- **JDK**: OpenJDK 11 或更高版本
- **Kotlin**: 1.9.0 或更高版本
- **Gradle**: 8.0 或更高版本
- **Android SDK**: API Level 24 (Android 7.0) 最低支持

#### 签名配置
```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/password_manager.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### 2. 构建配置

#### Proguard规则 (proguard-rules.pro)
```proguard
# 保持密码管理器核心类
-keep class com.passwordmanager.elite.data.model.** { *; }
-keep class com.passwordmanager.elite.security.** { *; }

# SQLDelight
-keep class com.squareup.sqldelight.** { *; }
-keep class **.*Queries { *; }

# Koin
-keep class org.koin.** { *; }
-keep class kotlin.Metadata { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# 生物识别
-keep class androidx.biometric.** { *; }

# 加密库
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
```

### 3. 版本管理

#### 版本号策略
```kotlin
// build.gradle.kts
android {
    defaultConfig {
        versionCode = 1
        versionName = "1.0.0"
        
        // 版本命名规则: MAJOR.MINOR.PATCH
        // MAJOR: 重大功能更新或架构变更
        // MINOR: 新功能添加
        // PATCH: Bug修复和小改进
    }
}
```

#### Git标签管理
```bash
# 创建版本标签
git tag -a v1.0.0 -m "首个正式版本发布"
git push origin v1.0.0

# 查看所有标签
git tag -l
```

## 📦 打包流程

### 1. 发布构建
```bash
# 清理项目
./gradlew clean

# 构建发布版本
./gradlew assembleRelease

# 生成Bundle (推荐用于Google Play)
./gradlew bundleRelease
```

### 2. 构建验证
```bash
# 运行所有测试
./gradlew test

# 运行Lint检查
./gradlew lint

# 检查依赖漏洞
./gradlew dependencyCheckAnalyze
```

### 3. APK分析
```bash
# 使用Android Studio的APK Analyzer
# 或使用命令行工具
aapt dump badging app-release.apk
```

## 🔐 安全配置

### 1. 密钥管理
```bash
# 生成发布密钥
keytool -genkey -v -keystore password_manager.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias password_manager_key
```

### 2. 环境变量配置
```bash
# ~/.bashrc 或 ~/.zshrc
export KEYSTORE_PASSWORD="your_keystore_password"
export KEY_ALIAS="password_manager_key"
export KEY_PASSWORD="your_key_password"
```

### 3. 代码混淆验证
```bash
# 检查混淆后的代码
./gradlew assembleRelease
# 查看 app/build/outputs/mapping/release/mapping.txt
```

## 🏪 应用商店发布

### 1. Google Play Console

#### 应用信息配置
- **应用名称**: 密码管家 Elite
- **简短描述**: 安全可靠的密码管理器
- **完整描述**: 
```
密码管家 Elite 是一款专业的密码管理应用，采用军用级AES-256加密技术，为您的数字生活提供全方位安全保护。

🔐 核心功能：
• 生物识别解锁 - 指纹/面部识别快速访问
• 智能密码生成 - 创建强密码，告别弱密码
• 安全分析 - 实时评估密码安全性
• 跨设备同步 - 端到端加密，数据安全同步
• 玻璃拟态设计 - 现代化UI，操作流畅

🛡️ 安全特性：
• 本地加密存储，数据不上云
• 零知识架构，连我们也无法访问您的密码
• 定期安全审计，确保代码安全
• 开源透明，接受社区监督

立即下载，让密码管理变得简单安全！
```

#### 应用分类和标签
- **类别**: 工具
- **标签**: 密码管理, 安全, 加密, 生物识别
- **内容分级**: 3岁以上

#### 隐私政策
```
隐私政策要点：
1. 数据收集：仅收集必要的功能数据
2. 数据存储：本地加密存储，不上传服务器
3. 数据共享：不与第三方共享用户数据
4. 用户权利：用户完全控制自己的数据
```

### 2. 华为应用市场

#### 应用信息适配
- 遵循华为应用审核规范
- 适配HMS Core服务
- 支持华为设备特性

### 3. 其他应用商店
- **小米应用商店**: 适配MIUI特性
- **OPPO软件商店**: ColorOS适配
- **vivo应用商店**: FuntouchOS适配

## 🧪 测试和质量保证

### 1. 自动化测试
```kotlin
// CI/CD配置 (.github/workflows/android.yml)
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Run lint
      run: ./gradlew lint
```

### 2. 设备兼容性测试
```bash
# 测试设备列表
- Samsung Galaxy S21 (Android 11)
- Google Pixel 6 (Android 12)
- Xiaomi Mi 11 (MIUI 12)
- Huawei P40 (EMUI 10)
- OnePlus 9 (OxygenOS 11)
```

### 3. 性能基准测试
```kotlin
// 性能测试配置
@RunWith(AndroidJUnit4::class)
class PerformanceBenchmark {
    
    @get:Rule
    val benchmarkRule = BenchmarkRule()
    
    @Test
    fun benchmarkPasswordDecryption() {
        benchmarkRule.measureRepeated {
            // 测试密码解密性能
            cryptoManager.decrypt(encryptedPassword)
        }
    }
}
```

## 📊 监控和分析

### 1. 崩溃监控
```kotlin
// Firebase Crashlytics集成
dependencies {
    implementation 'com.google.firebase:firebase-crashlytics-ktx'
    implementation 'com.google.firebase:firebase-analytics-ktx'
}

// 自定义崩溃报告
FirebaseCrashlytics.getInstance().apply {
    setUserId(userId)
    setCustomKey("feature", "password_management")
    recordException(exception)
}
```

### 2. 性能监控
```kotlin
// 性能指标收集
class PerformanceMonitor {
    fun trackStartupTime() {
        val startTime = System.currentTimeMillis()
        // ... 应用启动逻辑
        val duration = System.currentTimeMillis() - startTime
        
        FirebasePerformance.getInstance()
            .newTrace("app_startup")
            .apply {
                putMetric("duration_ms", duration)
                start()
                stop()
            }
    }
}
```

### 3. 用户行为分析
```kotlin
// 用户行为追踪
FirebaseAnalytics.getInstance(context).logEvent("password_created") {
    param("method", "manual")
    param("strength", "strong")
}
```

## 🔄 持续集成/持续部署

### 1. GitHub Actions配置
```yaml
# .github/workflows/release.yml
name: Release Build

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
    
    - name: Setup JDK
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Build Release APK
      run: ./gradlew assembleRelease
      env:
        KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
        KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: release-apk
        path: app/build/outputs/apk/release/
```

### 2. 自动化发布
```bash
# 发布脚本 (scripts/release.sh)
#!/bin/bash

VERSION=$1
if [ -z "$VERSION" ]; then
    echo "请提供版本号: ./release.sh v1.0.0"
    exit 1
fi

# 更新版本号
./scripts/update_version.sh $VERSION

# 构建发布版本
./gradlew clean assembleRelease bundleRelease

# 创建Git标签
git tag -a $VERSION -m "Release $VERSION"
git push origin $VERSION

echo "发布 $VERSION 完成！"
```

## 📋 发布检查清单

### 发布前检查
- [ ] 所有测试通过
- [ ] Lint检查无错误
- [ ] 性能测试达标
- [ ] 安全审计完成
- [ ] 版本号更新
- [ ] 更新日志编写
- [ ] 签名配置正确
- [ ] 混淆配置验证
- [ ] 权限声明检查
- [ ] 隐私政策更新

### 发布后监控
- [ ] 崩溃率监控
- [ ] 性能指标监控
- [ ] 用户反馈收集
- [ ] 应用商店评分
- [ ] 下载量统计
- [ ] 用户留存分析

## 🎯 发布里程碑

| 版本 | 功能 | 发布日期 | 状态 |
|------|------|----------|------|
| v1.0.0 | 基础密码管理 | 2024-01-15 | ✅ 已发布 |
| v1.1.0 | 生物识别认证 | 2024-02-01 | 🚀 准备中 |
| v1.2.0 | 数据同步功能 | 2024-02-15 | 📋 计划中 |
| v1.3.0 | 安全分析增强 | 2024-03-01 | 📋 计划中 |
| v2.0.0 | 跨平台支持 | 2024-04-01 | 💭 构思中 |

通过遵循这个部署指南，确保密码管理器应用能够安全、稳定地发布到各大应用商店，并为用户提供优质的使用体验。