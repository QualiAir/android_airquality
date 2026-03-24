plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.concordia.qualiair"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.concordia.qualiair"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "MQTT_BROKER", "\"tcp://test.mosquitto.org:1883\"")
        buildConfigField("String", "MQTT_USERNAME", "\"\"")
        buildConfigField("String", "MQTT_PASSWORD", "\"\"")
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
    buildFeatures {
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.github.Gruzer:simple-gauge-android:0.3.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1") {
        exclude(group = "org.checkerframework", module = "checker")
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    implementation(files("libs/esp-idf-provisioning-android-lib-2.1.0.aar"))
    implementation(libs.tink.android)
    implementation(libs.protobuf.javalite)
    implementation("org.greenrobot:eventbus:3.3.1")
}
