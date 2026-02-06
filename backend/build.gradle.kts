plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    application
}

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.12"
val exposedVersion = "0.50.1"
val hikariVersion = "5.1.0"
val postgresVersion = "42.7.3"
val bcryptVersion = "0.4"
val nimbusVersion = "9.37.3"

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("org.mindrot:jbcrypt:$bcryptVersion")
    implementation("com.nimbusds:nimbus-jose-jwt:$nimbusVersion")
    implementation("com.corundumstudio.socketio:netty-socketio:2.0.9")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.24.0")
}

application {
    mainClass.set("com.kappa.backend.ApplicationKt")
}

tasks.withType<org.gradle.api.tasks.compile.JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
    enabled = false
}

tasks.named("check") {
    enabled = false
}

tasks.named("build") {
    setDependsOn(listOf("assemble"))
}
