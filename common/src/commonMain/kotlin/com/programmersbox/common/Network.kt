package com.programmersbox.common

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class ApiService {
    private val json = Json {
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client by lazy {
        HttpClient {
            install(Logging)
            install(ContentNegotiation) { json(json) }
        }
    }

    suspend fun getQuote() = runCatching { client.get(url).body<List<Quote>>().first() }

    companion object {
        private const val url = "https://zenquotes.io/api/random"
    }
}

@Serializable
internal data class Quote(
    val q: String? = null,
    val a: String? = null,
    val i: String? = null,
    val c: Int? = null,
    val h: String? = null
)