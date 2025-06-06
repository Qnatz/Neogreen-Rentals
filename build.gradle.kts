// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://jitpack.io") // Correct way to specify a custom repository in Kotlin DSL
    }
}

    
    
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
        // Add Hilt plugin classpath
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.52")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3") 
    }
}

plugins {
    id("com.android.application") version "8.1.1" apply false
    id("com.android.library") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
