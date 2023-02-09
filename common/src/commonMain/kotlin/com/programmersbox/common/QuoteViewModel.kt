package com.programmersbox.common

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class)
internal class QuoteViewModel(private val scope: CoroutineScope, private val db: QuoteDatabase) {

    private val service = ApiService()

    val isLoggedIn get() = db.isLoggedIn()

    var state by mutableStateOf(NetworkState.NotLoading)
        private set

    var currentQuote by mutableStateOf<Quote?>(null)
        private set

    val savedQuotes = mutableStateListOf<SavedQuote>()

    val groupedSavedQuotes by derivedStateOf {
        savedQuotes
            .sortedBy { it.author }
            .groupBy { it.author }
    }

    val isCurrentQuoteSaved by derivedStateOf { savedQuotes.any { it.quote == currentQuote?.q } }

    var newQuoteAvailable by mutableStateOf(true)
        private set

    private var count by mutableStateOf(0)

    init {
        snapshotFlow { count }
            .filter { it <= 4 }
            .debounce(30000)
            .onEach {
                newQuoteAvailable = true
                count = 0
            }
            .launchIn(scope)

        snapshotFlow { count }
            .filter { it > 4 }
            .distinctUntilChanged()
            .onEach {
                newQuoteAvailable = false
                delay(30000)
                newQuoteAvailable = true
                count = 0
            }
            .launchIn(scope)
    }

    fun login() {
        scope.launch {
            async { db.login() }.await()
            db.getQuotes()
                .onEach {
                    savedQuotes.clear()
                    savedQuotes.addAll(it)
                }
                .filter { currentQuote == null }
                .onEach {
                    if (it.isEmpty()) {
                        newQuote()
                    } else {
                        currentQuote = it.randomOrNull()?.toQuote()
                    }
                }
                .launchIn(scope)
        }
    }

    fun newQuote() {
        scope.launch {
            state = NetworkState.Loading
            count++
            state = service.getQuote().fold(
                onSuccess = {
                    currentQuote = it
                    NetworkState.NotLoading
                },
                onFailure = {
                    it.printStackTrace()
                    NetworkState.Error
                }
            )
        }
    }

    fun setQuoteFromSaved(quote: SavedQuote) {
        currentQuote = quote.toQuote()
    }

    fun saveQuote(quote: Quote?) {
        scope.launch { quote?.let { db.saveQuote(it) } }
    }

    fun removeQuote(quote: Quote?) {
        scope.launch { quote?.let { db.removeQuote(it) } }
    }

    fun removeQuote(quote: SavedQuote) {
        scope.launch { db.removeQuote(quote) }
    }

}

internal enum class NetworkState { Loading, NotLoading, Error }