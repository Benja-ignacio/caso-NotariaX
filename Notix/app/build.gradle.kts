import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
}


android {
    namespace = "com.duoc.notix"

    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.duoc.notix"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(
                System.getenv("NOTIX_KEYSTORE") ?: "notix-release.jks"
            )
            storePassword = System.getenv("NOTIX_STORE_PASSWORD")
            keyAlias = "notix"
            keyPassword = System.getenv("NOTIX_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")

            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}


dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.withType<Test>().configureEach {
    extensions.configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Genera reporte de cobertura de las pruebas unitarias"

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val kotlinClasses = layout.buildDirectory.dir(
        "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes"
    )
    classDirectories.setFrom(
        fileTree(kotlinClasses) {
            include(
                "com/duoc/notix/viewmodel/**",
                "com/duoc/notix/repository/**",
                "com/duoc/notix/data/repository/**",
                "com/duoc/notix/data/validation/**",
                "com/duoc/notix/data/remote/**",
                "com/duoc/notix/util/**"
            )
            exclude(
                "**/*Factory*",
                "**/*Provider*",
                "**/*Companion*",
                "**/RetrofitProviders*",
                "**/ApiConfig*",
                "**/ArchivoUtil*",
                "**/NotificacionUtil*",
                "**/BuildConfig*",
                "**/R$*.class"
            )
        }
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory.dir("jacoco")) {
            include("*.exec")
        }
    )
}

tasks.register<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Verifica que la cobertura de línea sea al menos 80%"

    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }

    val kotlinClasses = layout.buildDirectory.dir(
        "intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes"
    )
    classDirectories.setFrom(
        fileTree(kotlinClasses) {
            include(
                "com/duoc/notix/viewmodel/**",
                "com/duoc/notix/repository/**",
                "com/duoc/notix/data/repository/**",
                "com/duoc/notix/data/validation/**",
                "com/duoc/notix/data/remote/**",
                "com/duoc/notix/util/**"
            )
            exclude(
                "**/*Factory*",
                "**/*Provider*",
                "**/*Companion*",
                "**/RetrofitProviders*",
                "**/ApiConfig*",
                "**/ArchivoUtil*",
                "**/NotificacionUtil*",
                "**/BuildConfig*",
                "**/R$*.class"
            )
        }
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory.dir("jacoco")) {
            include("*.exec")
        }
    )
}