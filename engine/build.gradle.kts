plugins {
    id("java")
    id("jacoco")
}

group = "uk.ac.york.bitbotarena"
version = "1.0-SNAPSHOT"
val mockitoAgent by configurations.creating

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.mockito:mockito-core:5.+")
    mockitoAgent("org.mockito:mockito-core:5.+")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("info.picocli:picocli:4.7.5")
    annotationProcessor("info.picocli:picocli-codegen:4.7.5")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    // Resolve the agent jar when the task runs and add it to the JVM args
    doFirst {
        val agentJar = configurations.getByName("mockitoAgent")
            .files
            .firstOrNull { it.name.contains("mockito", ignoreCase = true) }
            ?: throw GradleException("Mockito agent jar not found in 'mockitoAgent' configuration")

        jvmArgs("-javaagent:${agentJar.absolutePath}")
    }
}

tasks.register<JavaExec>("run") {
    group = "application"
    description = "Run the Main class"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("uk.ac.york.bitbotarena.Main")
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}