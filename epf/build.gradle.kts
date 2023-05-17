plugins {
    id("com.android.library")
    id("maven-publish")
}

android {
    namespace = "com.daasuu.epf"

    setCompileSdkVersion(property("compileSdkVersion") as Int)
    defaultConfig {
        minSdk = property("minSdkVersion") as Int
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    api("androidx.media3:media3-exoplayer:1.0.2")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.daasuu.epf"
            artifactId = "exoplayerfilter"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
