package com.paceraudio.numberreactor.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;



/**
 * Created by jeffwconaway on 12/4/14.
 */
public class ResetNextTurnAsync extends AsyncTask<Void, Integer, Void> {

    private static final String DEBUG_TAG = "ResetNextTurnAsync";

    TextView mCounterTV;
    ResetNextTurnListener mListener;
    int mTextColor;
    boolean isFadingIn = false;


    public ResetNextTurnAsync(Activity activity, ResetNextTurnListener listener, TextView tv) {
       // this.mCounterTV = (TextView) activity.findViewById(R.id.t_v_counter);
        this.mListener = listener;
        this.mCounterTV = tv;
    }

    @Override
    protected Void doInBackground(Void ...voids) {
        mTextColor = mCounterTV.getCurrentTextColor();
        int red = Color.red(mTextColor);
        int green = Color.green(mTextColor);
        int blue = Color.blue(mTextColor);

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long startTime = SystemClock.elapsedRealtime();
        long elapsedTime;
        int currentValue;
        int lastValue = 255;

        publishProgress(lastValue, red, green, blue);
//        currentValue gets subtracted from 255 to produce a negative alpha fade in counter text
        while ((elapsedTime = SystemClock.elapsedRealtime() - startTime) < 2000) {
            currentValue = 255 - (int) ((float) (elapsedTime * 255) / 2000);
            if (currentValue < lastValue) {
                lastValue = currentValue;
                publishProgress(lastValue, red, green, blue);
            }
        }
//        reset startTime for positive alpha fade in
        lastValue = 0;
        startTime = SystemClock.elapsedRealtime();

//        reset rgb values for a white fade in
        red = 255;
        green = 255;
        blue = 255;
        isFadingIn = true;
        while ((elapsedTime = SystemClock.elapsedRealtime() - startTime) < 1000) {
            currentValue = (int) ((float) (elapsedTime * 255) / 1000);
            if (currentValue > lastValue) {
                lastValue = currentValue;
                publishProgress(lastValue, red, green, blue);
            }

        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer ...integers) {
        super.onProgressUpdate(integers);
        int fadeTextColor = Color.argb(integers[0], integers[1], integers[2], integers[3]);
        mCounterTV.setTextColor(fadeTextColor);
//        TODO make a listener to fire and call all the reset methods in counter activity that get called w reset button. get rid of reset button
        if (isFadingIn) {
            mCounterTV.setText("0.00");
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mListener.onNextTurnReset();
    }

}
