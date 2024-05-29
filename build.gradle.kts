object Versions {
    const val atlantafx = "2.0.1"
    const val charts = "21.0.7"
    const val fxgl = "21.1"
    const val log4j = "2.23.1"
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.openjfx.javafxplugin") version "0.0.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.almasb:fxgl:${Versions.fxgl}")
    implementation("eu.hansolo.fx:charts:${Versions.charts}")
    implementation("io.github.mkpaz:atlantafx-base:${Versions.atlantafx}")
    implementation("org.apache.logging.log4j:log4j-core:${Versions.log4j}")

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