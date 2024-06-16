plugins {
    id("java")
}

group = "com.adong.study"
version = "6.2.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
	implementation(project(":spring-context"))

}

tasks.test {
    useJUnitPlatform()
}