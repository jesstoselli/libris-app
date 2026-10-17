package app.billfolder.libris.android

import android.content.Context
import app.billfolder.libris.shared.Session
import app.billfolder.libris.shared.TokenStore
import kotlinx.serialization.json.Json

/** App-private and excluded from backup (see res/xml): a restored refresh token would be a reused one. */
class PrefsTokenStore(context: Context) : TokenStore {

    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    override fun load(): Session? = prefs.getString(KEY, null)?.let {
        runCatching { Json.decodeFromString(Session.serializer(), it) }.getOrNull()
    }

    override fun save(session: Session?) {
        val editor = prefs.edit()
        if (session == null) editor.remove(KEY) else editor.putString(KEY, Json.encodeToString(Session.serializer(), session))
        editor.apply()
    }

    private companion object {
        const val FILE = "libris_session"
        const val KEY = "session"
    }
}
