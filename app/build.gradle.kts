import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
val secretsProperties = Properties()
val secretsFile = rootProject.file("app/secrets.properties")
if (secretsFile.exists()) {
    secretsProperties.load(secretsFile.inputStream())
}

android {
    namespace = "com.example.mymindv2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mymindv2"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["auth0Domain"] = "@string/com_auth0_domain"
        manifestPlaceholders["auth0Scheme"] = "@string/com_auth0_scheme"
        testInstrumentationRunner = "androidx.test.arunner.AndroidJUnitRunner"
        buildConfigField("String", "MGMT_CLIENT_ID", "\"${secretsProperties["MGMT_CLIENT_ID"]}\"")
        buildConfigField("String", "MGMT_CLIENT_SECRET", "\"${secretsProperties["MGMT_CLIENT_SECRET"]}\"")
        buildConfigField("String", "AUTH_CLIENT_ID", "\"${secretsProperties["AUTH_CLIENT_ID"]}\"")
        buildConfigField("String", "AUTH_CLIENT_SECRET", "\"${secretsProperties["AUTH_CLIENT_SECRET"]}\"")
        buildConfigField("String", "AUTH_DOMAIN", "\"${secretsProperties["AUTH_DOMAIN"]}\"")
        buildConfigField("String", "AUTH_AUDIENCE_BACKEND", "\"${secretsProperties["AUTH_AUDIENCE_BACKEND"]}\"")
        buildConfigField("String", "AUTH_AUDIENCE_MGMT", "\"${secretsProperties["AUTH_AUDIENCE_MGMT"]}\"")
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.8")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.8")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("com.auth0.android:auth0:2.+")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
    testImplementation("com.squareup.retrofit2:retrofit-mock:2.9.0")
}
