package app.quranhub.data.remote

import app.quranhub.data.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null

    private var httpClient: OkHttpClient? = null

    private const val REQUEST_TIMEOUT = 60

    @JvmStatic
    val client: Retrofit?
        get() {
            if (httpClient == null && retrofit == null) {
                synchronized(ApiClient::class.java) {
                    if (httpClient == null && retrofit == null) {
                        initHttpClient()
                        retrofit = Retrofit.Builder()
                            .baseUrl(Constants.API_BASE_URL)
                            .client(httpClient!!)
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .build()
                    }
                }
            }
            return retrofit
        }

    private fun initHttpClient() {
        val builder = OkHttpClient().newBuilder()
            .connectTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
        httpClient = builder.build()
    }
}