package com.triplaud.alarm;

import com.triplaud.UploadObject;
import com.triplaud.common.Common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class SetAlarm {
    private static AlarmManager manager;
    private static PendingIntent pendingIntent;
    private static Intent intent;
    private static long frequency;
    private static UploadObject obj;

    public static void setRepeatingAlarm(Context context, UploadObject obj) {
        intent = new Intent(context, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(context, 11111, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        frequency = Common.AlARM_INTERVAL;
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), frequency, pendingIntent);
    }

    public static void setRepeatingAlarm(Context context, String friendId) {
        intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("FROMFRIEND", friendId);
        pendingIntent = PendingIntent.getBroadcast(context, 11111, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        frequency = Common.AlARM_INTERVAL;
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), frequency, pendingIntent);
    }

    public static void cancelRepeatingAlarm() {
        if(manager != null) {
            if(pendingIntent != null) {
                manager.cancel(pendingIntent);
            }
        }
    }

}
