package com.programmersbox.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
internal class QuoteViewModel(private val scope: CoroutineScope) {

    private val service = ApiService()
    var state by mutableStateOf(NetworkState.NotLoading)
        private set

    private val db by lazy { QuoteDatabase() }

    var currentQuote by mutableStateOf(Quote())
        private set

    val savedQuotes = mutableStateListOf<SavedQuote>()

    init {
        flow { emit(db.getQuotes()) }
            .flattenConcat()
            .onEach {
                savedQuotes.clear()
                savedQuotes.addAll(it)
            }
            .launchIn(scope)
    }

    fun newQuote() {
        scope.launch {
            state = NetworkState.Loading
            state = service.getQuote()
                .fold(
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

    fun saveQuote(quote: Quote) {
        scope.launch { db.saveQuote(quote) }
    }

    fun removeQuote(quote: Quote) {
        scope.launch { db.removeQuote(quote) }
    }

    fun removeQuote(quote: SavedQuote) {
        scope.launch { db.removeQuote(quote) }
    }

}

internal enum class NetworkState { Loading, NotLoading, Error }