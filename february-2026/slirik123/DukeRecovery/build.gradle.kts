plugins {
    id("java")
    id("io.freefair.lombok") version "9.2.0"
    id("com.gradleup.shadow") version "9.3.1"
}

group = "com.solutiongameofficial"
version = "v1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    enabled = false
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.register<JavaExec>("runStandalone") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.solutiongameofficial.Main")

    environment("APP_NAME", "SolutionWCMD - Duke Recovery")
}

tasks.register<JavaExec>("runHeadless") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.solutiongameofficial.Main")

    args("--headless")
}

tasks.register<JavaExec>("convertRawToAscii") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.solutiongameofficial.RawToAscii")
}

tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes("Main-Class" to "com.solutiongameofficial.Main")
    }
}