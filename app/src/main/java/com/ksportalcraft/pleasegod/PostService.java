package com.ksportalcraft.pleasegod;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PostService {
    @GET("post_work.php")
    Call<ResponseBody> submitPost(
            @Query("username") String username,
            @Query("password") String password,
            @Query("postContent") String postContent
    );
}
