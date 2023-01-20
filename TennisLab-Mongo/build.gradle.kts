import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    application
    kotlin("plugin.serialization") version "1.7.20"
}

group = "es.ivanloli"
version = "1.0-SNAPSHOT"
val koin_version= "3.3.2"
val koin_ksp_version= "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    // Opcional, solo si vamos a usar asíncrono
    implementation("org.litote.kmongo:kmongo-async:4.7.2")
    // Usamos corrutinas para ello
    implementation("org.litote.kmongo:kmongo-coroutine:4.7.2")

    // Corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // Para hacer el logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("ch.qos.logback:logback-classic:1.4.5")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.10.1")


    // Koin Core features
    implementation("io.insert-koin:koin-core:$koin_version")
// Koin Test features
    testImplementation("io.insert-koin:koin-test:$koin_version")

    testImplementation("io.insert-koin:koin-test-junit5:$koin_version")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}