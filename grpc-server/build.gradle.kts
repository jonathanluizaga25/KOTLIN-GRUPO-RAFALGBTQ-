plugins {
    java
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.6"
}

group = "org.ucb"
version = "1.0.0"
description = "Servidor Spring Boot gRPC para el examen UCBP1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-grpc-server")
    implementation("io.grpc:grpc-services")
    testImplementation("org.springframework.boot:spring-boot-starter-grpc-server-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.36.2"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.84.1"
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<com.google.protobuf.gradle.GenerateProtoTask>().configureEach {
    notCompatibleWithConfigurationCache(
        "El plugin Protobuf conserva referencias al modelo de Gradle durante la generación"
    )
}
