package app.quranhub.data.model

import android.content.Context
import app.quranhub.data.Constants
import app.quranhub.data.local.prefs.AppPreferencesManager.getAppLangSetting
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class ReciterModel(
    @DocumentId
    var id: String = "",
    var name: Map<String, String>? = null,
    @Exclude
    private var localizedName: String? = null,
    var nationality: Map<String, String>? = null,
    @Exclude
    private var localizedNationality: String? = null,
    @PropertyName("audio_base_url")
    var audioBaseUrl: String = ""
) {

    fun getLocalizedName(context: Context): String {
        if (localizedName != null) return localizedName!!
        val appLangCode = getAppLangSetting(context)
        return if (name!!.containsKey(appLangCode)) name!![appLangCode]!! else name!![Constants.Language.DEFAULT_APP_LANGUAGE]!!
    }

    fun getLocalizedNationality(context: Context): String {
        if (localizedNationality != null) return localizedNationality!!
        val appLangCode = getAppLangSetting(context)
        return if (nationality!!.containsKey(appLangCode)) nationality!![appLangCode]!! else nationality!![Constants.Language.DEFAULT_APP_LANGUAGE]!!
    }
}