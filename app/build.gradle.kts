plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.simpleliveroom"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.simpleliveroom"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    //RecyclerView 是 Android 里一种用于显示列表的控件。
    //RecyclerView.Adapter 是 RecyclerView 用来显示列表项的一种适配器。
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    //ExoPlayer 是 Android 里一种用于播放视频的一种库。
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    //Media3 是 Android 里一种用于播放视频的一种库。
    //它包含了 ExoPlayer 等视频播放器的实现。
    implementation("androidx.media3:media3-ui:1.4.1")
    //ExoPlayer 是 Android 里一种用于播放视频的一种库。
    //它包含了 ExoPlayer 等视频播放器的实现。
    implementation("androidx.media3:media3-exoplayer-dash:1.4.1")
}