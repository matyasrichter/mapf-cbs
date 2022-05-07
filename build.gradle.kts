plugins {
    base
    kotlin("jvm") version "1.6.10" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
    tasks.withType<AbstractTestTask> {
        afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
            if (desc.parent == null) { // will match the outermost suite
                println("Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)")
            }
        }))
    }
}
