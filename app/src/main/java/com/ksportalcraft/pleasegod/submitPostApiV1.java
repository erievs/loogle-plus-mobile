package com.ksportalcraft.pleasegod;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface submitPostApiV1 {
    @POST("submit_post_endpoint") // Replace with the actual endpoint
    Call<YourResponseType> submitPost(@Body PostContent postContent);
}
