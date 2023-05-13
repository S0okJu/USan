package com.example.usan_comb1;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class RetrofitClient {

    private static final String BASE_URL = "http://13.124.53.124:53070/";

    private static Retrofit retrofit = null;

    public static Retrofit getRetrofitInstance() {

        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ProductService getProductService() {
        return getRetrofitInstance().create(ProductService.class);
    }
}