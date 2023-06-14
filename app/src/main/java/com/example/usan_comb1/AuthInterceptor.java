package com.example.usan_comb1;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private String accessToken;

    public AuthInterceptor(String accessToken) {
        this.accessToken = accessToken;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        if (accessToken != null) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();
        }
        return chain.proceed(request);
    }
}

/*
class Main {
    private static final String BASE_URL = "http://13.124.53.124:53143/";

    public static void main(String[] args) {
        String accessToken = "myAccessToken";

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new AuthInterceptor(accessToken));
        OkHttpClient client = httpClient.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        ProductService service = retrofit.create(ProductService.class);

        // Call API methods on 'service' object
    }
}
 */
