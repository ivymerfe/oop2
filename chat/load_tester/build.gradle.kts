plugins {
    java
    application
}

group = "labs.network.load_tester"
version = "1.0.0"

application {
    mainClass.set("labs.network.load_tester.App")
}

dependencies {
    implementation(project(":protocol"))
}
