/*
 * GitMessengerBot © 2021 지성빈 & 구환. all rights reserved.
 * GitMessengerBot license is under the GPL-3.0.
 *
 * [build.gradle.kts] created by Ji Sungbin on 21. 8. 2. 오후 7:28.
 *
 * Please see: https://github.com/GitMessengerBot/GitMessengerBot-Android/blob/master/LICENSE.
 */

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = Application.compileSdk

    defaultConfig {
        minSdk = Application.minSdk
        targetSdk = Application.targetSdk
        multiDexEnabled = true
    }

    sourceSets {
        getByName("main").run {
            java.srcDirs("src/main/kotlin")
        }
    }

    compileOptions {
        sourceCompatibility = Application.sourceCompat
        targetCompatibility = Application.targetCompat
    }

    kotlinOptions {
        jvmTarget = Application.jvmTarget
    }
}

dependencies {
    implementation(Dependencies.jsoup)
    implementation(Dependencies.livedata)
    implementation(Dependencies.browser)

    Dependencies.jackson.forEach(::implementation)
    Dependencies.essential.forEach(::api)
}
