package com.programmersbox.common

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                                    IconButton(onClick = {  })
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
                            onClick = { viewModel.newQuote() },
                            label = { Text("New Quote") },
                            icon = { Icon(Icons.Default.RequestQuote, null) }
                        )
                    },
                    floatingActionButton = {

                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier.padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(viewModel.state) { target ->
                    when (target) {
                        NetworkState.Loading -> CircularProgressIndicator()
                        NetworkState.NotLoading -> Text(viewModel.currentQuote.q.orEmpty())
                        NetworkState.Error -> Text("Something went wrong, please try again")
                    }
                }
            }
        }
    }
}