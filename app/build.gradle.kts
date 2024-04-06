import com.android.build.gradle.internal.tasks.UnstrippedLibs.reset






plugins {
    id("com.android.application")
}


android {
    namespace = "android21ktpm3.group07.androidgallery"
    compileSdk = 34
    configurations.all {
        resolutionStrategy.force ("com.google.code.findbugs:jsr305:3.0.2")
    }

    defaultConfig {
        applicationId = "android21ktpm3.group07.androidgallery"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}



dependencies {

    implementation ("com.github.yellowcath:PhotoMovie:1.6.4")
    implementation("org.jcodec:jcodec:0.2.5")

    implementation("org.jcodec:jcodec-android:0.2.5")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation ("androidx.activity:activity:1.8.2")
    implementation ("androidx.media3:media3-exoplayer:1.3.0")
    implementation ("androidx.media3:media3-exoplayer-dash:1.3.0")
    implementation ("androidx.media3:media3-ui:1.3.0")
    implementation ("com.google.android.exoplayer:exoplayer-core:2.19.1")
    implementation ("de.hdodenhof:circleimageview:2.1.0")
  //  implementation (project(":PhotoPicker") )
    testImplementation ("com.android.support.test:runner:1.0.2")
    testImplementation ("junit:junit:4.13.2")
    testImplementation ("androidx.test:core:1.5.0")
    implementation ("com.squareup.picasso:picasso:2.71828")

    testImplementation ("org.mockito:mockito-core:3.5.13")
    implementation ("com.github.rafjp:Photopicker:0.2.9")
    implementation("com.google.modernstorage:modernstorage-photopicker:1.0.0-alpha06")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.21")
    implementation ("com.github.denzcoskun:ImageSlideshow:0.1.2")




}