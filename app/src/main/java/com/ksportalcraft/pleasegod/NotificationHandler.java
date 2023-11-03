package com.ksportalcraft.pleasegod;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.ksportalcraft.pleasegod.CustomOkHttpClient;
import com.ksportalcraft.pleasegod.NotificationModel;

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
import java.util.Calendar;
import java.util.List;

public class NotificationHandler {
    private static final String TAG = "NotificationHandler";
    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "MyNotificationChannel";
    private Context context;
    private OkHttpClient client;
    private boolean hasScheduledNotifications = false;
    private NotificationManager notificationManager;
    private Handler logHandler;

    public NotificationHandler(Context context) {
        this.context = context;
        this.client = CustomOkHttpClient.getClient();
        createNotificationChannel();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        retrieveNotifications("d");

        // Call the method to schedule alarms
        scheduleNotificationAlarms();

        // Create a handler for logging messages every second
        logHandler = new Handler(Looper.getMainLooper());
        logMessagesPeriodically();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "My Notification Channel Name",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationChannel.setDescription("My Notification Channel Description");

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    private void scheduleNotificationAlarms() {
        scheduleNotificationAlarm(1, 58, "com.ksportalcraft.pleasegod.ACTION_SCHEDULE_NOTIFICATIONS_AM");
        scheduleNotificationAlarm(19, 25, "com.ksportalcraft.pleasegod.ACTION_SCHEDULE_NOTIFICATIONS_PM");
        hasScheduledNotifications = true;
    }

    private void scheduleNotificationAlarm(int hour, int minute, String action) {
        Intent intent = new Intent(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long alarmTime = calendar.getTimeInMillis();
        long currentTime = System.currentTimeMillis();

        if (alarmTime <= currentTime) {
            alarmTime += AlarmManager.INTERVAL_DAY;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    public void dismissNotification(final String username, final int post_id) {
        String url = "http://loogleplus.is-great.org/apiv1/dismiss_notification.php?username=" + username + "&postid=" + post_id;

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
                    Log.d("NotificationHandler", "Notification dismissed for post_id: " + post_id);
                }
            }
        });
    }

    public void retrieveNotifications(String username) {
        String url = "http://loogleplus.is-great.org/apiv1/get_user_notifications.php?username=" + username;

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
                    String jsonResponse = response.body().string();
                    List<NotificationModel> notifications = parseJson(jsonResponse);
                    showNotifications(notifications);
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

    private void logMessagesPeriodically() {
        logHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Running");
                logMessagesPeriodically(); // Schedule the next log message
            }
        }, 1000); // Delay of 1000 milliseconds (1 second)
    }

    private void showNotifications(List<NotificationModel> notifications) {
        for (NotificationModel notification : notifications) {
            String sender = notification.getSender();
            String content = notification.getContent();
            String notificationText = sender + ": " + content;
            // showPopupNotification(notificationText, sender, notification.getPostId());
        }
    }

    class NotificationAlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("com.ksportalcraft.pleasegod.ACTION_SCHEDULE_NOTIFICATIONS_AM") ||
                        action.equals("com.ksportalcraft.pleasegod.ACTION_SCHEDULE_NOTIFICATIONS_PM")) {
                    NotificationHandler notificationHandler = new NotificationHandler(context);
                    notificationHandler.retrieveNotifications("d");
                }
            }
        }
    }
}
