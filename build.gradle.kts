object Versions {
    const val atlantafx = "2.0.1"
    const val charts = "21.0.7"
    const val fxgl = "21.1"
    const val kotlinCoroutines = "1.8.1"
    const val kotlinSerialization = "1.6.3"
    const val ktmidi = "0.8.2"
    const val log4j = "2.23.1"
}

plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.openjfx.javafxplugin") version "0.0.10"
    // https://github.com/atsushieno/ktmidi/issues/79
    // This plugin replaces the references to all those javacpp platforms with whatever required
    // only for the platform.
    id("org.bytedeco.gradle-javacpp-platform") version "1.5.10"
    kotlin("plugin.serialization") version "1.9.23"
    application
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.almasb:fxgl:${Versions.fxgl}")
    implementation("dev.atsushieno:ktmidi:${Versions.ktmidi}")
    implementation("dev.atsushieno:ktmidi-jvm:${Versions.ktmidi}")
    implementation("dev.atsushieno:ktmidi-jvm-desktop:${Versions.ktmidi}")
    implementation("eu.hansolo.fx:charts:${Versions.charts}")
    implementation("io.github.mkpaz:atlantafx-base:${Versions.atlantafx}")
    implementation("org.apache.logging.log4j:log4j-core:${Versions.log4j}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinSerialization}")

    // Add Kotlin runtime dependency
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

javafx {
    version = "21.0.2"
    modules("javafx.controls", "javafx.fxml", "javafx.media")
}

application {
    mainClass.set("be.codewriter.melodymatrix.view.TestLauncher")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to application.mainClass.get()
        )
    }

    archiveBaseName.set("MelodyMatrixView")

    // Include Kotlin runtime dependencies
    from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) })

    // Set duplicates handling strategy
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
kotlin {
    jvmToolchain(8)
}