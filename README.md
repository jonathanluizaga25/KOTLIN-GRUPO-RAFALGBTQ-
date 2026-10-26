# KOTLIN-GRUPO-RAFALGBTQ-

Proyecto Kotlin Multiplatform con una aplicación Android y un servidor gRPC.

Antes de compilar la aplicación, descarga `google-services.json` de tu proyecto
Firebase y colócalo en `androidApp/`. Este archivo no se incluye en el repositorio.
Para la lista colaborativa se necesita crear Firestore y publicar las reglas
descritas en [INTEGRADOR_E100.md](./INTEGRADOR_E100.md).

La aplicación Android incluye Firebase Authentication, Realtime Database,
Remote Config, FCM, localización, temas y un cliente gRPC. El servidor gRPC de
Spring Boot y los pasos de demostración están documentados en
[GRPC_EXAM.md](./GRPC_EXAM.md).

La ejecución de todos los componentes desde el IDE está explicada en
[ANDROID_STUDIO.md](./ANDROID_STUDIO.md).

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- Servidor gRPC: `./start-grpc-server.sh`
- Túnel HTTPS de ngrok: `./start-ngrok.sh`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
