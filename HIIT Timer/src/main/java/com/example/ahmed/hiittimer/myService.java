package com.example.ahmed.hiittimer;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.Serializable;

/**
 * Created by Ahmed on 7/18/2017.
 */

public class myService extends Service {
    public static final String BROADCAST_ACTION = "com.MainActivity";
    Intent sendIntent;
    private Handler handler = new Handler();
    myTimer T;
    MediaPlayer beepSound;
    MediaPlayer doublebeepSound;
    int mNotificationId = 001;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         T = (myTimer) intent.getSerializableExtra("theTimer");
        beepSound = MediaPlayer.create(this, R.raw.beep);
        doublebeepSound = MediaPlayer.create(this, R.raw.beepbeep);
        handler.removeCallbacks(updateThread);

        T.inService=true;
        handler.postDelayed(updateThread, 0);

 /*       final Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
*/


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("HIIT Timer Running !")
                        .setContentText("Click to return")
                        .setOngoing(true)
                        .setAutoCancel(true);

        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);



// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

// Builds the notification and issues it.

        mNotifyMgr.notify(mNotificationId, mBuilder.build());

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        sendIntent = new Intent(BROADCAST_ACTION);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateThread);

        sendIntent.putExtra("theTimer",T);
        sendBroadcast(sendIntent);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mNotificationId);
        stopSelf();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    Runnable updateThread = new Runnable() {
        @Override
        public void run() {
            T.timeinMillis = SystemClock.uptimeMillis() - T.startTime;
            T.updateTime = T.timeSwap + T.timeinMillis;
            int milli = (int) (T.updateTime % 1000);
            int secs = (int) (T.updateTime / 1000);
            int mins = secs / 60;
            //beepSound.start();

            T.totalTime = T.intervals * (T.lowInterval + T.highInterval);
            if (secs < T.totalTime) {
                if (T.low) {

                    T.intervalTimer = SystemClock.uptimeMillis() - T.intervalStart;
                    if (T.intervalTimer > T.lowInterval * 1000) {
                        doublebeepSound.start();
                        T.high = true;
                        T.low = false;
                        //T.intervalStart = SystemClock.uptimeMillis();
                        T.intervalStart+=T.lowInterval*1000;
                        Vibrate(900);
                    }

                } else if (T.high) {

                    T.intervalTimer = SystemClock.uptimeMillis() - T.intervalStart;
                    if (T.intervalTimer > T.highInterval * 1000) {
                        beepSound.start();
                        T.high = false;
                        T.low = true;
                        //T.intervalStart = SystemClock.uptimeMillis();
                        T.intervalStart+=T.highInterval*1000;
                        Vibrate(500);

                    }


                }


                int secstodisp = secs % 60;


                //timer.setText(mins + ":" + String.format("%02d", secstodisp) + ":" + String.format("%03d", milli));


                handler.postDelayed(updateThread, 0);
            } else {
                beepSound.start();
                makeToast("Good job :) don't forget to stay hydrated");
                Vibrate(1400);
                T.Reset();



                //timer.setText((T.totalTime / 60) + ":" + String.format("%02d", (T.totalTime % 60)) + ":000");

            }
        }
    };

    public void Vibrate(long period){

        Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(period);

    }

    public void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();

    }
}
