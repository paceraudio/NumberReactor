package com.paceraudio.numberreactor.app;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by jeffwconaway on 9/15/14.
 */
public class CounterAsync extends AsyncTask<Long, Double, Double> {
    // AsyncTask params - Long from execute(Long...), Float for publishProgress(Double), and Float for onPostExecute(Double)

    private static final String DEBUG_TAG = "jwc";

    private long startTime;
    private long target;
    private long elapsedTimeMillis;
    private double elapsedTimeSeconds;
    private double elapsedAcceleratedCount;
    private double nextCount;
    private double accelerator;
    private int mCount;

    private final CounterListener mListener;
    private final TextView tvCounter;
    private TimeCounter mTimeCounter;


    public CounterAsync(Activity activity, CounterListener listener) {
        this.mListener = listener;
        tvCounter = (TextView) activity.findViewById(R.id.t_v_counter);

    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Double doInBackground(Long... params) {

        // first param in execute()
        startTime = params[0];

        // upper buffer over the mTarget number
        int upperBuffer = 5;

        // second param in execute, adding 5 to give an upper buffer over the mTarget number
        target = params[1] + upperBuffer;

        // the mCount towards the mTarget accelerates
        elapsedAcceleratedCount = 0;
        mCount = 0;
        accelerator = 1.1f;
        elapsedTimeSeconds = 0;
        nextCount = 0.01;

        //tvCounter.setTextColor(0xffffffff);

        // instantiate a TimeCounter to manipulate in calcElapsedAcceleratedCount method
//        mTimeCounter = new TimeCounter(startTime, elapsedAcceleratedCount, accelerator);

        /* TODO tvCounter increments one more step, but is green in color i.e. mTarget is 5.0, counter is 5.1 but text is green.  Figure this out
         */


        //while (elapsedAcceleratedCount < mTarget && !isCancelled()) {
        while (elapsedAcceleratedCount < target) {
            // mTimeCounter calls method and passes itself as the parameter
//            mTimeCounter.calcElapsedAcceleratedCountObj(mTimeCounter);
            // get the member variables from mTimeCounter after the method has changed them
//            elapsedAcceleratedCount = mTimeCounter.acceleratedCount;
            // needed for logging only
//            elapsedTimeSeconds = mTimeCounter.elapsedSecondsPointTenths;
//            elapsedTimeMillis = mTimeCounter.elapsedTimeMillis;
//            if (elapsedAcceleratedCount >= nextCount) {
                publishProgress(elapsedAcceleratedCount);
                nextCount += 0.01;
//                mTimeCounter.accelerator *= 1.0004;
                mCount++;
            }
            if (isCancelled()) {
                //Log.d(DEBUG_TAG, "is cancelled in do in background");
                Log.d(DEBUG_TAG, "mTimeCounter via async elapsed time: " + elapsedTimeMillis);
                Log.d(DEBUG_TAG, "is cancelled accelerated mCount" +  Double.toString(elapsedAcceleratedCount));
//                break;
            }
//        }
        // this gets passed to the onPostExecute parameter
        return elapsedAcceleratedCount;
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        tvCounter.setText(String.format("%.2f", values[0]));
    }

    @Override
    protected void onCancelled() {
        mListener.onCounterCancelled(elapsedAcceleratedCount, mCount);
    }

    @Override
    protected void onPostExecute(Double accelCount) {
        Log.d(DEBUG_TAG, "on post execute running");
        mListener.onCounterComplete(accelCount);
    }
}
