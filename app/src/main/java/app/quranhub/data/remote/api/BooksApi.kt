package app.quranhub.data.remote.api;

import app.quranhub.data.remote.model.BooksResponse;
import io.reactivex.Single;
import retrofit2.http.GET;

public interface BooksApi {

    @GET("/api/user/get-books")
    Single<BooksResponse> getAllBooks();

}
