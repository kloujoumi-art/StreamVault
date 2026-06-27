import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
}

group = "com.atilfaz"
version = "1.0.0"

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    // HTTP client pour l'API Xtream
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines (Swing dispatcher pour UI desktop)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
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
                menuGroup = "Atilfaz"
                upgradeUuid = "atilfaz-iptv-desktop-001"
                dirChooser = true
                shortcut = true
                perUserInstall = true
            }
        }
    }
}
