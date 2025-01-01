import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation(project(":storage:mysql"))

    implementation("org.springframework.boot:spring-boot-starter-web")
}

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = true
jar.enabled = false
