plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
    `maven-publish`
    id("org.sonarqube") version "6.0.1.5171" // Advanced code quality checks
    id("xyz.jpenilla.run-paper") version "2.3.1" // Paper server for testing/hotloading JVM
    id("io.papermc.hangar-publish-plugin") version "0.1.3" // publish to hangar
}

group = "net.mvndicraft.townywaypoints"
version = "1.9"
description = "Configurable plot types for Towny that players can teleport between."
java.sourceCompatibility = JavaVersion.VERSION_21
val mainMinecraftVersion = "1.21.4"
val lowestSupportedMinecraftVersion = "1.20"
val supportedMinecraftVersions = "$lowestSupportedMinecraftVersion - $mainMinecraftVersion"
val townyVersion = "0.101.0.2"
val vaultUnlockedVersion = "2.10.0"

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
    compileOnly("io.papermc.paper:paper-api:$mainMinecraftVersion-R0.1-SNAPSHOT")
    compileOnly("com.palmergames.bukkit.towny:towny:$townyVersion")
    compileOnly("io.github.townyadvanced.commentedconfiguration:CommentedConfiguration:1.0.0")
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.10")
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
    processResources {
        val props = mapOf(
            "name" to project.name,
            "version" to project.version,
            "description" to project.description,
            "apiVersion" to lowestSupportedMinecraftVersion,
            "group" to project.group
        )
        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }

    runServer {
        downloadPlugins {
            github("TownyAdvanced", "Towny", "$townyVersion", "towny-$townyVersion.jar") // we can't use the latest release because it's inside a zip.
            modrinth("vaultunlocked", "$vaultUnlockedVersion")
        }
        minecraftVersion("$mainMinecraftVersion")
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

sonar {
  properties {
    property("sonar.projectKey", project.name)
    property("sonar.projectName", project.name)
    property("sonar.host.url", "https://mvndisonar.formiko.fr")
  }
}

tasks.register("echoVersion") {
    description = "Print the version of this project."
    group = JavaBasePlugin.CHECK_TASK_NAME
    doLast {
        println("${project.version}")
    }
}

tasks.register("echoReleaseName") {
    description = "Print the release name for this project."
    group = JavaBasePlugin.CHECK_TASK_NAME
    doLast {
        println("${project.version} [${supportedMinecraftVersions}]")
    }
}

val versionString: String = version as String
val isRelease: Boolean = !versionString.contains("SNAPSHOT")

// Enable for public plugin with a Hangar page only.
hangarPublish { // ./gradlew publishPluginPublicationToHangar
    publications.register("plugin") {
        version.set(project.version as String)
        channel.set(if (isRelease) "Release" else "Snapshot")
        id.set(project.name)
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))
        platforms {
            register(io.papermc.hangarpublishplugin.model.Platforms.PAPER) {
                url = "https://github.com/Mvndi/"+project.name+"/releases/download/"+versionString+"/"+project.name+"-"+versionString+".jar"

                // Set platform versions from gradle.properties file
                val versions: List<String> = supportedMinecraftVersions.replace(" ", "").split(",")
                platformVersions.set(versions)
            }
        }
    }
}