import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
}

group = "com.atilfaz"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    // HTTP client pour l'API Xtream
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

    // Chargement d'images (logo chaînes)
    implementation("io.coil-kt.coil3:coil-compose:3.0.0-rc01")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0-rc01")
}

compose.desktop {
    application {
        mainClass = "com.atilfaz.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Deb, TargetFormat.Dmg)
            packageName = "AtilfazIPTV"
            packageVersion = "1.0.0"
            description = "Atilfaz IPTV — Lecteur multiplateforme"
            vendor = "Atilfaz"

            windows {
                iconFile.set(project.file("src/main/resources/icon.ico"))
                menuGroup = "Atilfaz"
                upgradeUuid = "atilfaz-iptv-desktop-001"
                dirChooser = true
                shortcut = true
                perUserInstall = true
            }
        }
    }
}

// Tâche pour créer un JAR portable (sans installation, juste Java 17+)
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.atilfaz.desktop.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
