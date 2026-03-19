package com.concordia.qualiair;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.app.NotificationChannel;//creates the notification category (required Android 8+)
import android.app.NotificationManager;//the Android system service that actually sends the notification
import android.content.Context;//access to Android system services
import android.os.Build;//used to check the Android version before creating the channel
import androidx.core.app.NotificationCompat;//builds the notification with title, text, sound, vibration
public class AlertManager {

    private static final long TIMER_DURATION = 5_000L; // 5 seconds
    private Handler handler;
    private Runnable pendingAlert;
    private boolean timerRunning=false;
    private final Context context;
    private static final String CHANNEL_ID = "qualiair_caution";
    private static final int NOTIF_ID=1001;



    public AlertManager(Context context)
    {
        this.context=context;
        this.handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }
    private void startTimer(final AirQualityMonitor monitor){
        if(timerRunning) return;
        timerRunning=true;
        Log.d("AlertManager", "Caution detected- starting 5 s timer");

        pendingAlert= new Runnable(){
            @Override
            public void run() {
                timerRunning=false;
                if(monitor.isAnyCaution()){
                    Log.d("AlertManager", "Still in caution after 5s - firing alert");
                    fireAlert(monitor);
                }else{
                    Log.d("AlertManager","Dropped back to Good before 5s- no alert");
                }
            }
        };
        handler.postDelayed(pendingAlert, TIMER_DURATION);
    }
    private void cancelTimer(){
        if(pendingAlert!=null){
            handler.removeCallbacks(pendingAlert);
            pendingAlert=null;
            Log.d("AlertManager","Value back to Good -timer cancelled");
        }
        timerRunning=false;
    }
    public void onNewReading(AirQualityMonitor monitor) {
        if (monitor.isAnyCaution()) {
            startTimer(monitor);
        } else {
            cancelTimer();
        }
    }

    private void fireAlert(AirQualityMonitor monitor) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Air Quality Alert")
                .setContentText("Caution: elevated contaminant levels detected")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                .setAutoCancel(true);

        NotificationManager nm=
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(nm!=null){
            nm.notify(NOTIF_ID, builder.build());
        }
    }
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Air QUality Alerts ",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alerts when air quality is at caution level");
            channel.enableVibration(true);
            NotificationManager nm=
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(nm!=null){
                nm.createNotificationChannel(channel);
            }
        }
    }
}
