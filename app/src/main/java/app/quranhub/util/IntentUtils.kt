package app.quranhub.util

import android.content.Context
import android.content.Intent
import app.quranhub.R

object IntentUtils {

    fun getShareIntent(ayaText: String, context: Context): Intent {
        val shareBody = """
               $ayaText
               
               ${context.getString(R.string.app_name)}.
               """.trimIndent()
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.aya))
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        return Intent.createChooser(
            sharingIntent,
            context.resources.getString(R.string.share_using)
        )
    }
}