// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    extra["minSdkVersion"] = 21
    extra["compileSdkVersion"] = 33
    extra["targetSdkVersion"] = 33

    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.0.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

tasks.register("clean", Delete::class) {
    delete(buildDir)
}