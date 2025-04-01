plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
    `maven-publish`
}

group = "net.mvndicraft.townywaypoints"
version = "1.8"
description = "Configurable plot types for Towny that players can teleport between."

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.glaremasters.me/repository/towny/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.codemc.org/repository/maven-public")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.palmergames.bukkit.towny:towny:0.99.5.0")
    compileOnly("io.github.townyadvanced.commentedconfiguration:CommentedConfiguration:1.0.0")
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.9")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("com.github.Anon8281:UniversalScheduler:0.1.6")
    implementation("fr.formiko.mc.biomeutils:biomeutils:1.1.8")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    shadowJar {
        val prefix = "${project.group}.lib"
        sequenceOf(
            "co.aikar",
            "com.github.Anon8281.universalScheduler",
        ).forEach { pkg ->
            relocate(pkg, "$prefix.$pkg")
        }

        archiveFileName.set("${project.name}-${project.version}.jar")
    }
    build {
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props = mapOf(
            "name" to project.name,
            "version" to project.version,
            "description" to project.description,
            "apiVersion" to "1.20",
            "group" to project.group
        )
        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}
