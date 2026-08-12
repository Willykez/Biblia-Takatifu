package com.biblia.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biblia.app.data.BibleBook
import com.biblia.app.data.BibleVerse
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.common.SparkPredictiveBackHandler
import com.biblia.app.ui.screens.ChaptersScreen
import com.biblia.app.ui.screens.HomeScreen
import com.biblia.app.ui.screens.OnboardingScreen
import com.biblia.app.ui.screens.ReaderScreen
import com.biblia.app.ui.screens.SavedScreen
import com.biblia.app.ui.screens.SearchScreen
import com.biblia.app.ui.screens.SettingsScreen
import com.biblia.app.ui.screens.SplashScreen
import com.biblia.app.ui.theme.LocalReducedMotion
import com.biblia.app.ui.theme.MyApplicationTheme
import com.biblia.app.ui.theme.reducedMotionAwareSpec

/**
 * App shell for Biblia Takatifu.
 *
 * Same navigation shell as the design framework this was built from - a bottom-nav with
 * four root tabs (Home, Search, Saved, Settings), a real push/pop back-stack for everything
 * else, and identical transition/back-gesture behavior. "chapters" and "reader" are
 * non-root, pushed routes: since routes are plain strings with no argument-passing of their
 * own, the book/chapter being viewed is tracked as separate Activity-level state
 * ([selectedBook]/[selectedChapterNum]) that each navigate-to-reader call updates just
 * before pushing the route.
 */
private val ROOT_ROUTES = setOf("home", "search", "saved", "settings")

private const val PREFS_NAME = "biblia_prefs"
private const val KEY_ONBOARDED = "has_onboarded"

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    setContent {
      MyApplicationTheme {
        // Real back-stack: every navigate() call pushes, hardware/gesture back pops.
        // rememberSaveable (not plain remember) so that if Android reclaims this process
        // while backgrounded, the restored Activity lands back on the same screen instead
        // of cold-starting at the splash screen again.
        val backStack = rememberSaveable(
          saver = listSaver<SnapshotStateList<String>, String>(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
          )
        ) {
          mutableStateListOf("splash")
        }
        val currentScreen = backStack.last()
        val viewModel: BibleViewModel = viewModel()

        // Which book/chapter "chapters" and "reader" are currently showing - plain
        // Activity-level state since the string-based backStack has no slot for arguments.
        var selectedBook by remember { mutableStateOf<BibleBook?>(null) }
        var selectedChapterNum by remember { mutableIntStateOf(1) }
        // Set by openVerse() when jumping in from Search/Saved and the target book isn't
        // already loaded; the LaunchedEffect below resolves it and completes the navigation.
        var pendingVerseBookId by remember { mutableStateOf<Int?>(null) }

        LaunchedEffect(pendingVerseBookId) {
          val bookId = pendingVerseBookId ?: return@LaunchedEffect
          viewModel.getBook(bookId)?.let { book -> selectedBook = book }
          pendingVerseBookId = null
        }

        fun goBack() {
          if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
        }

        fun navigate(route: String) {
          when {
            route == currentScreen -> Unit
            // Tapping a "back" affordance that leads to the screen already just under
            // the top of the stack should pop, not push a duplicate entry.
            backStack.size >= 2 && backStack[backStack.size - 2] == route -> goBack()
            route in ROOT_ROUTES -> {
              backStack.clear()
              backStack.add(route)
            }
            else -> backStack.add(route)
          }
        }

        fun openBook(book: BibleBook) {
          selectedBook = book
          navigate("chapters")
        }

        fun openChapter(chapterNum: Int) {
          selectedChapterNum = chapterNum
          navigate("reader")
        }

        fun openVerse(verse: BibleVerse) {
          selectedChapterNum = verse.chapterNum
          if (selectedBook?.id != verse.bookId) {
            // Jumped here from Search/Saved rather than via the book/chapter pickers - kick
            // off async book resolution (the LaunchedEffect above); "reader" simply renders
            // nothing until selectedBook updates a frame or two later.
            pendingVerseBookId = verse.bookId
          }
          navigate("reader")
        }

        // System/gesture back: pop our own stack instead of the default (which would
        // otherwise skip screens or exit unexpectedly). Using the predictive variant so
        // Android 14+ shows the gesture back-preview instead of a hard cut.
        SparkPredictiveBackHandler(enabled = backStack.size > 1) { goBack() }

        // Direction-aware transition: pushing a screen slides the new one in from
        // the right (old screen drifts left), popping slides in from the left: a
        // real "forward/back" feel instead of a flat crossfade for every change.
        // Switching between bottom-nav tabs (root routes) just fades - no
        // directionality implied since tabs are peers, not a stack relationship.
        // All of it collapses to an instant cut when the user has reduced motion on.
        var previousStackSize by remember { mutableIntStateOf(backStack.size) }
        val isRootSwitch = currentScreen in ROOT_ROUTES && backStack.size == 1
        val isPush = !isRootSwitch && backStack.size > previousStackSize
        SideEffect { previousStackSize = backStack.size }
        val reducedMotion = LocalReducedMotion.current
        val rootFadeInSpec: androidx.compose.animation.core.FiniteAnimationSpec<Float> =
          reducedMotionAwareSpec(tween(220, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        val rootFadeOutSpec: androidx.compose.animation.core.FiniteAnimationSpec<Float> =
          reducedMotionAwareSpec(tween(140, easing = androidx.compose.animation.core.LinearOutSlowInEasing))
        val rootScaleSpec: androidx.compose.animation.core.FiniteAnimationSpec<Float> =
          reducedMotionAwareSpec(
            androidx.compose.animation.core.spring(
              dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
              stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
            )
          )
        val slideSpec: androidx.compose.animation.core.FiniteAnimationSpec<androidx.compose.ui.unit.IntOffset> =
          reducedMotionAwareSpec(
            androidx.compose.animation.core.spring(
              dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
              stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
            )
          )
        val enterFadeSpec: androidx.compose.animation.core.FiniteAnimationSpec<Float> =
          reducedMotionAwareSpec(tween(260, easing = androidx.compose.animation.core.FastOutSlowInEasing))
        val exitFadeSpec: androidx.compose.animation.core.FiniteAnimationSpec<Float> =
          reducedMotionAwareSpec(tween(180, easing = androidx.compose.animation.core.LinearOutSlowInEasing))

        Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
          AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
              when {
                reducedMotion ->
                  EnterTransition.None togetherWith ExitTransition.None
                isRootSwitch ->
                  (fadeIn(rootFadeInSpec) + scaleIn(rootScaleSpec, initialScale = 0.98f)) togetherWith
                    (fadeOut(rootFadeOutSpec) + scaleOut(rootScaleSpec, targetScale = 1.01f))
                isPush ->
                  (slideInHorizontally(slideSpec) { it } + fadeIn(enterFadeSpec)) togetherWith
                    (slideOutHorizontally(slideSpec) { -it / 3 } + fadeOut(exitFadeSpec))
                else ->
                  (slideInHorizontally(slideSpec) { -it } + fadeIn(enterFadeSpec)) togetherWith
                    (slideOutHorizontally(slideSpec) { it / 3 } + fadeOut(exitFadeSpec))
              }
            },
            label = "screen_transition",
          ) { screen ->
            when (screen) {
              "splash" -> SplashScreen(
                onFinish = {
                  val nextRoute = if (prefs.getBoolean(KEY_ONBOARDED, false)) "home" else "onboarding"
                  navigate(nextRoute)
                }
              )
              "onboarding" -> OnboardingScreen(
                onGetStarted = {
                  prefs.edit().putBoolean(KEY_ONBOARDED, true).apply()
                  navigate("home")
                }
              )
              "home" -> HomeScreen(
                viewModel = viewModel,
                onNavigate = ::navigate,
                onOpenBook = ::openBook,
              )
              "chapters" -> selectedBook?.let { book ->
                ChaptersScreen(
                  book = book,
                  onBack = ::goBack,
                  onSelectChapter = ::openChapter,
                )
              }
              "reader" -> selectedBook?.let { book ->
                ReaderScreen(
                  viewModel = viewModel,
                  bookId = book.id,
                  bookTitle = book.title,
                  chapterNum = selectedChapterNum,
                  numChapters = book.numChapters,
                  onChapterChange = { selectedChapterNum = it },
                  onBack = ::goBack,
                  onSearch = { navigate("search") },
                  onSettings = { navigate("settings") },
                )
              }
              "search" -> SearchScreen(
                viewModel = viewModel,
                onNavigate = ::navigate,
                onBack = { navigate("home") },
                onOpenVerse = ::openVerse,
              )
              "saved" -> SavedScreen(
                viewModel = viewModel,
                onNavigate = ::navigate,
                onOpenVerse = ::openVerse,
              )
              "settings" -> SettingsScreen(viewModel = viewModel, onNavigate = ::navigate)
            }
          }

          val snackbarHostState = com.biblia.app.ui.theme.LocalSnackbarHostState.current
          SnackbarHost(
            hostState = snackbarHostState,
            modifier = androidx.compose.ui.Modifier
              .align(androidx.compose.ui.Alignment.BottomCenter)
              .padding(bottom = 100.dp)
          )
        }
      }
    }
  }

  companion object {
    /** Plain "bring the app to front" intent. */
    fun newIntent(context: Context): Intent =
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
  }
}
