plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Shared Presentation Logic, navigation and connection between data and UI"
        homepage = "X"
        version = "0.0.1"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../../iosClient/Podfile")
        framework {
            baseName = "presentation"
            isStatic = false
        }
    }

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
            implementation(project(":shared:domain"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.logging.kermit)
            implementation(libs.kotlinx.coroutines)
            implementation("io.coil-kt.coil3:coil-compose:3.0.0-rc02")
            implementation("io.coil-kt.coil3:coil-svg:3.0.0-rc02")

            //https://github.com/Kamel-Media/Kamel
            //implementation("media.kamel:kamel-image:1.0.0")
            //implementation("media.kamel:kamel-decoder-svg-std:1.0.0")
            //implementation("io.ktor:ktor-server-netty:3.0.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "network.bisq.mobile"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependencies {
        //implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0-rc02")
        //implementation("io.ktor:ktor-client-android:3.0.0")
    }
//    dependencies {
//        implementation("media.kamel:kamel-fetcher-resources-android:1.0.0")
//    }
}

//appleMain {
//    dependencies {
//        implementation("io.ktor:ktor-client-darwin:3.0.0")
//    }
//}
//jvmMain {
//    dependencies {
//        implementation("io.ktor:ktor-client-java:3.0.0>")
//    }
//}
