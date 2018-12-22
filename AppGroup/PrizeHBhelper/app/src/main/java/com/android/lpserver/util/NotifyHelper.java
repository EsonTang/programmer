package com.android.lpserver.util;

import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import com.android.lpserver.Config;

import java.util.Calendar;

public class NotifyHelper {

    private static Vibrator sVibrator;
    private static KeyguardManager sKeyguardManager;
    private static PowerManager sPowerManager;

    /** Play sound*/
    public static void sound(Context context,final MediaPlayer mediaPlayer) {
        //prize modify v8.0 by zhaojian 2017921 start
        /*MediaPlayer mediaPlayer = new MediaPlayer().create(context, R.raw.hb_sound2);*/
        try{
            mediaPlayer.start();
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
        //prize modify v8.0 by zhaojian 2017921 end
    }

    /** Vibration*/
    public static void vibrator(Context context) {
        if(sVibrator == null) {
            sVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        sVibrator.vibrate(new long[]{100, 10, 100, 1000}, -1);
    }

    /** Whether for the night*/
    public static  boolean isNightTime() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if(hour >= 23 || hour < 7) {
            return true;
        }
        return false;
    }

    public static KeyguardManager getKeyguardManager(Context context) {
        if(sKeyguardManager == null) {
            sKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        }
        return sKeyguardManager;
    }

    public static PowerManager getPowerManager(Context context) {
        if(sPowerManager == null) {
            sPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        }
        return sPowerManager;
    }

    /** Whether the lock screen or black screen*/
    public static boolean isLockScreen(Context context) {
        KeyguardManager km = getKeyguardManager(context);

        return km.inKeyguardRestrictedInputMode() || !isScreenOn(context);
    }

    public static boolean isScreenOn(Context context) {             //If the screen is in an interactive state, return true
        PowerManager pm = getPowerManager(context);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return pm.isInteractive();
        } else {
            return pm.isScreenOn();
        }
    }

    /** Play effect, sound and vibration*/
    public static void playEffect(Context context, Config config,MediaPlayer mediaPlayer) {
        //prize-delete-v8.0 by zhaojian 2017918 start
        /*if(NotifyHelper.isNightTime() && config.isNotifyNight()) {
            return;
        }*/
        //prize-delete-v8.0 by zhaojian 2017918 end
        Log.d("debug","isNotifySound = " + config.isNotifySound());
        if(config.isNotifySound()) {
            sound(context,mediaPlayer);
        }

        /*if(config.isNotifyVibrate()) {
            vibrator(context);
        }*/
    }

    /** Display notification*/
    public static void showNotify(Context context, String title, PendingIntent pendingIntent) {

    }

    /** Execute PendingIntent event*/
    public static void send(PendingIntent pendingIntent) {
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}
