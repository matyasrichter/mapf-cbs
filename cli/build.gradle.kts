plugins {
    kotlin("jvm")
}

description = "cli"
dependencies {
    implementation("com.github.ajalt.clikt:clikt:3.4.2")
    implementation(project(":domain"))
    implementation(project(":parser"))
    implementation(project(":solver"))
    testImplementation(kotlin("test"))
}