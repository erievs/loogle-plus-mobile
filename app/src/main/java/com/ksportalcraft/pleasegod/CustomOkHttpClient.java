package com.ksportalcraft.pleasegod;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class CustomOkHttpClient {

    public static OkHttpClient getClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        // Add custom headers here
        clientBuilder.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .addHeader("Cookie", "__test=fcc21eac01ffba8302cc093670e6d98c")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240")
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        });

        return clientBuilder.build();
    }
}
