package com.ksportalcraft.pleasegod;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ksportalcraft.pleasegod.Custom;
import com.ksportalcraft.pleasegod.LoogleApiV1;
import com.ksportalcraft.pleasegod.Model;
import com.ksportalcraft.pleasegod.PostContent;
import com.ksportalcraft.pleasegod.R;
import com.ksportalcraft.pleasegod.YourResponseType;
import com.ksportalcraft.pleasegod.submitPostApiV1;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Model> modelArrayList;
    private LoogleApiV1 apiV1;
    private submitPostApiV1 apiSv1;

    private ListView lv;
    private EditText postEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button postButton;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Handler handler = new Handler();
    private final int delay = 60 * 1000; // 60 seconds in milliseconds
    private String cookie = "__test=62d89c24e9b57d03a3ba3717401a3e96";
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240";
    private OkHttpClient customOkHttpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = findViewById(R.id.lv);
        postEditText = findViewById(R.id.postEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        postButton = findViewById(R.id.postButton);

        modelArrayList = new ArrayList<>();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        // Set up the swipe-to-refresh functionality
        swipeRefreshLayout.setOnRefreshListener(() -> {
            displayPosts();
        });

        postButton.setOnClickListener(view -> {
            String content = postEditText.getText().toString();
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            postContent(content, username, password);
        });

        displayPosts();

        // Schedule periodic requests
        handler.postDelayed(new Runnable() {
            public void run() {
                displayPosts();
                handler.postDelayed(this, delay);
            }
        }, delay);
    }


    private void displayPosts() {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(chain -> {
            okhttp3.Request original = chain.request();
            okhttp3.Request.Builder requestBuilder = original.newBuilder()
                    .header("Cookie", cookie)
                    .header("User-Agent", userAgent);
            okhttp3.Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        OkHttpClient httpClient = httpClientBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://loogleplus.free.nf/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();

        apiV1 = retrofit.create(LoogleApiV1.class);
        Call<ArrayList<Model>> arrayListCall = apiV1.callModel();
        arrayListCall.enqueue(new Callback<ArrayList<Model>>() {
            @Override
            public void onResponse(Call<ArrayList<Model>> call, Response<ArrayList<Model>> response) {
                if (response.isSuccessful()) {
                    modelArrayList = response.body();
                    Custom custom = new Custom(modelArrayList, MainActivity.this, R.layout.singleview);
                    lv.setAdapter(custom);
                } else {
                    String rawResponse = null;
                    try {
                        if (response.errorBody() != null) {
                            rawResponse = response.errorBody().string();
                            Log.e("RetrofitError", "Error Response: " + rawResponse);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.e("RetrofitError", "Response not successful: " + response.code());
                    Log.e("RetrofitError", "Raw Response: " + rawResponse);
                }
                // Make sure to call setRefreshing(false) when the refresh is completed
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ArrayList<Model>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("RetrofitError", "Error: " + t.getMessage());
                Log.e("RetrofitError", "URL: " + call.request().url().toString());
                // Make sure to call setRefreshing(false) when the refresh is completed
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void postContent(String content, String username, String password) {
        // Create an OkHttpClient with the necessary headers
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(chain -> {
            okhttp3.Request original = chain.request();
            okhttp3.Request.Builder requestBuilder = original.newBuilder()
                    .header("Cookie", cookie)
                    .header("User-Agent", userAgent);
            okhttp3.Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        OkHttpClient httpClient = httpClientBuilder.build();

        // Create a Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://loogleplus.free.nf/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();

        apiV1 = (LoogleApiV1) retrofit.create(submitPostApiV1.class);

        // Create a new post content object with the provided username, password, and content
        PostContent postContentObject = new PostContent(username, password, content);

        // Make the POST request to your server's submit post API
        Call<YourResponseType> call = apiSv1.submitPost(postContentObject);

        call.enqueue(new Callback<YourResponseType>() {
            @Override
            public void onResponse(Call<YourResponseType> call, Response<YourResponseType> response) {
                if (response.isSuccessful()) {
                    // Handle a successful response
                    Log.d("PostContent", "Post submitted successfully");
                    // Clear the input fields
                    postEditText.setText("");
                    usernameEditText.setText("");
                    passwordEditText.setText("");
                } else {
                    // Handle an unsuccessful response
                    Log.e("PostContent", "Failed to submit post");
                    Toast.makeText(MainActivity.this, "Failed to submit post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<YourResponseType> call, Throwable t) {
                // Handle the failure of the POST request
                t.printStackTrace();
                Log.e("PostContent", "Error: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
