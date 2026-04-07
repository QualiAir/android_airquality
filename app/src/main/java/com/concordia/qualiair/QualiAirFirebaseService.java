package com.concordia.qualiair;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class QualiAirFirebaseService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "qualiair_fcm";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title;
        String body;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        } else {
            title = "Air Quality Alert";
            body = "Check your air quality readings.";
        }

        showNotification(title, body);
    }

    @Override
    public void onNewToken(String token) {
        //save to sharedprefs first so it's ready when we have a device id
        getSharedPreferences("QualiAirPreferences", MODE_PRIVATE).edit().putString("fcm_token", token).apply();
        // Send this token to Python backend - but this will fail gracefully if no device detected
        FcmTokenManager.sendTokenToBackend(this, token);
    }

    private void showNotification(String title, String body) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "FCM Notifications", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        nm.notify(2001, builder.build());
    }
}