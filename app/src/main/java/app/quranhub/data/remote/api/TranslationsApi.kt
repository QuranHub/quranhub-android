package app.quranhub.data.remote.api

import app.quranhub.data.remote.model.TranslationsResponse
import retrofit2.Call
import retrofit2.http.GET

interface TranslationsApi {

    @get:GET("/api/user/get-translations")
    val allTranslations: Call<TranslationsResponse?>?

}