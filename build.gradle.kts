import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
}

group = "com.contedevel"
version = "1.0-SNAPSHOT"

val exposedVersion = "0.37.3"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    runtimeOnly("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.hibernate:hibernate-core:5.6.3.Final")
    implementation("org.postgresql:postgresql:42.3.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}

application {
    mainClass.set("MainKt")
}