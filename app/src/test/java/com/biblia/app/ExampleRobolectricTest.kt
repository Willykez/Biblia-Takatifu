package com.biblia.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Pinned to 34 rather than this project's compileSdk (36): Robolectric's bundled SDK
// catalog lags behind the newest Android release, and 4.16.1 doesn't have a 36 entry yet
// (confirmed by a real CI run - see UnsupportedOperationException at DefaultSdkProvider).
// This is independent of the app's compileSdk/targetSdk; Robolectric just needs an API
// level it has shadows for, anywhere between minSdk (24) and targetSdk (36).
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("App Design", appName)
  }
}
