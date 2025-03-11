plugins {
    alias(libs.plugins.androidApplication) // Corrected plugin name
    alias(libs.plugins.kotlinAndroid) // Corrected plugin name
    alias(libs.plugins.googleServices) // Corrected plugin name
}

android {
    namespace = "com.example.uni"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.uni"
        minSdk = 26
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidxCoreKtx) // Corrected library name
    implementation(libs.androidxAppcompat) // Corrected library name
    implementation(libs.material)
    implementation(libs.androidxConstraintlayout) // Corrected library name
    implementation(libs.androidxLifecycleLivedataKtx) // Corrected library name
    implementation(libs.androidxLifecycleViewmodelKtx) // Corrected library name
    implementation(libs.androidxNavigationFragmentKtx) // Corrected library name
    implementation(libs.androidxNavigationUiKtx) // Corrected library name
    implementation(libs.hdodenhofCircleimageview) // Corrected library name
    implementation(libs.androidxActivity) // Corrected library name
    implementation(libs.firebaseMessagingKtx) // Corrected library name
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidxJunit) // Corrected library name
    androidTestImplementation(libs.androidxEspressoCore) // Corrected library name
    implementation(libs.firebaseStorage) // Corrected library name
    implementation (libs.picasso)


    // Other dependencies



    // Firebase:
    implementation(platform(libs.firebaseBom)) // Corrected library name
    implementation(libs.firebaseAnalytics) // Corrected library name

    // Firebase AuthUI
    implementation (libs.firebaseUiAuth) // Corrected library name

    //Realtime DB:
    implementation(libs.firebaseDatabase) // Corrected library name

    // push notifications
    implementation(libs.firebaseMessaging) // Corrected library name

    //Glide
    implementation(libs.glide)


    implementation(libs.androidImageCropper) // Corrected library name
    implementation (libs.androidxSwiperefreshlayout)


}