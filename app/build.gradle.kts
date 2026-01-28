plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simplstudios.simplstream"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.simplstudios.simplstream"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // TMDB API Configuration
        buildConfigField("String", "TMDB_API_KEY", "\"335a2d8a6455213ca6201aba18056860\"")
        buildConfigField("String", "TMDB_ACCESS_TOKEN", "\"eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIzMzVhMmQ4YTY0NTUyMTNjYTYyMDFhYmExODA1Njg2MCIsIm5iZiI6MTc1MTc0OTQ1OC4zOTIsInN1YiI6IjY4Njk5MzUyMWQwZWY3OWVhYjcwMjU5OCIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.U2qkr0rOSg5WVFsruZacaE9V0Pf21S7ofF8HTN-jcRk\"")
        buildConfigField("String", "TMDB_BASE_URL", "\"https://api.themoviedb.org/3/\"")
        buildConfigField("String", "TMDB_IMAGE_BASE_URL", "\"https://image.tmdb.org/t/p/\"")
        
        // Video Sources (embed URLs) - 4 servers only
        buildConfigField("String", "MOVIES111_BASE_URL", "\"https://111movies.com\"")  // Server Alpha
        buildConfigField("String", "VIDNEST_BASE_URL", "\"https://vidnest.fun\"")      // Server Dot (least ads)
        buildConfigField("String", "VIDSRC_BASE_URL", "\"https://vidsrc-embed.ru/embed\"") // Server Canteen (uses IMDB ID)
        buildConfigField("String", "VIDLINK_BASE_URL", "\"https://vidlink.pro\"")      // Server Omega
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.11.0")

    // Leanback (Android TV)
    implementation("androidx.leanback:leanback:1.0.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

    // Image Loading
    implementation("io.coil-kt:coil:2.5.0")

    // Palette (dynamic colors from images)
    implementation("androidx.palette:palette-ktx:1.0.0")

    // WebView
    implementation("androidx.webkit:webkit:1.10.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Security (encrypted preferences for PIN)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // HTML Parser (for stream extraction)
    implementation("org.jsoup:jsoup:1.17.2")

    // Media3 (ExoPlayer) - Native video player
    val media3Version = "1.2.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-exoplayer-hls:$media3Version") // HLS streaming support
    implementation("androidx.media3:media3-ui:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")
}
