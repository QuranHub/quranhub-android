package app.quranhub.data.remote.api

import app.quranhub.data.remote.model.BooksResponse
import io.reactivex.Single
import retrofit2.http.GET

interface BooksApi {

    @get:GET("/api/user/get-books")
    val allBooks: Single<BooksResponse?>?
}