package com.ksportalcraft.pleasegod;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface submitPostApiV1 {
    @POST("post_work.php")
    Call<ResponseBody> submitPost(@Query("username") String username, @Query("password") String password, @Body String postContent);

    Call<ResponseBody> submitPost(PostContent postContent);
}
