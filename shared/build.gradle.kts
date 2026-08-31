plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("app.cash.sqldelight")
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                }
            }
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            implementation("app.cash.sqldelight:runtime:2.0.2")
            implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
        }
        androidMain.dependencies {
            implementation("app.cash.sqldelight:android-driver:2.0.2")
            implementation("com.google.android.gms:play-services-location:21.3.0")
            implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
            implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
            implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
            implementation("androidx.activity:activity-compose:1.9.3")
            implementation("androidx.core:core-ktx:1.15.0")
        }
        iosMain.dependencies {
            implementation(compose.ui)
            implementation("app.cash.sqldelight:native-driver:2.0.2")
        }
    }
}

android {
    namespace = "com.gardenyes.rowingmetrics.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.gardenyes.rowingmetrics.generated.resources"
}

sqldelight {
    databases {
        create("RowingMetricsDatabase") {
            packageName.set("com.gardenyes.rowingmetrics.data.sqldelight")
        }
    }
}
