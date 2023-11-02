package com.ksportalcraft.pleasegod;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NotificationHandler {
    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "MyNotificationChannel";
    private Context context;
    private OkHttpClient client;
    private boolean isRequestInProgress = false; // Flag to track if a request is already in progress

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "My Notification Channel Name", // Replace with your desired name
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationChannel.setDescription("My Notification Channel Description"); // Replace with your desired description

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }

            // Add a log message to indicate that the channel has been created
            Log.d("NotificationHandler", "Notification channel created");
        }
    }

    public NotificationHandler(Context context) {
        this.context = context;
        this.client = CustomOkHttpClient.getClient();
        createNotificationChannel(); // Add this line to create the notification channel
        schedulePeriodicNotifications();
    }


    // Schedule periodic notifications at a specified interval
    private void schedulePeriodicNotifications() {
        final Handler handler = new Handler();
        final int delay = 15000; // Delay in milliseconds (15 seconds)

        handler.postDelayed(new Runnable() {
            public void run() {
                if (!isRequestInProgress) {
                    retrieveNotifications("d");
                }
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public void retrieveNotifications(String username) {
        if (isRequestInProgress) {
            return; // Return early if a request is already in progress
        }

        isRequestInProgress = true;

        String url = "http://loogleplus.free.nf/apiv1/get_user_notifications.php?username=" + username;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                isRequestInProgress = false; // Reset the flag on failure
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                isRequestInProgress = false; // Reset the flag after response
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    List<NotificationModel> notifications = parseJson(jsonResponse);
                    showNotifications(notifications);
                }
            }
        });
    }

    public void dismissNotification(String username, int post_id) {
        String url = "http://loogleplus.free.nf/apiv1/get_user_notifications.php?username=" + username + "&postid=" + post_id;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // The notification with post_id has been dismissed on the server
                }
            }
        });
    }

    private List<NotificationModel> parseJson(String json) {
        List<NotificationModel> notifications = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int post_id = jsonObject.getInt("post_id");
                String sender = jsonObject.getString("sender");
                String content = jsonObject.getString("content");
                NotificationModel notification = new NotificationModel(post_id, sender, content);
                notifications.add(notification);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    private void showNotifications(List<NotificationModel> notifications) {
        for (NotificationModel notification : notifications) {
            String content = notification.getContent();

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            Notification notificationItem = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.loogle)
                    .setContentTitle("New Notification")
                    .setContentText(content)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID, notificationItem);
            }
        }
    }
}
