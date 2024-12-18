plugins {
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
    kotlin("plugin.jpa") version "2.0.21"
    kotlin("kapt") version "2.0.21"
}

group = "com.raynor.demo"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // querydsl
    implementation("com.querydsl:querydsl-core:5.1.0")
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
    //
//    implementation("com.querydsl:querydsl-sql:5.1.0")
//    implementation("com.querydsl:querydsl-sql-spring:5.1.0")

    runtimeOnly("com.mysql:mysql-connector-j")
    developmentOnly("org.springframework.boot:spring-boot-devtools")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-property:5.9.1")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

// querydsl gen 설정
val queryDslGenerated = file("src/main/generated")

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.generatedSourceOutputDirectory.set(queryDslGenerated)
}

sourceSets {
    main {
        kotlin.srcDirs += queryDslGenerated
    }
}

tasks.named("clean") {
    doLast {
        queryDslGenerated.deleteRecursively()
    }
}

kapt {
    generateStubs = true
}
// querydsl gen 설정 end

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.Embeddable")
    annotation("javax.persistence.MappedSuperclass")
}

noArg {
//    annotation("javax.persistence.Entity")
//    annotation("javax.persistence.MappedSuperclass")
//    annotation("javax.persistence.Embeddable")
//    annotation("com.raynor.demo.abouttransaction.annotation.NoArg")
//    invokeInitializers = true
}
