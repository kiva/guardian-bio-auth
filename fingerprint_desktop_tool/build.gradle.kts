plugins {
    java
    kotlin("jvm") version "1.3.72"
    application
}

group = "org.kiva"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "org.kiva.fingerprint_desktop_tool.Templatize"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("info.picocli:picocli:4.3.2")
    implementation("com.machinezoo.sourceafis:sourceafis:3.8.2")
    implementation("org.slf4j:slf4j-simple:1.8.0-beta4")
    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}