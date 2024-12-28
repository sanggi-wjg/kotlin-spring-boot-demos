import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation(project(":support"))
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    runtimeOnly("com.mysql:mysql-connector-j")
}

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = true
jar.enabled = false
