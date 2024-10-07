object Versions {
    const val atlantafx = "2.0.1"
<<<<<<< HEAD
    const val charts = "21.0.19"
=======
    const val charts = "21.0.21"
>>>>>>> 31621d5 (Fixed “orange circle problem” in RadarChart and updated dependency to charts lib)
    const val fxgl = "21.1"
    const val jserial = "2.11.0"
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

application {
    mainClass.set("be.codewriter.melodymatrix.view.TestLauncher")
}

dependencies {
    implementation("com.github.almasb:fxgl:${Versions.fxgl}")
    implementation("eu.hansolo.fx:charts:${Versions.charts}")
    implementation("io.github.mkpaz:atlantafx-base:${Versions.atlantafx}")
    implementation("org.apache.logging.log4j:log4j-core:${Versions.log4j}")
    implementation("com.fazecast:jSerialComm:${Versions.jserial}")

    // Add Kotlin runtime dependency
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

javafx {
    version = "21.0.2"
    modules("javafx.controls", "javafx.fxml", "javafx.media")
}