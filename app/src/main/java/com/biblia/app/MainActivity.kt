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
import com.biblia.app.ui.BibleViewModel
import com.biblia.app.ui.LiturgicalViewModel
import com.biblia.app.ui.screens.CalendarScreen
import com.biblia.app.ui.screens.ChaptersScreen
import com.biblia.app.ui.screens.HomeScreen
import com.biblia.app.ui.screens.OnboardingScreen
import com.biblia.app.ui.screens.ReaderScreen
import com.biblia.app.ui.screens.ReadingsScreen
import com.biblia.app.ui.screens.SearchScreen
import com.biblia.app.ui.screens.SettingsSheetContent
import com.biblia.app.ui.screens.SplashScreen
import com.biblia.app.ui.theme.BibliaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * App shell for Biblia Takatifu.
 *
 * Bottom-nav has two root tabs (Home, Calendar) - Search and Settings are reached via icon
 * buttons instead: Search pushes a normal back-stack screen, Settings opens as a
 * ModalBottomSheet ([showSettingsSheet]) rather than navigating anywhere. "chapters",
 * "reader", "readings" and "search" are non-root, pushed routes: since routes are plain
 * strings with no argument-passing of their own, what they're showing is tracked as separate
 * Activity-level state ([selectedBook]/[selectedChapterNum]/[selectedReadingsDate]) that each
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
private val ROOT_ROUTES = setOf("home", "calendar")
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

            // Collect StateFlow values as Compose state.
            // Do not access viewModel.readingState.value directly
            // from the composition.
            val themeMode by viewModel.themeMode.collectAsState()
            val readingState by viewModel.readingState.collectAsState()

            BibliaTheme(themeMode = themeMode) {

                // Real back-stack: navigate() pushes/roots; goBack() pops one level
                // (used by in-app top-bar back arrows). rememberSaveable (not plain
                // remember) so that if Android reclaims this process while backgrounded,
                // the restored Activity lands back on the same screen instead of
                // cold-starting at the splash screen again.
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

                var selectedBook by remember {
                    mutableStateOf<BibleBook?>(null)
                }

                var selectedChapterNum by remember {
                    mutableIntStateOf(1)
                }

                var selectedReadingsDate by remember {
                    mutableStateOf(LocalDate.now())
                }

                var showSettingsSheet by remember {
                    mutableStateOf(false)
                }

                // Set by openVerse() when jumping in from Search and the target book
                // isn't already loaded; the LaunchedEffect below resolves it and
                // completes the navigation.
                var pendingVerseBookId by remember {
                    mutableStateOf<Int?>(null)
                }

                LaunchedEffect(pendingVerseBookId) {
                    val bookId = pendingVerseBookId ?: return@LaunchedEffect

                    viewModel.getBook(bookId)?.let { book ->
                        selectedBook = book
                    }

                    pendingVerseBookId = null
                }

                fun goBack() {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.size - 1)
                    }
                }

                fun navigate(route: String) {
                    when {
                        route == currentScreen -> Unit

                        backStack.size >= 2 &&
                            backStack[backStack.size - 2] == route -> {
                            goBack()
                        }

                        route in ROOT_ROUTES -> {
                            backStack.clear()
                            backStack.add(route)
                        }

                        else -> {
                            backStack.add(route)
                        }
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

                // Hardware/gesture back:
                // anywhere but Home jumps straight to Home;
                // on Home it's double-press-to-exit instead of exiting
                // on the first press.
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                var awaitingExitConfirm by remember {
                    mutableStateOf(false)
                }

                BackHandler(enabled = true) {
                    if (currentScreen != "home") {
                        backStack.clear()
                        backStack.add("home")
                    } else if (awaitingExitConfirm) {
                        (context as? Activity)?.finish()
                    } else {
                        awaitingExitConfirm = true

                        Toast.makeText(
                            context,
                            "Bonyeza tena kutoka",
                            Toast.LENGTH_SHORT
                        ).show()

                        scope.launch {
                            delay(EXIT_PRESS_WINDOW_MS)
                            awaitingExitConfirm = false
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(tween(180)) togetherWith
                                fadeOut(tween(120))
                        },
                        label = "screen_transition",
                    ) { screen ->

                        when (screen) {

                            "splash" -> SplashScreen(
                                onFinish = {
                                    val nextRoute =
                                        if (prefs.getBoolean(KEY_ONBOARDED, false)) {
                                            "home"
                                        } else {
                                            "onboarding"
                                        }

                                    navigate(nextRoute)
                                }
                            )

                            "onboarding" -> OnboardingScreen(
                                onGetStarted = {
                                    prefs.edit()
                                        .putBoolean(KEY_ONBOARDED, true)
                                        .apply()

                                    navigate("home")
                                }
                            )

                            "home" -> HomeScreen(
                                viewModel = viewModel,

                                onNavigate = ::navigate,

                                onOpenBook = ::openBook,

                                onContinueReading = {
                                    // This is inside an event callback, not directly
                                    // in composition, so reading StateFlow.value here
                                    // is safe.
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

                                onOpenSearch = {
                                    navigate("search")
                                },

                                onOpenSettings = {
                                    showSettingsSheet = true
                                },
                            )

                            "calendar" -> CalendarScreen(
                                viewModel = liturgicalViewModel,

                                onNavigate = ::navigate,

                                onOpenDate = ::openDate,
                            )

                            "readings" -> ReadingsScreen(
                                viewModel = liturgicalViewModel,

                                date = selectedReadingsDate,

                                onBack = ::goBack,

                                onPreviousDate = {
                                    selectedReadingsDate =
                                        selectedReadingsDate.minusDays(1)
                                },

                                onNextDate = {
                                    selectedReadingsDate =
                                        selectedReadingsDate.plusDays(1)
                                },

                                onOpenCalendar = {
                                    navigate("calendar")
                                },
                            )

                            "chapters" -> selectedBook?.let { book ->
                                ChaptersScreen(
                                    book = book,

                                    // FIX:
                                    // Use the collected Compose state instead of
                                    // reading viewModel.readingState.value directly
                                    // during composition.
                                    currentChapter =
                                        if (book.id == readingState.lastBookId) {
                                            readingState.lastChapterNum
                                        } else {
                                            null
                                        },

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

                                    onChapterChange = {
                                        selectedChapterNum = it
                                    },

                                    onBack = ::goBack,

                                    onSearch = {
                                        navigate("search")
                                    },

                                    onSettings = {
                                        showSettingsSheet = true
                                    },
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
                            onDismissRequest = {
                                showSettingsSheet = false
                            },

                            sheetState = rememberModalBottomSheetState(
                                skipPartiallyExpanded = true
                            ),
                        ) {
                            SettingsSheetContent(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {

        fun newIntent(
            context: Context
        ): Intent =
            Intent(context, MainActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }
}