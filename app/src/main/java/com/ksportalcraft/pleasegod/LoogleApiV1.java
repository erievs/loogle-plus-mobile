package com.ksportalcraft.pleasegod;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;

public interface LoogleApiV1 {
    @GET("apiv1/mobile_posts.php")
    Call<ArrayList<Model>> callModel();

}
