buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://chaquo.com/maven") }
    }

    dependencies {
        classpath("com.chaquo.python:gradle:${Versions.Essential.Python}")
        classpath("com.android.tools.build:gradle:${Versions.Essential.Gradle}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Essential.Kotlin}")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri( "https://chaquo.com/maven") }
        maven { url = uri("https://oss.jfrog.org/libs-snapshot") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
