package com.paceraudio.numberreactor.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;



/**
 * Created by jeffwconaway on 12/4/14.
 */
public class ResetNextTurnAsync extends AsyncTask<Integer, Integer, Void> {

    private static final String DEBUG_TAG = "ResetNextTurnAsync";

    TextView mCounterTV;
    ResetNextTurnListener mListener;
    Context mContext;
    int mTextColor;
    boolean isFadingIn = false;

    private static final int LAST_TURN_RESET_BEFORE_NEW_ACTIVITY = -1;
//    private static final int NORMAL_TURN_RESET = 0;

    private boolean mIsLastTurn = false;


    public ResetNextTurnAsync(ResetNextTurnListener listener, Context context, TextView tv) {
        this.mListener = listener;
        this.mContext = context;
        this.mCounterTV = tv;
    }

    @Override
    protected Void doInBackground(Integer ...integers) {

//        Check to see if this is the last turn of Counter Activity.  If so, do not
//        fade the new counter at "0.00" in.  We only have a fade out.
        if (integers[0] == LAST_TURN_RESET_BEFORE_NEW_ACTIVITY) {
            mIsLastTurn = true;
        }

/*
        //        Get the current text color for the counter
        mTextColor = mCounterTV.getCurrentTextColor();
        int alpha = Color.alpha(mTextColor);
        int red = Color.red(mTextColor);
        int green = Color.green(mTextColor);
        int blue = Color.blue(mTextColor);
*/
       // getTextColor(mCounterTV);

        //        Do nothing for 1 second
/*
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
//

        setDisplayBeforeFade(1000);

        fadeTextOut(getTextColor(mCounterTV), 2000);



/*
        long startTime = SystemClock.elapsedRealtime();
        long elapsedTime;
        int currentValue;
        int lastValue = alpha;
        publishProgress(lastValue, red, green, blue);

//        currentValue gets subtracted from 255 to produce a negative alpha fade in counter text
        while ((elapsedTime = SystemClock.elapsedRealtime() - startTime) < 2000) {
            currentValue = 255 - (int) ((float) (elapsedTime * 255) / 2000);
            if (currentValue < lastValue) {
                lastValue = currentValue;
                publishProgress(lastValue, red, green, blue);
            }
        }
*/

//        If this is after the last turn, skip this, no fade in
        if (!mIsLastTurn) {
//        reset startTime for positive alpha fade in
            lastValue = 0;
            startTime = SystemClock.elapsedRealtime();

//        reset rgb values for a white fade in
            int color = mContext.getResources().getColor(R.color.white);
            red = Color.red(color);
            green = Color.green(color);
            blue = Color.blue(color);
            isFadingIn = true;
            while ((elapsedTime = SystemClock.elapsedRealtime() - startTime) < 1000) {
                currentValue = (int) ((float) (elapsedTime * 255) / 1000);
                if (currentValue > lastValue) {
                    lastValue = currentValue;
                    publishProgress(lastValue, red, green, blue);
                }
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

    private int[] getTextColor(TextView tv) {
//        Get the current text color for the counter
        int color = tv.getCurrentTextColor();
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        int[] rgbValues = {alpha, red, green, blue};
        return rgbValues;
    }

    private void setDisplayBeforeFade(long millis) {
        try{
            Thread.sleep(millis);
        } catch (Exception e) {e.printStackTrace();}
    }

    private void fadeTextOut(int[] rgbValues, long fadeTime) {
        int alpha = rgbValues[0];
        int red = rgbValues[1];
        int green = rgbValues[2];
        int blue = rgbValues[3];

        int futureValue;
        int currentValue = alpha;
        long elapsedTime;

        long startTime = SystemClock.elapsedRealtime();
        if (currentValue > 0) {
            while ((elapsedTime = SystemClock.elapsedRealtime() - startTime) < fadeTime) {
                futureValue = 255 - ((int) ((double) (elapsedTime * 255 / fadeTime)));
                if(futureValue < currentValue) {
                    currentValue = futureValue;
                    publishProgress(currentValue, red, green, blue);
                }
            }
        }
    }

    private void fadeTextIn(Color colorToBe, long fadeTime) {
        int color = mContext.getResources().getColor(R)

    }

}
