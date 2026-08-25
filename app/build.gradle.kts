import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}

fun Properties.requireKeystoreProperty(name: String): String =
    getProperty(name) ?: error("Missing \"$name\" in keystore.properties (see keystore.properties.example)")

android {
    namespace = "com.gardenyes.rowingmetrics"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gardenyes.rowingmetrics"
        minSdk = 26
        targetSdk = 35
        val appVersionCode = 2
        versionCode = appVersionCode
        versionName = appVersionCode.toString()
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                keyAlias = keystoreProperties.requireKeystoreProperty("keyAlias")
                keyPassword = keystoreProperties.requireKeystoreProperty("keyPassword")
                storeFile = rootProject.file(
                    keystoreProperties.requireKeystoreProperty("storeFile"),
                )
                storePassword = keystoreProperties.requireKeystoreProperty("storePassword")
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isDebuggable = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.15.0")
}
