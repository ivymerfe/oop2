plugins {
    java
    application
}

group = "labs.network.server"
version = "1.0.0"

application {
    mainClass.set("labs.network.server.Main")
}

dependencies {
    implementation(project(":protocol"))
    implementation("org.apache.logging.log4j:log4j-core:2.25.4")
    implementation("org.apache.logging.log4j:log4j-api:2.25.4")
}
