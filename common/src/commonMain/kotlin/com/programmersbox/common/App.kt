package com.programmersbox.common

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
internal fun App(
    onShareClick: (SavedQuote) -> Unit
) {
    val scope = rememberCoroutineScope()
    val db = LocalQuoteDb.current
    val viewModel = remember { QuoteViewModel(scope, db) }
    val state = rememberDrawerState(DrawerValue.Closed)
    var loggedIn by remember { mutableStateOf(viewModel.isLoggedIn) }
    if (loggedIn) {
        LaunchedEffect(Unit) { viewModel.login() }
        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet {
                    TopAppBar(
                        title = { Text("Saved") },
                        actions = { Text(viewModel.savedQuotes.size.toString()) }
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        viewModel.groupedSavedQuotes.forEach {
                            var expand by mutableStateOf(it.value.size == 1)

                            if (it.value.size > 1) {
                                item {
                                    Surface(
                                        onClick = { expand = !expand }
                                    ) {
                                        ListItem(
                                            leadingContent = { Text(it.value.size.toString()) },
                                            headlineText = { Text(it.key) },
                                            trailingContent = {
                                                Icon(
                                                    Icons.Default.ArrowDropDown,
                                                    null,
                                                    modifier = Modifier.rotate(animateFloatAsState(if (expand) 180f else 0f).value)
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            item {
                                Column(
                                    modifier = Modifier.animateContentSize(),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    if (expand) {
                                        it.value.forEach { quote ->
                                            var remove by remember { mutableStateOf(false) }
                                            AnimatedContent(
                                                remove,
                                                transitionSpec = {
                                                    if (targetState > initialState) {
                                                        slideInHorizontally { width -> -width } + fadeIn() with
                                                                slideOutHorizontally { width -> width } + fadeOut()
                                                    } else {
                                                        slideInHorizontally { width -> width } + fadeIn() with
                                                                slideOutHorizontally { width -> -width } + fadeOut()
                                                    }.using(SizeTransform(clip = false))
                                                }
                                            ) { target ->
                                                if (target) {
                                                    OutlinedCard(
                                                        border = BorderStroke(
                                                            CardDefaults.outlinedCardBorder().width,
                                                            Color.Red
                                                        )
                                                    ) {
                                                        ListItem(
                                                            leadingContent = {
                                                                IconButton(onClick = { remove = false }) {
                                                                    Icon(Icons.Default.Close, null)
                                                                }
                                                            },
                                                            headlineText = { Text("Are you sre you want to remove this?") },
                                                            supportingText = { Text(quote.quote) },
                                                            trailingContent = {
                                                                IconButton(onClick = { viewModel.removeQuote(quote) }) {
                                                                    Icon(Icons.Default.Check, null)
                                                                }
                                                            }
                                                        )
                                                    }
                                                } else {
                                                    ElevatedCard(onClick = { viewModel.setQuoteFromSaved(quote) }) {
                                                        ListItem(
                                                            headlineText = { Text(quote.quote) },
                                                            overlineText = { Text(quote.author) },
                                                            trailingContent = {
                                                                IconButton(onClick = { remove = true }) {
                                                                    Icon(Icons.Default.Favorite, null)
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Divider()
                                }
                            }
                        }
                    }
                }
            },
            drawerState = state
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Quotes") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { state.open() } }) {
                                Icon(
                                    Icons.Default.MenuOpen,
                                    null
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomAppBar(
                        actions = {
                            NavigationBarItem(
                                selected = false,
                                onClick = { viewModel.currentQuote?.let { onShareClick(it.toSavedQuote()) } },
                                label = { Text("Share") },
                                icon = { Icon(Icons.Default.Share, null) }
                            )

                            NavigationBarItem(
                                selected = viewModel.newQuoteAvailable,
                                onClick = { viewModel.newQuote() },
                                label = { if (viewModel.newQuoteAvailable) Text("New Quote") },
                                icon = {
                                    if (viewModel.newQuoteAvailable) {
                                        Icon(Icons.Default.FormatQuote, null)
                                    } else {
                                        CircularProgressIndicator()
                                    }
                                },
                                enabled = viewModel.newQuoteAvailable
                            )
                        },
                        floatingActionButton = {
                            ExtendedFloatingActionButton(
                                expanded = viewModel.isCurrentQuoteSaved,
                                text = { Text("Saved") },
                                icon = {
                                    Crossfade(viewModel.isCurrentQuoteSaved) { target ->
                                        Icon(
                                            when (target) {
                                                true -> Icons.Default.Favorite
                                                false -> Icons.Default.FavoriteBorder
                                            },
                                            null
                                        )
                                    }
                                },
                                onClick = {
                                    when (viewModel.isCurrentQuoteSaved) {
                                        true -> viewModel.removeQuote(viewModel.currentQuote)
                                        false -> viewModel.saveQuote(viewModel.currentQuote)
                                    }
                                }
                            )
                        }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(viewModel.state) { target ->
                        when (target) {
                            NetworkState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                            NetworkState.Error -> Text("Something went wrong, please try again")
                            NetworkState.NotLoading -> Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                viewModel.currentQuote?.let { quote ->
                                    Text(
                                        quote.q.orEmpty(),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(horizontal = 20.dp)
                                    )
                                    Text(
                                        "By",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        quote.a.orEmpty(),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        LoginScreen { loggedIn = true }
    }
}