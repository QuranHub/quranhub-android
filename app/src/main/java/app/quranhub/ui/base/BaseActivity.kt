package app.quranhub.ui.base

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import app.quranhub.util.LocaleUtils.initAppLanguage

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        initAppLanguage(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(initAppLanguage(newBase))
    }

    fun restart() {
        if (intent != null) {
            startActivity(intent)
            finish()
        } else {
            Log.e(TAG, "Couldn't restart the activity!")
        }
    }

    companion object {
        private val TAG = BaseActivity::class.java.simpleName
    }
}