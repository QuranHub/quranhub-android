plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.room)
}

android {
    namespace = "app.quranhub"
    compileSdk = libs.versions.compileSdkVersion.get().toInt()

    defaultConfig {
        applicationId = "app.quranhub"
        minSdk = libs.versions.minSdkVersion.get().toInt()
        targetSdk = libs.versions.targetSdkVersion.get().toInt()
        versionCode = 14
        versionName = "1.5.0"

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            manifestPlaceholders["enableCrashlytics"] = true
        }
        debug {
            isMinifyEnabled = false

            manifestPlaceholders["enableCrashlytics"] = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }
    lint {
        abortOnError = false
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // Misc.
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.multidex)
    implementation(libs.easypermissions)
    implementation(libs.eventbus)
    implementation(libs.androidx.annotation)
    implementation(libs.kotlin.parcelize.runtime)
    implementation(libs.kotlinx.coroutines.android)

    // UI-related
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.materialdrawer)
    implementation(libs.cardview)
    implementation(libs.recyclerview)
    implementation(libs.legacy.support.v4)
    implementation(libs.expandabletextview)
    implementation(libs.expandablerecyclerview)
    implementation(libs.recyclerview.fastscroll)
    implementation(libs.spectrum)
    implementation(libs.viewpager)
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)
    implementation(libs.fancyshowcaseview)
    implementation(libs.circular.progress.button)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit.adapter.rxjava2)
    implementation(libs.glide)
    ksp(libs.glide.ksp)
    implementation(project(":prdownloader-service"))

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.room.rxjava2)

    // Lifecycle, ViewModel & LiveData
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.common.java8)

    // ReactiveX
    implementation(libs.rxjava2.rxandroid)
    implementation(libs.rxjava2)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
