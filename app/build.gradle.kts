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
    //Retrofit 是 Android 里一种用于网络请求的一种库。
    //它包含了 OkHttp 等网络请求的实现。
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    //Gson 是 Android 里一种用于解析 JSON 的一种库。
    //它包含了 Gson 解析 JSON 的实现。
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.code.gson:gson:2.10.1")
    //OkHttp 是 Android 里一种用于网络请求的一种库。
    //它包含了 OkHttp 等网络请求的实现。
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    //Fresco 是 Android 里一种用于加载图片的一种库。
    //它包含了 Fresco 加载图片的实现。
    implementation("com.facebook.fresco:fresco:3.4.0")
}