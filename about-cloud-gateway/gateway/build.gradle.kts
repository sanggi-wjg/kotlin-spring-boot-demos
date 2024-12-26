import org.springframework.boot.gradle.tasks.bundling.BootJar

extra["springCloudVersion"] = "2024.0.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux") // Reactive Web
    implementation("org.springframework.cloud:spring-cloud-starter-gateway") // Gateway
//    implementation("org.springframework.cloud:spring-cloud-starter-loadbalancer") // LoadBalancer
//    implementation("org.springframework.boot:spring-boot-starter-security") // Security
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = false
jar.enabled = true
