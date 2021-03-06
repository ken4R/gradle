plugins {
    gradlebuild.distribution.`core-api-java`
}

description = "Logging infrastructure"

gradlebuildJava.usedInWorkers()

dependencies {
    api(library("slf4j_api"))

    implementation(project(":baseServices"))
    implementation(project(":messaging"))
    implementation(project(":cli"))
    implementation(project(":buildOption"))

    implementation(project(":native"))
    implementation(library("jul_to_slf4j"))
    implementation(library("ant"))
    implementation(library("commons_lang"))
    implementation(library("guava"))
    implementation(library("jansi"))

    runtimeOnly(library("log4j_to_slf4j"))
    runtimeOnly(library("jcl_to_slf4j"))

    testImplementation(testFixtures(project(":core")))

    integTestImplementation(library("ansi_control_sequence_util"))

    integTestRuntimeOnly(project(":apiMetadata"))
    integTestRuntimeOnly(project(":runtimeApiInfo"))
    integTestRuntimeOnly(project(":kotlinDslProviderPlugins"))
    integTestRuntimeOnly(project(":testingJunitPlatform"))

    testFixturesImplementation(project(":baseServices"))
    testFixturesImplementation(testFixtures(project(":core")))
    testFixturesImplementation(library("slf4j_api"))
}

classycle {
    excludePatterns.set(listOf("org/gradle/internal/featurelifecycle/**"))
}
