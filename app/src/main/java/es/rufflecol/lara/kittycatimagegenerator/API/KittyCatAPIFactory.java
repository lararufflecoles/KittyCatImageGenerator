package es.rufflecol.lara.kittycatimagegenerator.API;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class KittyCatAPIFactory {

    private static final String API_BASE_URL = "https://nijikokun-random-cats.p.mashape.com";

    public static KittyCatAPI create() {

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new KittyCatInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit.create(KittyCatAPI.class);
    }
}