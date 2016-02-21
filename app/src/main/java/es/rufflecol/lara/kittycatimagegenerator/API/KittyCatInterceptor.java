package es.rufflecol.lara.kittycatimagegenerator.API;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;

public class KittyCatInterceptor implements Interceptor {

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request request = original.newBuilder()
                .header("X-Mashape-Key", "rElncNLHLPmshc3cqJMrdQHa8daxp1kXSXDjsn0TWiEzwIxda4")
                .header("Accept", "application/json")
                .method(original.method(), original.body())
                .build();
        return chain.proceed(request);
    }
}
