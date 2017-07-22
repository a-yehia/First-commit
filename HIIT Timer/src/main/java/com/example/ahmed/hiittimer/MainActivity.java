package com.example.ahmed.hiittimer;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    boolean backClicked =false;
    private AlphaAnimation AAbuttonClick = new AlphaAnimation(1F, 0.8F);
    //boolean working = false;
    //long onPause;
    TextView lowText;
    TextView highText;
    TextView intervalsText;
    Button StartBut;
    Button PauseBut;
    TextView timer;
    Handler handler = new Handler();
    //int highInterval = 60; // 2 beeps at the beginning of high interval
    //int lowInterval = 60; // 1 beep at the beginning of low interval
    //int intervals = 10;
    //int totalTime;
    MediaPlayer beepSound;
    MediaPlayer doublebeepSound;
    //Long startTime = 0L, timeinMillis = 0L, timeSwap = 0L, updateTime = 0L;
    //boolean low = true, high = false;
    //long intervalTimer, intervalStart;
    View popupView;
    Button doneBut;
    Intent intent;

    myTimer T ;
    Runnable updateThread = new Runnable() {
        @Override
        public void run() {
            T.timeinMillis = SystemClock.uptimeMillis() - T.startTime;
            T.updateTime = T.timeSwap + T.timeinMillis;
            int milli = (int) (T.updateTime % 1000);
            int secs = (int) (T.updateTime / 1000);
            int mins = secs / 60;


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
                       // T.intervalStart = SystemClock.uptimeMillis();
                        T.intervalStart+=T.highInterval*1000;
                        Vibrate(500);

                    }


                }


                int secstodisp = secs % 60;


                timer.setText(mins + ":" + String.format("%02d", secstodisp) + ":" + String.format("%03d", milli));


                handler.postDelayed(this, 0);
            } else {
                beepSound.start();
                makeToast("Good job :) don't forget to stay hydrated");
                Vibrate(1400);
                T.Reset();



                timer.setText((T.totalTime / 60) + ":" + String.format("%02d", (T.totalTime % 60)) + ":000");

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        T = new myTimer();
        beepSound = MediaPlayer.create(this, R.raw.beep);
        doublebeepSound = MediaPlayer.create(this, R.raw.beepbeep);
        StartBut = (Button) findViewById(R.id.startButID);
        PauseBut = (Button) findViewById(R.id.pauseButID);
        timer = (TextView) findViewById(R.id.timeViewerID);


        //totalTime = intervals*(lowInterval+highInterval);

        final LayoutInflater popupInflater = getLayoutInflater();

        popupView = popupInflater.inflate(R.layout.popup_window, null);

        lowText = (TextView) findViewById(R.id.lowText);
        lowText.setText(Integer.toString(T.lowInterval));
        highText = (TextView) findViewById(R.id.highText);
        highText.setText(Integer.toString(T.highInterval));
        intervalsText = (TextView) findViewById(R.id.intervalsText);
        intervalsText.setText(Integer.toString(T.intervals));


        registerReceiver(broadcastReceiver, new IntentFilter(myService.BROADCAST_ACTION));
    }


    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            backClicked=true;
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isMyServiceRunning(myService.class)) {
            intent= new Intent(MainActivity.this,myService.class);
            stopService(intent);
        }



    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            T = (myTimer) intent.getSerializableExtra("theTimer");
            T.inService=false;
            if (T.working)
            {
                //makeToast("Helllooo");//TO DO @@@@@@@@@@@@@@@@@@@@@@@@@
                handler.postDelayed(updateThread, 0);
            }
            else {
                timer.setText((T.totalTime / 60) + ":" + String.format("%02d", (T.totalTime % 60)) + ":000");
            }
            SetVariables(T.lowInterval,T.highInterval,T.intervals);
        }
    };

    @Override
    protected void onStop() {
        super.onStop();

        if(T.working & !backClicked) {

            handler.removeCallbacks(updateThread);
            intent= new Intent(MainActivity.this,myService.class);
            intent.putExtra("theTimer",T);
            startService(intent);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isMyServiceRunning(myService.class)) {
            intent= new Intent(MainActivity.this,myService.class);
            stopService(intent);
        }
        handler.removeCallbacks(updateThread);

    }

    public void onStartClick(View view) {
        view.startAnimation(AAbuttonClick);
        if (!T.working) {
            T.working = true;
            if (T.startTime == 0L) {
                T.intervalStart = T.startTime = SystemClock.uptimeMillis();
            } else {
                T.intervalStart += SystemClock.uptimeMillis() - T.onPause;
                T.startTime = SystemClock.uptimeMillis();
            }

            handler.postDelayed(updateThread, 0);
        }

    }

    public void onPauseClick(View view) {
        view.startAnimation(AAbuttonClick);
        if (T.working) {
            T.working = false;
            T.timeSwap += T.timeinMillis;
            T.onPause = SystemClock.uptimeMillis();
            handler.removeCallbacks(updateThread);
        }

    }

    public void onResetClick(View view) {
        view.startAnimation(AAbuttonClick);
        T.working = false;
        handler.removeCallbacks(updateThread);
        T.Reset();
        timer.setText("0:00:000");

    }



    public void makeToast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();

    }

    public void Vibrate(long period){
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(period);


    }

    public void onSettingsClick(View view) {
        view.startAnimation(AAbuttonClick);

        if (T.working) {
            makeToast("Pause First!");
        } else {
            LayoutInflater li = LayoutInflater.from(this);
            View popWindow = li.inflate(R.layout.popup_window, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);


            alertDialogBuilder.setView(popWindow);

            final EditText userInputLow = (EditText) popWindow.findViewById(R.id.lowID);
            final EditText userInputHigh = (EditText) popWindow.findViewById(R.id.highID);
            final EditText userInputIntervals = (EditText) popWindow.findViewById(R.id.intervalsID);
            userInputLow.setText(String.valueOf(T.lowInterval));
            userInputHigh.setText(String.valueOf(T.highInterval));
            userInputIntervals.setText(String.valueOf(T.intervals));

            ;
            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // get user input and set it to result
                                    // edit text
                                    if (userInputLow.getText().toString().matches("") || userInputHigh.getText().toString().matches("") || userInputIntervals.getText().toString().matches("")) {
                                        makeToast("Please Fill All Fields!");
                                    } else {
                                        SetVariables(Integer.parseInt(userInputLow.getText().toString()), Integer.parseInt(userInputHigh.getText().toString()), Integer.parseInt(userInputIntervals.getText().toString()));
                                        T.Reset();
                                        timer.setText("0:00:000");
                                    }
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });


            AlertDialog alertDialog = alertDialogBuilder.create();


            alertDialog.show();

        }
    }

    public void onTextClick(View view)
    {
        onSettingsClick(view);

    }

    public void onHelpClick(View view)
    {
        view.startAnimation(AAbuttonClick);
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
        helpBuilder.setTitle("Help");
        helpBuilder.setMessage("-What is HIIT (High Intensity Interval Training)?\nHIIT is the concept where one performs a short burst of high-intensity (or max-intensity) exercise followed by a brief low-intensity activity, repeatedly, until too exhausted to continue.\n" +
                "-How to use HIIT Timer ?\nFirst set your intervals' time and set the number of intervals you want to do\n" +
                "Put on your headphones to hear the beeps or hold your device to feel the vibration and click Start\n" +
                "1 Beep is the start of a low interval while 2 Beeps are for high Intervals");
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();

    }
   public void SetVariables(int low, int high , int num)
   {
       T.lowInterval=low;
       T.highInterval=high;
       T.intervals=num;
       lowText.setText(String.valueOf(low));
       highText.setText(String.valueOf(high));
       intervalsText.setText(String.valueOf(num));

       T.totalTime = T.intervals*(T.lowInterval+T.highInterval);

   }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    }
