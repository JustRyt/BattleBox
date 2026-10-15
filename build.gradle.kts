plugins {
    id("java")
    id("io.freefair.lombok") version "9.8.0"
}

group = "net.justryt.battlebox"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    compileOnly("com.zaxxer:HikariCP:7.1.0")

    runtimeOnly("com.mysql:mysql-connector-j:26.7.0")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.5.10")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.processResources {
    expand(mapOf("version" to version))
}