plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
    `maven-publish`
    id("org.sonarqube") version "6.0.1.5171" // Advanced code quality checks
    id("xyz.jpenilla.run-paper") version "2.3.1" // Paper server for testing/hotloading JVM
    id("com.modrinth.minotaur") version "2.8.7"
    id("me.modmuss50.mod-publish-plugin") version "0.5.1"
    id("io.papermc.hangar-publish-plugin") version "0.1.3" // publish to hangar
}

group = "net.mvndicraft.townywaypoints"
version = "1.9.1"
description = "Configurable plot types for Towny that players can teleport between."
java.sourceCompatibility = JavaVersion.VERSION_21
val mainMinecraftVersion = "1.21.4"
val lowestSupportedMinecraftVersion = "1.20"
val supportedMinecraftVersions = "$lowestSupportedMinecraftVersion - $mainMinecraftVersion"
val townyVersion = "0.102.0.0"
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
    assemble {
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
            github(
                "TownyAdvanced",
                "Towny",
                "$townyVersion",
                "towny-$townyVersion.jar"
            ) // we can't use the latest release because it's inside a zip.
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

val modrinthVersions = "1.20.1, 1.20.2, 1.20.3, 1.20.4, 1.20.5, 1.20.6, 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4"

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("townywaypoints")
    versionNumber.set(project.version as String)
    versionType.set("release")
    versionName.set("${project.name} ${versionNumber.get()}")
    uploadFile.set(tasks.jar.get().archiveFile)
    gameVersions.addAll(modrinthVersions.split(",").map { it.trim() })
    loaders.addAll("paper", "folia")
    changelog.set(readChangelog())

    syncBodyFrom.set(rootProject.file("README.md").readText())
}

publishMods {
    file.set(tasks.jar.get().archiveFile)
    type.set(me.modmuss50.mpp.ReleaseType.STABLE)
    modLoaders.addAll("paper", "folia")

    changelog.set(readChangelog())

    github {
        accessToken.set(System.getenv("TOWNYWAYPOINTS_GITHUB_PAT"))
        repository.set("Mvndi/TownyWaypoints")
        commitish.set("master")
        tagName.set(project.version as String)
        displayName.set("Version " + tagName.get())
    }
}

// Enable for public plugin with a Hangar page only.
hangarPublish { // ./gradlew publishPluginPublicationToHangar
    publications.register("plugin") {
        version.set(project.version as String)
        channel.set(if (isRelease) "Release" else "Snapshot")
        id.set(project.name)
        apiKey.set(System.getenv("HANGAR_API_TOKEN"))

        changelog.set(readChangelog())

        platforms {
            register(io.papermc.hangarpublishplugin.model.Platforms.PAPER) {
                url =
                    "https://github.com/Mvndi/" + project.name + "/releases/download/" + versionString + "/" + project.name + "-" + versionString + ".jar"

                // Set platform versions from gradle.properties file
                val versions: List<String> = supportedMinecraftVersions.replace(" ", "").split(",")
                platformVersions.set(versions)
            }
        }
    }
}

tasks.register("publishPlugin") {
    if ((project.version as String).endsWith("-SNAPSHOT"))
        throw GradleException("Snapshot versions should not be deployed")

    tasks.getByName("publishPluginPublicationToHangar").dependsOn(tasks.shadowJar)
    tasks.getByName("publishGithub").dependsOn(tasks.shadowJar)

    dependsOn(tasks.publishMods)
    dependsOn(tasks.publishAllPublicationsToHangar)
    dependsOn(tasks.modrinth)

    //dependsOn(tasks.syncAllPagesToHangar)
    dependsOn(tasks.modrinthSyncBody)
}

tasks.register("readChangelog") {
    println(readChangelog())
}

fun readChangelog(): String {
    val lines = mutableListOf<String>()
    val version = project.version.toString().substringBefore("-") // remove -SNAPSHOT if present

    var versionFound = false
    rootProject.file("src/main/resources/Changelog.txt").readLines().forEach {
        val line = it.trim()

        if (line.startsWith(version))
            versionFound = true
        else if (versionFound && !line.startsWith("-"))
            return@forEach

        if (versionFound && line.startsWith("-"))
            lines.add(line)
    }

    return lines.joinToString("\n")
}