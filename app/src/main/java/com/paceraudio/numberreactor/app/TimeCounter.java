package com.paceraudio.numberreactor.app;

/**
 * Created by jeffwconaway on 9/21/14.
 */
public class TimeCounter {

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

    public double calcElapsedAcceleratedCount(long startTime, double accelerator) {
        elapsedTimeMillis = System.currentTimeMillis() - startTime;
        elapsedSecondsPointTenths = (float) elapsedTimeMillis / 1000;
        acceleratedCount = elapsedSecondsPointTenths * accelerator;
        return acceleratedCount;
    }

    public TimeCounter calcElapsedAcceleratedCountObj(TimeCounter timeCounter) {
        timeCounter.elapsedTimeMillis = System.currentTimeMillis() - timeCounter.startTime;
        timeCounter.elapsedSecondsPointTenths = (double) timeCounter.elapsedTimeMillis / 1000;
        timeCounter.acceleratedCount = timeCounter.elapsedSecondsPointTenths * timeCounter.accelerator;
        return timeCounter;
    }


}
