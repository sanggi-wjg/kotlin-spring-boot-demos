import org.springframework.boot.gradle.tasks.bundling.BootJar

val jar: Jar by tasks
val bootJar: BootJar by tasks

bootJar.enabled = true
jar.enabled = false

plugins {
    id("org.asciidoctor.jvm.convert") version "3.3.2"
}

dependencies {
    implementation(project(":shared"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("com.mysql:mysql-connector-j")

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    implementation("com.querydsl:querydsl-kotlin:5.0.0")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")

    // SpringRestDocs
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
//    testImplementation("org.springframework.restdocs:spring-restdocs-core")
//    testImplementation("org.springframework.restdocs:spring-restdocs-webtestclient")

    // TestContainers
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
//    testImplementation("org.testcontainers:kafka")
}

val snippetsDir by extra { file("build/generated-snippets") }

tasks {
    test {
        outputs.dir(snippetsDir)
    }

    asciidoctor {
        dependsOn(test)
        inputs.dir(snippetsDir)

        doFirst {
//            delete(file("src/main/resources/static/docs"))
        }
        doLast {
            copy {
//                from(snippetsDir)
//                into(file("src/main/resources/static/docs"))
            }
        }
    }

    build {
        dependsOn(asciidoctor)
    }

    bootJar {
        dependsOn(asciidoctor)
    }
}
