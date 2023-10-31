package com.ksportalcraft.pleasegod;

import okhttp3.OkHttpClient;

public class OkHttpClientSingleton {
    private static OkHttpClient instance;

    private OkHttpClientSingleton() {
        // Private constructor to prevent instantiation
    }

    public static OkHttpClient getInstance() {
        if (instance == null) {
            instance = createOkHttpClient();
        }
        return instance;
    }

    private static OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                // Add your interceptors, headers, and cookies here
                .build();
    }
}
