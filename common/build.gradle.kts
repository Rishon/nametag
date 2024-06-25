plugins {
    java
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
}

group = "systems.rishon.common"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    paperweight.paperDevBundle("1.20.6-R0.1-SNAPSHOT")
}

val targetJavaVersion = 21
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.register("prepareKotlinBuildScriptModel") {}
