package com.example.ahmed.hiittimer;

import android.media.MediaPlayer;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Ahmed on 7/18/2017.
 */


public class myTimer implements Serializable {

    boolean working = false;
    long onPause;
    public int highInterval = 60; // 2 beeps at the beginning of high interval
    public int lowInterval = 60; // 1 beep at the beginning of low interval
    public int intervals = 10;
    int totalTime;
    boolean inService= false;


    Long startTime = 0L, timeinMillis = 0L, timeSwap = 0L, updateTime = 0L;
    boolean low = true, high = false;
    long intervalTimer, intervalStart;




    public myTimer() {}

    public void Reset() {

        low = true;
        high = false;
        working = false;

        startTime = 0L;
        timeinMillis = 0L;
        timeSwap = 0L;
        updateTime = 0L;
    }


}
