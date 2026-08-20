plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.aboutlibraries.plugin)
}

android {
    namespace = "our.bunny.julie"
    compileSdk = 35

    defaultConfig {
        applicationId = "our.bunny.julie"
        minSdk = 26
        targetSdk = 37
        versionCode = 6
        versionName = "1.0.10"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    flavorDimensions += "abi"

    productFlavors {
        create("arm64") {
            dimension = "abi"
            buildConfigField("boolean", "ENABLE_FANCY_UI", "true")
            ndk {
                abiFilters += "arm64-v8a"
            }
        }
        create("armv7") {
            dimension = "abi"
            buildConfigField("boolean", "ENABLE_FANCY_UI", "false")
            ndk {
                abiFilters += "armeabi-v7a"
            }
        }
        create("universal") {
            dimension = "abi"
            buildConfigField("boolean", "ENABLE_FANCY_UI", "true")
            // No abiFilters, so all ABIs are included
        }
    }

    signingConfigs {
        create("release") {
            // Read from project properties (from ~/.gradle/gradle.properties) or environment variables
            val storeFilePath = project.findProperty("JULIE_STORE_FILE")?.toString() ?: System.getenv("JULIE_STORE_FILE")
            if (storeFilePath != null && file(storeFilePath).exists()) {
                storeFile = file(storeFilePath)
                storePassword = project.findProperty("JULIE_STORE_PASSWORD")?.toString() ?: System.getenv("JULIE_STORE_PASSWORD")
                keyAlias = project.findProperty("JULIE_KEY_ALIAS")?.toString() ?: System.getenv("JULIE_KEY_ALIAS")
                keyPassword = project.findProperty("JULIE_KEY_PASSWORD")?.toString() ?: System.getenv("JULIE_KEY_PASSWORD")
            } else {
                throw GradleException("Release keystore not found or credentials not provided. Set JULIE_STORE_FILE, JULIE_STORE_PASSWORD, JULIE_KEY_ALIAS, JULIE_KEY_PASSWORD in ~/.gradle/gradle.properties or env vars.")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            resValue("string", "app_name", "Julie")
        }
        debug {
            resValue("string", "app_name", "Julie Debug")
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
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

androidComponents {
    onVariants(selector().withBuildType("debug")) { variant ->
        variant.applicationId.set("our.rabbit.julie")
    }
}

android {
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val outputImpl = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
            val flavorName = variant.flavorName
            val buildType = variant.buildType.name
            val version = variant.versionName
            val typeSuffix = if (buildType == "release") "" else "-debug"
            if (flavorName.isNotEmpty()) {
                outputImpl.outputFileName = "Julie-v$version-$flavorName$typeSuffix.apk"
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.work)

    // WorkManager & DataStore
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)

    // Coil
    implementation(libs.coil.compose)

    // Material Color Utilities (HCT, DynamicScheme, palette styles)
    implementation(libs.material.color.utilities)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // AboutLibraries
    implementation(libs.aboutlibraries.compose.m3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
