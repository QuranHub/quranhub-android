package app.quranhub.data.remote;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import app.quranhub.data.Constants;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static Retrofit retrofit;
    private static OkHttpClient httpClient;
    private static final int REQUEST_TIMEOUT = 60;


    private ApiClient() {
    }

    public static Retrofit getClient() {

        if (httpClient == null && retrofit == null) {
            synchronized (ApiClient.class) {
                if (httpClient == null && retrofit == null) {
                    initHttpClient();
                    retrofit = new Retrofit.Builder()
                            .baseUrl(Constants.API_BASE_URL)
                            .client(httpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    private static void initHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder()
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS);
        httpClient = builder.build();
    }

}
