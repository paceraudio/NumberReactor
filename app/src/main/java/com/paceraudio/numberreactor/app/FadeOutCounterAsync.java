package com.paceraudio.numberreactor.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.TextView;

/**
 * Created by jeffwconaway on 9/20/14.
 */
public class FadeOutCounterAsync extends AsyncTask<Long, Double, Double> {

    //private long startTime,  elapsedTimeMillis, mTarget;
    private int alpha = 0xff;
//    private double elapsedTimeSecondsPointTenths = 0;
    double nextCount = 0.01;

    TextView tvFadeCounter;
    TextView tvTarget;

    FadeCounterListener mListener;

    TimeCounter mTimeCounter;

    public FadeOutCounterAsync(Activity activity, FadeCounterListener listener) {
        this.mListener = listener;
        tvFadeCounter = (TextView) activity.findViewById(R.id.t_v_fade_counter);
    }

    @Override
    protected Double doInBackground(Long... params) {
        long startTime = params[0];
        int buffer = 5;
        long target = params[1] + buffer;
        double elapsedSeconds = 0;
        while (elapsedSeconds < target && !isCancelled()) {

            if (elapsedSeconds >= nextCount && !isCancelled()) {
                publishProgress(elapsedSeconds);
                nextCount += 0.01;
            }
        }
        return elapsedSeconds;
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        tvFadeCounter.setText(String.format("%.2f", values[0]));
        int intValue = (int) (values[0] * 100);
        tvFadeCounter.setTextColor(Color.argb(alpha, 0xff, 0xff, 0xff));
        //if (intValue % 2 == 0) {
            if (alpha >= 1) {
                alpha -= 1;
            } else {
                alpha = 0;
            }
        //}
    }

    @Override
    protected void onPostExecute(Double seconds) {
        mListener.onFadeCountComplete(seconds);
    }

}
