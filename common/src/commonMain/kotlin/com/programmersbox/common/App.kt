package com.programmersbox.common

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun App() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { QuoteViewModel(scope) }
    val state = rememberDrawerState(DrawerValue.Closed)
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
                    items(viewModel.savedQuotes) { quote ->
                        ElevatedCard(onClick = { viewModel.setQuoteFromSaved(quote) }) {
                            ListItem(
                                headlineText = { Text(quote.quote) },
                                overlineText = { Text(quote.author) },
                                trailingContent = {
                                    IconButton(onClick = { viewModel.removeQuote(quote) }) {
                                        Icon(Icons.Default.Favorite, null)
                                    }
                                }
                            )
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
                        IconButton(onClick = { scope.launch { state.open() } }) { Icon(Icons.Default.MenuOpen, null) }
                    }
                )
            },
            bottomBar = {
                BottomAppBar(
                    actions = {
                        NavigationBarItem(
                            selected = false,
                            onClick = { scope.launch { state.open() } },
                            label = { Text(viewModel.savedQuotes.size.toString()) },
                            icon = { Icon(Icons.Default.Favorite, null) }
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
                            text = { Text("Favorited") },
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
                            Text(
                                viewModel.currentQuote?.q.orEmpty(),
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
                                viewModel.currentQuote?.a.orEmpty(),
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