plugins {
    java
    id("org.springframework.boot") version "2.6.3"
}

apply(plugin = "io.spring.dependency-management")

group = "com.github.fmjsjx.demo"
version = "1.0.0-SNAPSHOT"
description = "A game demo project for HTTP server."

repositories {
    maven {
        url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
    }
    mavenCentral()
}

configurations {
	compileOnly.extendsFrom(configurations.annotationProcessor.get())
	"implementation" {
	    // using log4j2 must exclude logback
        exclude(module = "spring-boot-starter-logging")
    }
}

extra["netty.version"] = "4.1.73.Final"

dependencies {
    implementation(platform("com.github.fmjsjx:libnetty-bom:2.4.2"))
    implementation(platform("com.github.fmjsjx:libcommon-bom:2.6.1"))
    implementation(platform("com.github.fmjsjx:myboot-bom:1.1.9"))
    implementation(platform("com.github.fmjsjx:bson-model-bom:1.4.0"))

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("com.lmax:disruptor:3.4.4")
    implementation("com.github.fmjsjx:bson-model-core")
    compileOnly("com.github.fmjsjx:bson-model-generator")
    implementation("org.jruby:jruby:9.3.2.0")
    implementation("com.github.fmjsjx:libcommon-collection")
    implementation("com.github.fmjsjx:libcommon-util")
    implementation("com.github.fmjsjx:libcommon-json-jackson2")
    implementation("com.github.fmjsjx:libcommon-json-jsoniter")
    implementation("org.javassist:javassist:3.28.0-GA")
    implementation("com.github.fmjsjx:libcommon-yaml")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:2.2.1")
    implementation("mysql:mysql-connector-java")
    implementation("com.github.fmjsjx:myboot-starter-redis") {
        exclude(group = "org.apache.commons", module = "commons-pool2")
    }
    implementation("com.github.fmjsjx:myboot-starter-mongodb")
    implementation(group = "io.netty", name = "netty-tcnative-boringssl-static", classifier = "linux-x86_64")
    implementation(group = "io.netty", name = "netty-transport-native-epoll", classifier = "linux-x86_64")
    implementation("com.github.fmjsjx:libnetty-http-client")
    implementation("com.github.fmjsjx:libnetty-http-server")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.test {
    // Use junit platform for unit tests.
    useJUnitPlatform()
}

tasks.javadoc {
    enabled = false
}
