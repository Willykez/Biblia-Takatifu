package com.biblia.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biblia.app.data.BibleBook
import com.biblia.app.data.BibleVerse
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.LiturgicalViewModel
import com.biblia.app.ui.screens.CalendarScreen
import com.biblia.app.ui.screens.ChaptersScreen
import com.biblia.app.ui.screens.HomeScreen
import com.biblia.app.ui.screens.OnboardingScreen
import com.biblia.app.ui.screens.ReaderScreen
import com.biblia.app.ui.screens.ReadingsScreen
import com.biblia.app.ui.screens.SavedScreen
import com.biblia.app.ui.screens.SearchScreen
import com.biblia.app.ui.screens.SettingsScreen
import com.biblia.app.ui.screens.SplashScreen
import com.biblia.app.ui.theme.BibliaTheme
import java.time.LocalDate

/**
 * App shell for Biblia Takatifu.
 *
 * A bottom-nav with four root tabs (Home, Search, Saved, Settings), a real push/pop
 * back-stack for everything else. "chapters", "reader" and "readings" are non-root, pushed
 * routes: since routes are plain strings with no argument-passing of their own, what they're
 * showing is tracked as separate Activity-level state ([selectedBook]/[selectedChapterNum]/
 * [selectedReadingsDate]) that each navigate-to call updates just before pushing the route.
 *
 * Transitions are a plain crossfade everywhere - no slide/spring/scale motion, matching the
 * flat, content-first design used throughout (see ui/theme and ui/components).
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
      val viewModel: BibleViewModel = viewModel()
      val themeMode by viewModel.themeMode.collectAsState()

      BibliaTheme(themeMode = themeMode) {
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
        val liturgicalViewModel: LiturgicalViewModel = viewModel()

        var selectedBook by remember { mutableStateOf<BibleBook?>(null) }
        var selectedChapterNum by remember { mutableIntStateOf(1) }
        var selectedReadingsDate by remember { mutableStateOf(LocalDate.now()) }
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
            pendingVerseBookId = verse.bookId
          }
          navigate("reader")
        }

        fun openDate(date: LocalDate) {
          selectedReadingsDate = date
          navigate("readings")
        }

        BackHandler(enabled = backStack.size > 1) { goBack() }

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
          AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
              (fadeIn(tween(180))) togetherWith (fadeOut(tween(120)))
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
                onOpenCalendar = { navigate("calendar") },
              )
              "calendar" -> CalendarScreen(
                viewModel = liturgicalViewModel,
                onNavigate = ::navigate,
                onOpenDate = ::openDate,
                onBack = { navigate("home") },
              )
              "readings" -> ReadingsScreen(
                viewModel = liturgicalViewModel,
                date = selectedReadingsDate,
                onBack = ::goBack,
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
        }
      }
    }
  }

  companion object {
    fun newIntent(context: Context): Intent =
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
  }
}
