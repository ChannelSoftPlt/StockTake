package com.jby.stocktake.shareObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.jby.stocktake.R;
import com.jby.stocktake.home.HomeActivity;
import com.jby.stocktake.setting.UserAccountActivity;
import com.jby.stocktake.sharePreference.SharedPreferenceManager;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by user on 2/3/2018.
 */

public class NotificationSetUp {
    private Context context;
    private int id;
    private String url = "https://www.chafor.net";
    private String notificationTitle = "Chafor";
    private String notificationText = "This notification is sent from Chafor";
    private String version = "";

    public NotificationSetUp(Context context,Intent intent){
        this.context = context;
        this.id = intent.getIntExtra("id", 0);
        this.notificationText = intent.getStringExtra("message");
        if(intent.getStringExtra("url") != null)
            this.url = intent.getStringExtra("url");
        if(intent.getStringExtra("version") != null)
            this.version = intent.getStringExtra("version");
        checkingID();
    }

    private void checkingID(){
        if(this.id == 1)
            notificationReminderWhenAlmostExpire();
        else if(this.id == 2)
        {
            if(!SharedPreferenceManager.getVersion(context).equals(version))
                notificationReminderWhenNewUpdateRelease();
        }
        else
            notificationDefault();
    }

     private void notificationReminderWhenAlmostExpire(){
        // Prepare a notification with vibration, sound and lights
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.app_icon))
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setLights(Color.RED, 1000, 1000)
                .setVibrate(new long[]{0, 400, 250, 400})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, UserAccountActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));

        // Get an instance of the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
        // Build the notification and display it
    }

    private void notificationReminderWhenNewUpdateRelease(){
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        notificationIntent.setData(Uri.parse(url));
        PendingIntent pi = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        // Prepare a notification with vibration, sound and lights
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.app_icon))
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setLights(Color.RED, 1000, 1000)
                .setVibrate(new long[]{0, 400, 250, 400})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pi);

        // Get an instance of the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
        // Build the notification and display it
    }

    private void notificationDefault(){
        // Prepare a notification with vibration, sound and lights
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.app_icon))
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setLights(Color.RED, 1000, 1000)
                .setVibrate(new long[]{0, 400, 250, 400})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, HomeActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));

        // Get an instance of the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
        // Build the notification and display it
    }
}
