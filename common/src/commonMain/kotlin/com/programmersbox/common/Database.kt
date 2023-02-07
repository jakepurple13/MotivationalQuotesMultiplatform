package com.programmersbox.common

import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.migration.AutomaticSchemaMigration
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

internal class SavedQuotes : RealmObject {
    var quotes: RealmList<SavedQuote> = realmListOf()
}

public class SavedQuote : RealmObject {
    @field:PrimaryKey
    public var quote: String = ""
    public var author: String = ""
    public var image: String? = null
    public var characterCount: Int = 0
    public var htmlFormatted: String = ""
}

internal fun SavedQuote.toQuote() = Quote(quote, author, image, characterCount, htmlFormatted)
internal fun Quote.toSavedQuote() = SavedQuote().apply {
    quote = q.orEmpty()
    author = a.orEmpty()
    image = i.orEmpty()
    characterCount = c ?: 0
    htmlFormatted = h.orEmpty()
}

public class QuoteDatabase {
    private val realm by lazy {
        Realm.open(
            RealmConfiguration.Builder(setOf(SavedQuotes::class, SavedQuote::class))
                .schemaVersion(4)
                .migration(AutomaticSchemaMigration { })
                .deleteRealmIfMigrationNeeded()
                .build()
        )
    }

    private suspend fun initialDb(): SavedQuotes {
        val f = realm.query(SavedQuotes::class).first().find()
        return f ?: realm.write { copyToRealm(SavedQuotes()) }
    }

    public suspend fun getQuotes(): Flow<List<SavedQuote>> = initialDb().asFlow()
        .mapNotNull { it.obj }
        .distinctUntilChanged()
        .mapNotNull { it.quotes.toList() }

    internal suspend fun saveQuote(quote: Quote) {
        realm.updateInfo<SavedQuotes> { it?.quotes?.add(quote.toSavedQuote()) }
    }

    internal suspend fun removeQuote(quote: Quote) {
        realm.updateInfo<SavedQuotes> { it?.quotes?.remove(quote.toSavedQuote()) }
    }

    internal suspend fun removeQuote(quote: SavedQuote) {
        realm.updateInfo<SavedQuotes> { it?.quotes?.remove(quote) }
    }
}

private suspend inline fun <reified T : RealmObject> Realm.updateInfo(crossinline block: MutableRealm.(T?) -> Unit) {
    query(T::class).first().find()?.also { info ->
        write { block(findLatest(info)) }
    }
}