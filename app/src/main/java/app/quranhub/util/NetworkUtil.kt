package app.quranhub.util

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtil {

    const val STATUS_NOT_DOWNLOADED = 0
    const val STATUS_DOWNLOADING = 1
    const val STATUS_DOWNLOADED = 2

    @JvmStatic
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}