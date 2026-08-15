plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.roborazzi)
}

android {
  namespace = "com.biblia.app"
  compileSdk { version = release(37) { minorApiLevel = 0 } }

  defaultConfig {
    applicationId = "com.biblia.app"
    minSdk = 24
    targetSdk = 37
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      // Supplied via env vars, never committed. Locally, assembleRelease is a no-op until
      // these are set. In CI: android-ci.yml only ever builds the debug variant (never
      // touches these); android-release.yml sets them - using a real keystore from repo
      // secrets if configured, otherwise an auto-generated throwaway one so the workflow
      // always produces a validly-signed build. See README > Releasing.
      val keystorePath = System.getenv("KEYSTORE_PATH")
      if (keystorePath != null) storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    // Debug builds intentionally do NOT declare a custom signingConfig here: no debug
    // keystore is (or should be) committed to the repo, so debug quietly falls back to
    // AGP's own auto-generated ~/.android/debug.keystore (androiddebugkey/android) -
    // this is what keeps `assembleDebug` reproducible on a clean checkout and in CI.
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // No signingConfig override - see the comment on signingConfigs above.
      applicationIdSuffix = ".debug"
      versionNameSuffix = "-debug"
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    // java.time (LocalDate, YearMonth, DateTimeFormatter...) used throughout the liturgical
    // calendar feature needs this on minSdk 24-25 devices (native support starts at API 26).
    isCoreLibraryDesugaringEnabled = true
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }

  // The bundled Bible database (assets/bible_swahili.sqlite, ~18MB) ships uncompressed so
  // SQLiteDatabase.openDatabase() can read it directly without an extra extraction step.
  androidResources {
    noCompress += "sqlite"
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.animation)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.material.kolor)
  implementation(libs.androidx.graphics.shapes)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  coreLibraryDesugaring(libs.desugar.jdk.libs)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
}
