# InZight — Android App

Native Android client for personal finance management, social interactions, messaging, and financial planning.

## Features

- Onboarding, login, registration, and OTP verification screens.
- Income/expense tracking, wallet and budget views, and financial charts.
- Social posts, comments, friendships, and real-time chat.
- AI assistant chat backed by the InZight API.
- Premium-plan checkout and payment-return navigation.
- Financial planning views, QR scanning, and receipt text-recognition components.

Some flows require backend configuration, device permissions, and valid integration credentials on the server.

## Technology stack

Java, Android SDK, AndroidX, Material Components, View Binding, Retrofit, Gson, OkHttp, MPAndroidChart, Glide, STOMP over WebSocket, RxJava/RxAndroid, CameraX, ML Kit, ZXing, and Gradle.

## Requirements

- Android Studio compatible with Android Gradle Plugin 8.10.1.
- JDK 17 for Gradle/AGP; application Java source compatibility is configured as Java 11.
- Android SDK 35 and an emulator or device running Android 7.0/API 24 or newer.
- A running InZight backend.

## Open and run

1. Open the `InZightApp` directory in Android Studio and let Gradle sync.
2. Install the requested Android SDK components. Android Studio creates `local.properties` for your local SDK path; do not commit it.
3. Configure the backend URL in `app/src/main/java/com/example/inzightapp/api/ApiClient.java`.
4. Start the backend, select a device/emulator, and run the `app` configuration.

For the standard Android emulator, the backend on your host machine is reachable at `http://10.0.2.2:8080/`. For a physical device, set `BASE_URL` to a reachable backend address with a trailing slash. Verify both REST and WebSocket connections after changing it. Production endpoints should use HTTPS/WSS.

```powershell
cd InZightApp
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

Instrumented tests require an emulator or device:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Structure

```text
InZightApp/
  app/
    src/main/java/com/example/inzightapp/
      api/             Retrofit client and API definitions
      view/            Activities and screens
    src/main/res/      Layouts, strings, icons, and images
    src/main/AndroidManifest.xml
    build.gradle
  gradle/              Wrapper and version catalog
  settings.gradle
```

## Configuration and permissions

Authentication tokens are obtained from the backend at runtime; do not hardcode them. Payment-provider secrets and AI-provider keys belong on the server, not in the app. Camera/media permissions support scanning and image selection. Review manifest permissions, backup behavior, and network-security settings before a production release.

The development API URL defaults to the Android emulator host. No private tunnel domain, signing keystore, or local SDK path is required in version control. Configure release signing locally and keep keystores/passwords outside Git.

## Build scope

Compiling an APK does not verify login, payments, AI responses, or device-permission flows. Validate those features against the configured backend and on the intended Android versions.
