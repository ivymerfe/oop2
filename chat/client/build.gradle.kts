plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "labs.network.client"
version = "1.0.0"

application {
    mainModule.set("labs.network.client")
    mainClass.set("labs.network.client.App")
}

javafx {
    version = "26.0.1"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation(project(":protocol"))
    implementation("io.github.mkpaz:atlantafx-base:2.1.0")
}
