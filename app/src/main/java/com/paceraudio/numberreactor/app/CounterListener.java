package com.paceraudio.numberreactor.app;

/**
 * Created by jeffwconaway on 9/15/14.
 */
public interface CounterListener {

    public void onCounterComplete(Double accelCount);

    public void onCounterCancelled(Double accelCount, int count);
}
