package com.programmersbox.common

import androidx.compose.runtime.staticCompositionLocalOf
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.migration.AutomaticSchemaMigration
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

internal class SavedQuotes : RealmObject {
    var quotes: RealmList<SavedQuote> = realmListOf()

    @PrimaryKey
    var _id: String = "hello"
}

public class SavedQuote : RealmObject {
    @PrimaryKey
    public var _id: String = RealmUUID.random().toString()
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
    _id = quote
}

internal val LocalQuoteDb = staticCompositionLocalOf { QuoteDatabase() }

public class QuoteDatabase {
    private companion object {
        const val USE_SYNC = true
    }

    private val app = App.create(
        AppConfiguration.Builder(appId = BuildKonfig.appId)
            .log(LogLevel.ALL)
            .build()
    )

    private val realm: Realm by lazy {
        Realm.open(
            if (USE_SYNC) {
                SyncConfiguration.Builder(app.currentUser!!, setOf(SavedQuotes::class, SavedQuote::class))
                    .schemaVersion(5)
                    .initialSubscriptions { realm ->
                        add(realm.query<SavedQuotes>(), name = "list", updateExisting = true)
                        add(realm.query<SavedQuote>(), name = "quotes", updateExisting = true)
                    }
                    .initialData { copyToRealm(SavedQuotes().apply { _id = app.currentUser!!.id }) }
                    .name("savingQuotes11")
            } else {
                RealmConfiguration.Builder(setOf(SavedQuotes::class, SavedQuote::class))
                    .schemaVersion(4)
                    .migration(AutomaticSchemaMigration { })
                    .deleteRealmIfMigrationNeeded()
            }.build()
        )
    }

    public fun isLoggedIn(): Boolean = app.currentUser != null || !USE_SYNC

    public suspend fun login(username: String = BuildKonfig.username, password: String = BuildKonfig.password): User? {
        return if (USE_SYNC) {
            app.login(Credentials.emailPassword(username, password))
        } else {
            null
        }
    }

    public suspend fun registration(password: String, email: String) {
        if (USE_SYNC) {
            try {
                app.emailPasswordAuth.registerUser(email = email, password = password)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun initialDb(): SavedQuotes {
        val f = realm.query(SavedQuotes::class).first().find()
        return f ?: realm.write { copyToRealm(SavedQuotes().apply { _id = "hello" }) }
    }

    public suspend fun getQuotes(): Flow<List<SavedQuote>> = if (USE_SYNC) {
        realm.query(SavedQuotes::class)
            .asFlow()
            .mapNotNull {
                it.list.lastOrNull() ?: realm.write { copyToRealm(SavedQuotes().apply { _id = app.currentUser!!.id }) }
            }
    } else {
        initialDb()
            .asFlow()
            .mapNotNull { it.obj }
            .distinctUntilChanged()
    }
        .mapNotNull { it.quotes.toList() }

    internal suspend fun saveQuote(quote: Quote) {
        realm.updateInfo<SavedQuotes> {
            if (it?.quotes?.any { q -> q.quote == quote.q } == false) {
                it.quotes.add(quote.toSavedQuote())
            }
        }
    }

    internal suspend fun removeQuote(quote: Quote) {
        realm.updateInfo<SavedQuotes> {
            if (it?.quotes?.any { q -> q.quote == quote.q } == true) {
                it.quotes.removeAll { q -> q.quote == quote.q }
            }
        }
    }

    internal suspend fun removeQuote(quote: SavedQuote) {
        realm.updateInfo<SavedQuotes> {
            it?.quotes?.removeAll { q -> q.quote == quote.quote }
        }
    }
}

private suspend inline fun <reified T : RealmObject> Realm.updateInfo(crossinline block: MutableRealm.(T?) -> Unit) {
    query(T::class).first().find()?.also { info ->
        write { block(findLatest(info)) }
    }
}