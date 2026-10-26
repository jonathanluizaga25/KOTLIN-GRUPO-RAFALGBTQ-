plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ksp) apply false
}

tasks.register<Exec>("runExamBackend") {
    group = "examen"
    description = "Inicia juntos el servidor Spring Boot gRPC y el túnel ngrok"
    dependsOn(":grpcServer:bootJar")
    commandLine("zsh", rootProject.file("start-exam-backend.sh").absolutePath)
}
