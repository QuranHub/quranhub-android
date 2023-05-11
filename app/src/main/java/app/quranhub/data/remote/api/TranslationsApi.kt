package app.quranhub.data.remote.api;

import app.quranhub.data.remote.model.TranslationsResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface TranslationsApi {

    @GET("/api/user/get-translations")
    Call<TranslationsResponse> getAllTranslations();

}
