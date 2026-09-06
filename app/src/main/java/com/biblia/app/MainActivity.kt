package com.biblia.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.biblia.app.data.BibleBook
import com.biblia.app.data.BibleVerse
import com.biblia.app.data.PlanPacing
import com.biblia.app.data.ReadingPlan
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.ReadingPlanViewModel
import com.biblia.app.ui.screens.ChaptersScreen
import com.biblia.app.ui.screens.HomeScreen
import com.biblia.app.ui.screens.OnboardingScreen
import com.biblia.app.ui.screens.PlanDayScreen
import com.biblia.app.ui.screens.ReaderScreen
import com.biblia.app.ui.screens.ReadingPlansScreen
import com.biblia.app.ui.screens.SavedScreen
import com.biblia.app.ui.screens.SearchScreen
import com.biblia.app.ui.screens.SettingsSheetContent
import com.biblia.app.ui.screens.SplashScreen
import com.biblia.app.ui.theme.BibliaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * App shell for Biblia Takatifu - a Bible reader only now; the liturgical calendar/lectionary
 * feature was removed entirely (see README).
 *
 * Bottom-nav has three root tabs (Home, Mipango/Reading Plans, Yaliyohifadhiwa/Saved) - Search
 * and Settings are reached via icon buttons instead: Search pushes a normal back-stack screen,
 * Settings opens as a ModalBottomSheet ([showSettingsSheet]) rather than navigating anywhere.
 * "chapters", "reader", "search" and "plan_day" are non-root, pushed routes: since routes are
 * plain strings with no argument-passing of their own, what they're showing is tracked as
 * separate Activity-level state ([selectedBook]/[selectedChapterNum]/[selectedPlan]) that each
 * navigate-to call updates just before pushing the route.
 *
 * Hardware/gesture back is intentionally NOT a plain stack-pop (that's what the in-app top-bar
 * back arrows do, via [goBack]): from anywhere else it jumps straight to Home, and from Home
 * itself it's double-press-to-exit (a Toast on the first press, [EXIT_PRESS_WINDOW_MS] to
 * press again before it resets) rather than exiting immediately.
 *
 * Transitions are a plain crossfade everywhere - no slide/spring/scale motion, matching the
 * flat, content-first design used throughout (see ui/theme and ui/components).
 */
private val ROOT_ROUTES = setOf("home", "reading_plans", "saved")
private const val EXIT_PRESS_WINDOW_MS = 2000L

private const val PREFS_NAME = "biblia_prefs"
private const val KEY_ONBOARDED = "has_onboarded"

class MainActivity : ComponentActivity() {
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    setContent {
      val viewModel: BibleViewModel = viewModel()
      val themeMode by viewModel.themeMode.collectAsState()

      BibliaTheme(themeMode = themeMode) {
        // Real back-stack: navigate() pushes/roots; goBack() pops one level (used by in-app
        // top-bar back arrows). rememberSaveable (not plain remember) so that if Android
        // reclaims this process while backgrounded, the restored Activity lands back on the
        // same screen instead of cold-starting at the splash screen again.
        val backStack = rememberSaveable(
          saver = listSaver<SnapshotStateList<String>, String>(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
          )
        ) {
          mutableStateListOf("splash")
        }
        val currentScreen = backStack.last()
        val readingPlanViewModel: ReadingPlanViewModel = viewModel()

        var selectedBook by remember { mutableStateOf<BibleBook?>(null) }
        var selectedChapterNum by remember { mutableIntStateOf(1) }
        var selectedPlan by remember { mutableStateOf<ReadingPlan?>(null) }
        var selectedPacing by remember { mutableStateOf(PlanPacing.ONE_YEAR) }
        var showSettingsSheet by remember { mutableStateOf(false) }
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

        /** Shared by the Verse of Day card and reading-plan chapters: jump straight to a
         * specific book+chapter, resolving the book asynchronously like openVerse() does. */
        fun openBookChapter(bookId: Int, chapterNum: Int) {
          selectedChapterNum = chapterNum
          if (selectedBook?.id != bookId) {
            pendingVerseBookId = bookId
          }
          navigate("reader")
        }

        // Hardware/gesture back: anywhere but Home jumps straight to Home; on Home it's
        // double-press-to-exit instead of exiting on the first press.
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var awaitingExitConfirm by remember { mutableStateOf(false) }

        BackHandler(enabled = true) {
          if (currentScreen != "home") {
            backStack.clear()
            backStack.add("home")
          } else if (awaitingExitConfirm) {
            (context as? Activity)?.finish()
          } else {
            awaitingExitConfirm = true
            Toast.makeText(context, "Bonyeza tena kutoka", Toast.LENGTH_SHORT).show()
            scope.launch {
              delay(EXIT_PRESS_WINDOW_MS)
              awaitingExitConfirm = false
            }
          }
        }

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
                onContinueReading = {
                  val state = viewModel.readingState.value
                  scope.launch {
                    val book = viewModel.getBook(state.lastBookId)
                    if (book != null) {
                      selectedBook = book
                      selectedChapterNum = state.lastChapterNum
                      navigate("reader")
                    }
                  }
                },
                onOpenVerseOfDay = { bookId, chapterNum -> openBookChapter(bookId, chapterNum) },
                onOpenReadingPlans = { navigate("reading_plans") },
                onOpenSearch = { navigate("search") },
                onOpenSettings = { showSettingsSheet = true },
              )
              "reading_plans" -> ReadingPlansScreen(
                viewModel = readingPlanViewModel,
                onNavigate = ::navigate,
                onOpenPlan = { planId ->
                  val plan = readingPlanViewModel.plans.value.firstOrNull { it.id == planId }
                  if (plan != null) {
                    selectedPlan = plan
                    selectedPacing = readingPlanViewModel.pacingByPlan.value[planId] ?: PlanPacing.ONE_YEAR
                    navigate("plan_day")
                  }
                },
              )
              "plan_day" -> selectedPlan?.let { plan ->
                PlanDayScreen(
                  viewModel = readingPlanViewModel,
                  plan = plan,
                  pacing = selectedPacing,
                  onBack = ::goBack,
                  onOpenChapter = { bookId, chapterNum -> openBookChapter(bookId, chapterNum) },
                )
              }
              "saved" -> SavedScreen(
                viewModel = viewModel,
                onNavigate = ::navigate,
                onOpenVerse = ::openVerse,
              )
              "chapters" -> selectedBook?.let { book ->
                ChaptersScreen(
                  book = book,
                  currentChapter = if (book.id == viewModel.readingState.value.lastBookId) viewModel.readingState.value.lastChapterNum else null,
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
                  onSettings = { showSettingsSheet = true },
                )
              }
              "search" -> SearchScreen(
                viewModel = viewModel,
                onBack = ::goBack,
                onOpenVerse = ::openVerse,
              )
            }
          }

          if (showSettingsSheet) {
            ModalBottomSheet(
              onDismissRequest = { showSettingsSheet = false },
              sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            ) {
              SettingsSheetContent(viewModel = viewModel)
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
