package app.quranhub.core.common.base

import android.app.Service
import android.content.Context
import app.quranhub.core.common.util.LocaleUtils.initAppLanguage

abstract class BaseService : Service() {

    override fun onCreate() {
        initAppLanguage(this)
        super.onCreate()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(initAppLanguage(newBase))
    }
}