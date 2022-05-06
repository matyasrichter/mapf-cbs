plugins {
    kotlin("jvm")
}

description = "domain"
dependencies {
    testImplementation(kotlin("test"))
}
tasks.test {
    useJUnitPlatform()
}