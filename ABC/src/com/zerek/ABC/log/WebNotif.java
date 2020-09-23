package com.zerek.ABC.log;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import com.zerek.ABC.R;

/**
 * Created with IntelliJ IDEA.
 * Date: 9/19/13
 * Time: 4:38 PM
 */

public class WebNotif {
    private static Ringtone rt;

    // Simple event TODO GMS
    public static class Event {
        public String compName;
        public int date;
        public String ticker;
        public String title;
        public String text;
        public String project;
        public String url;
    }

    public static void display(Intent i, Activity act, String sTag, String ticker, String title, String text, String sRingtone, boolean bVibration) {

        PendingIntent pendingIntent = PendingIntent.getActivity(act, 0, i, 0);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(act, 0, i, 0);
//        act.registerReceiver(receiver, new IntentFilter(NOTIFICATION_DELETED_ACTION));

        // New LIB
        NotificationCompat.Builder builder = new NotificationCompat.Builder(act);
        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.about_icon)
                .setTicker(ticker)
                .setContentTitle(title)
                .setContentText(Html.fromHtml(text));

        Notification notification = builder.getNotification(); // .build();

//        notification.defaults = Notification.DEFAULT_ALL;

//            notification.sound
        if (sRingtone != null) {
//         Only 1 at once
//        if (alarmNotif.size() == 1) {
            Uri uri = Uri.parse(sRingtone);
            if (rt == null || !rt.isPlaying())
                rt = RingtoneManager.getRingtone(act, uri);
            if (!rt.isPlaying())
                rt.play();
        }

        // Automatically delete from newWebFileDescs
        notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL; // Notification.FLAG_INSISTENT;

        if (bVibration)
            notification.vibrate = new long[]{100, 250, 100, 500};

        // Lights
        notification.ledARGB = Color.RED;
        notification.ledOffMS = 0;
        notification.ledOnMS = 1;

        NotificationManager nm = (NotificationManager) act.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(sTag, 0, notification);
    }
}
