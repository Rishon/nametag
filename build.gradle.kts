plugins {
    java
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "systems.rishon.nametag"
version = "1.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        name = "seladevelopmentReleases"
        url = uri("https://repo.rishon.systems/releases")
    }
    maven {
        name = "placeholderapi-repo"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.5")

    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")

    implementation("systems.rishon:utils-api:1.0.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

val targetJavaVersion = 21
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.shadowJar {
    archiveBaseName.set("nametag")
    archiveClassifier.set("")
    mergeServiceFiles()
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}