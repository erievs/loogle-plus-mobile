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
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Model> modelArrayList;
    private LoogleApiV1 apiV1;
    private PostService postService;

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

        Retrofit retrofitLoogle = new Retrofit.Builder()
                .baseUrl("http://loogleplus.free.nf/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();

        apiV1 = retrofitLoogle.create(LoogleApiV1.class);
        postService = retrofitLoogle.create(PostService.class); // Create PostService instance

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

                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void postContent(String content, String username, String password) {
        Call<ResponseBody> call = postService.submitPost(username, password, content);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Handle a successful response
                    Log.d("PostContent", "Post submitted successfully");
                    postEditText.setText("");
                    usernameEditText.setText("");
                    passwordEditText.setText("");
                    showToast("Post submitted successfully");
                } else {
                    // Handle an unsuccessful response
                    Log.e("PostContent", "Failed to submit post");
                    // Log the error details here
                    Log.e("PostContent", "Error message: " + response.message());
                    Log.e("PostContent", "Error code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("PostContent", "Error body: " + errorBody);
                        showToast("Failed to submit post: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showToast("Failed to submit post: An error occurred");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

                t.printStackTrace();
                Log.e("PostContent", "Error: " + t.getMessage());
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
