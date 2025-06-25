import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import com.google.gms.googleservices.GoogleServicesPlugin.GoogleServicesPluginConfig
import java.text.SimpleDateFormat


plugins {
    alias(libs.plugins.homeassistant.android.application)
    alias(libs.plugins.homeassistant.android.flavor)
    alias(libs.plugins.firebase.appdistribution)
    alias(libs.plugins.google.services)
    alias(libs.plugins.homeassistant.android.dependencies)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.screenshot)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    useLibrary("android.car")

    defaultConfig {
        applicationId = "io.homeassistant.companion.android"
        minSdk = libs.versions.androidSdk.min.get().toInt()
        targetSdk = libs.versions.androidSdk.target.get().toInt()

        versionName = getVersionName()
        versionCode = getVersionCode()

//        manifestPlaceholders["sentryRelease"] = "$applicationId@$versionName"
//        manifestPlaceholders["sentryDsn"] = System.getenv("SENTRY_DSN") ?: ""

        bundle {
            language {
                // We want to keep the translations in the final AAB for all the language
                enableSplit = false
            }
        }
    }

    lint {
        // Until we fully migrate to Material3 this lint issue is too verbose https://github.com/home-assistant/android/issues/5420
        disable += listOf("UsingMaterialAndMaterial3Libraries")
    }

    experimentalProperties["android.experimental.enableScreenshotTest"] = true

    screenshotTests {
        imageDifferenceThreshold = 0.00025f // 0.025%
    }

    firebaseAppDistribution {
        serviceCredentialsFile = "firebaseAppDistributionServiceCredentialsFile.json"
        releaseNotesFile = "./app/build/outputs/changelogBeta"
        groups = "continuous-deployment"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "release_keystore.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEYSTORE_ALIAS") ?: ""
            keyPassword = System.getenv("KEYSTORE_ALIAS_PASSWORD") ?: ""
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        named("debug").configure {
            applicationIdSuffix = ".debug"
        }
        named("release").configure {
            isDebuggable = false
            isJniDebuggable = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    flavorDimensions.add("version")
    productFlavors {
        create("minimal") {
            applicationIdSuffix = ".minimal"
            versionNameSuffix = "-minimal"
        }
        create("full") {
            applicationIdSuffix = ""
            versionNameSuffix = "-full"
        }

        // Generate a list of application ids into BuildConfig
        val values = productFlavors.joinToString {
            "\"${it.applicationId ?: defaultConfig.applicationId}${it.applicationIdSuffix}\""
        }

        defaultConfig.buildConfigField("String[]", "APPLICATION_IDS", "{$values}")
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }

    lint {
        abortOnError = false
        disable += "MissingTranslation"
    }
}

dependencies {
    implementation(project(":common"))

    coreLibraryDesugaring(libs.tools.desugar.jdk)

    implementation(libs.blurView)
    implementation(libs.androidx.health.connect.client)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    "fullImplementation"(libs.kotlinx.coroutines.play.services)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.preference.ktx)
    implementation(libs.material)
    implementation(libs.fragment.ktx)

    implementation(libs.jackson.module.kotlin)
    implementation(libs.okhttp)
    implementation(libs.picasso)

    "fullImplementation"(libs.play.services.location)
    "fullImplementation"(libs.play.services.home)
    "fullImplementation"(libs.play.services.threadnetwork)
    "fullImplementation"(platform(libs.firebase.bom))
    "fullImplementation"(libs.firebase.messaging)
    "fullImplementation"(libs.sentry.android)
    "fullImplementation"(libs.play.services.wearable)
    "fullImplementation"(libs.wear.remote.interactions)
    "fullImplementation"(libs.crashlytics)
    "fullImplementation"(libs.analytics)
    "fullImplementation"(libs.amap)

    implementation(libs.biometric)
    implementation(libs.webkit)

    implementation(libs.bundles.media3)
    "fullImplementation"(libs.media3.datasource.cronet)
//    "minimalImplementation"(libs.media3.datasource.cronet) {
//        exclude(group = "com.google.android.gms", module = "play-services-cronet")
//    }
//    "minimalImplementation"(libs.cronet.embedded)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.animation)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.uiTooling)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.iconics.core)
    implementation(libs.iconics.compose)
    implementation(libs.community.material.typeface)

    implementation(libs.bundles.paging)

    implementation(libs.reorderable)
    implementation(libs.changeLog)

    implementation(libs.zxing)

    implementation(libs.car.core)
    "fullImplementation"(libs.car.projected)

    screenshotTestImplementation(libs.compose.uiTooling)
}

// Disable to fix memory leak and be compatible with the configuration cache.
configure<GoogleServicesPluginConfig> {
    disableVersionCheck = true
}

fun getVersionCode(): Int {
    val time = System.currentTimeMillis()
    return (time / 1000).toInt()
}

fun getVersionName(): String {
    return "v" + SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
}
