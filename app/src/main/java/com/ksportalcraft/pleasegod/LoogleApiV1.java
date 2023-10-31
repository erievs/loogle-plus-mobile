package com.ksportalcraft.pleasegod;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;

public interface LoogleApiV1 {
    @GET("posts")
    Call<ArrayList<Model>> callModel();

}
