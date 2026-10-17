package app.billfolder.libris.android

import android.app.Application
import app.billfolder.libris.ui.initKoin
import io.ktor.client.engine.okhttp.OkHttp

class LibrisApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(BuildConfig.API_BASE_URL, PrefsTokenStore(this), OkHttp.create())
    }
}
