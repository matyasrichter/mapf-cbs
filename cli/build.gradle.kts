plugins {
    application
    kotlin("jvm")
}

application {
    mainClass.set("dev.mrichter.mapf.cli.MainKt")
}

description = "cli"
dependencies {
    implementation("com.github.ajalt.clikt:clikt:3.4.2")
    implementation(project(":domain"))
    implementation(project(":parser"))
    implementation(project(":solver"))
    implementation(project(":visualiser"))
    testImplementation(kotlin("test"))
}