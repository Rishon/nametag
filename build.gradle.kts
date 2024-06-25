plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    group = "systems.rishon"
    version = "1.0"
}


java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

subprojects {
    apply(plugin = "com.github.johnrengelman.shadow")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(project(":plugin"))
}