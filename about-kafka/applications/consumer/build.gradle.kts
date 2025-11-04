import org.springframework.boot.gradle.tasks.bundling.BootJar

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = true
jar.enabled = false

dependencies {
    implementation(project(":shared"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}