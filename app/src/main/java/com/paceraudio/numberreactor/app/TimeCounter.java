package com.paceraudio.numberreactor.app;

import android.os.SystemClock;

/**
 * Created by jeffwconaway on 9/21/14.
 */
public class TimeCounter {

/*
    long startTime;
    long elapsedTimeMillis;
    double elapsedSecondsPointTenths;
    double secondsCount;
    double acceleratedCount;
    double accelerator;
    int count = 0;

    public TimeCounter(long startTime, double acceleratedCount, double accelerator) {
        this.startTime = startTime;
        this.acceleratedCount = acceleratedCount;
        this.accelerator = accelerator;
    }

    public TimeCounter(long startTime, double elapsedSecondsPointTenths) {
        this.startTime = startTime;
        this.elapsedSecondsPointTenths = elapsedSecondsPointTenths;
    }

    public long calcElapsedMillis(long startTime) {
        elapsedTimeMillis =  System.currentTimeMillis() - startTime;
        return elapsedTimeMillis;
    }

    public TimeCounter calcElapsedSecondsPointTenths(TimeCounter timeCounter) {
        timeCounter.elapsedTimeMillis = System.currentTimeMillis() - timeCounter.startTime;
        timeCounter.elapsedSecondsPointTenths = (float) timeCounter.elapsedTimeMillis / 1000;
        return timeCounter;
    }
*/
    public static double calcElapsedSeconds(long startTime) {
        double elapsedTimeSeconds = (SystemClock.elapsedRealtime() - startTime) / 1000.00;
        return elapsedTimeSeconds;
    }

    public static double calcElapsedAcceleratedCount(long startTime, double accelerator) {
        long elapsedTimeMillis = SystemClock.elapsedRealtime() - startTime;
        double acceleratedCount = (elapsedTimeMillis / 1000.00) * accelerator;
        return acceleratedCount;
    }

//    public static double calcElapsedInterval(long startTime, long interval, double accelCount) {
//        if ()
//    }

/*
    public TimeCounter calcElapsedAcceleratedCountObj(TimeCounter timeCounter) {
        timeCounter.elapsedTimeMillis = System.currentTimeMillis() - timeCounter.startTime;
        timeCounter.elapsedSecondsPointTenths = (double) timeCounter.elapsedTimeMillis / 1000;
        timeCounter.acceleratedCount = timeCounter.elapsedSecondsPointTenths * timeCounter.accelerator;
        return timeCounter;
    }
*/


}
