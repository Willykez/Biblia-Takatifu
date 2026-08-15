package com.biblia.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biblia.app.ui.theme.ReadingFont
import kotlinx.coroutines.launch

private data class OnboardPage(
    val title: String,
    val body: String,
)

private val pages = listOf(
    OnboardPage(
        "Biblia yako, nje ya mtandao",
        "Vitabu vyote 66, kwa Kiswahili na Kiingereza pamoja — hakuna mtandao unaohitajika.",
    ),
    OnboardPage(
        "Weka alama, angazia, andika dokezo",
        "Gusa mstari wowote kuuweka alama, kuupamba kwa rangi, au kuandika dokezo lako mwenyewe.",
    ),
    OnboardPage(
        "Tafuta neno lolote papo hapo",
        "Andika neno au fungu la maneno na upate kila mstari unaolihusu, mara moja.",
    ),
)

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
) {
    val pagerState = rememberPagerState(
        pageCount = { pages.size },
    )

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            val p = pages[page]

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = p.title,
                    fontFamily = ReadingFont,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(
                    modifier = Modifier.height(16.dp),
                )

                Text(
                    text = p.body,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
        ) {
            repeat(pages.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(
                            if (index == pagerState.currentPage) {
                                8.dp
                            } else {
                                6.dp
                            },
                        )
                        .background(
                            color = if (index == pagerState.currentPage) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = CircleShape,
                        ),
                )
            }
        }

        if (pagerState.currentPage == pages.lastIndex) {
            Button(
                onClick = onGetStarted,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = "Anza",
                    fontSize = 16.sp,
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(
                    onClick = onGetStarted,
                ) {
                    Text(
                        text = "Ruka",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                TextButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1,
                            )
                        }
                    },
                ) {
                    Text(
                        text = "Endelea",
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}