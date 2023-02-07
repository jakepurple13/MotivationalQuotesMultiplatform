package com.programmersbox.common

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
internal class QuoteViewModel(private val scope: CoroutineScope) {

    private val service = ApiService()
    private val stopwatch = Stopwatch()

    var state by mutableStateOf(NetworkState.NotLoading)
        private set

    private val db by lazy { QuoteDatabase() }

    var currentQuote by mutableStateOf<Quote?>(null)
        private set

    val savedQuotes = mutableStateListOf<SavedQuote>()

    val isCurrentQuoteSaved by derivedStateOf { savedQuotes.any { it.quote == currentQuote?.q } }

    var newQuoteAvailable by mutableStateOf(true)
        private set

    init {
        flow { emit(db.getQuotes()) }
            .flattenConcat()
            .onEach {
                savedQuotes.clear()
                savedQuotes.addAll(it)
            }
            .filter { currentQuote == null }
            .onEach {
                if (it.isEmpty()) {
                    newQuote()
                } else {
                    currentQuote = it.lastOrNull()?.toQuote()
                }
            }
            .launchIn(scope)

        stopwatch.time
            .combine(snapshotFlow { state }.distinctUntilChanged()) { _, s -> s }
            .onEach {
                newQuoteAvailable = when (it) {
                    NetworkState.Loading -> false
                    NetworkState.NotLoading -> {
                        delay(5000)
                        true
                    }

                    NetworkState.Error -> {
                        true
                    }
                }
            }
            .launchIn(scope)
    }

    fun newQuote() {
        scope.launch {
            state = NetworkState.Loading
            newQuoteAvailable = false
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