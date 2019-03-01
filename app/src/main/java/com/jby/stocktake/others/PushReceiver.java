package com.jby.stocktake.others;

import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;

import com.jby.stocktake.shareObject.NotificationSetUp;

public class PushReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // Attempt to extract the "message" property from the payload: {"message":"Hello World!"}
        if (intent.getStringExtra("message") != null) {
            new NotificationSetUp(context, intent);
        }

//        // Prepare a notification with vibration, sound and lights
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
//                .setSmallIcon(R.drawable.logo)
//                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
//                        R.drawable.logo))
//                .setContentTitle(notificationTitle)
//                .setContentText(notificationText)
//                .setLights(Color.RED, 1000, 1000)
//                .setVibrate(new long[]{0, 400, 250, 400})
//                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, UserAccountActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
//
//        // Get an instance of the NotificationManager service
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
//
//        // Build the notification and display it
//        notificationManager.notify(1, builder.build());
    }
}
