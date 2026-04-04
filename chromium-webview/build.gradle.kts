import de.undercouch.gradle.tasks.download.Download

plugins {
    id("com.android.library")
    id("de.undercouch.download") version "5.6.0"
}

val chromiumAwVersion = "93.0.4577.82-1"
val chromiumAwFileName = "chromium-aw-release.aar"
val chromiumAwDestPath = "${rootProject.projectDir}/libs/$chromiumAwFileName"

tasks.register("downloadChromiumAw", Download::class.java) {
    enabled = !file(chromiumAwDestPath).exists()
    src("https://github.com/ridi/chromium-aw/releases/download/$chromiumAwVersion/$chromiumAwFileName")
    dest(chromiumAwDestPath)
    overwrite(true)
    onlyIfModified(true)
}

tasks.matching { it.name != "downloadChromiumAw" }.all {
    dependsOn("downloadChromiumAw")
}

android {
    namespace = "io.homeassistant.companion.android.chromium"
    compileSdk = libs.versions.androidSdk.compile.get().toInt()

    defaultConfig {
        minSdk = 29
    }

    androidResources {
        noCompress += listOf("bin", "dat", "pak")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}

dependencies {
    implementation(files(chromiumAwDestPath))
    implementation(libs.timber)
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
}
