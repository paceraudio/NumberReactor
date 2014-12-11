package com.paceraudio.numberreactor.app;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.TextView;


/**
 * Created by jeffwconaway on 12/4/14.
 */
public class ResetNextTurnAsync extends AsyncTask<Integer, Integer, Void> {

    private static final String DEBUG_TAG = "ResetNextTurnAsync";

    TextView mCounterTV;
    ResetNextTurnListener mListener;
    Context mContext;
//    int mTextColor;
    boolean mHasFadeIn = false;

    private static final int LAST_TURN_RESET_BEFORE_NEW_ACTIVITY = -1;
    private static final int NORMAL_TURN_RESET = 0;



    public ResetNextTurnAsync(ResetNextTurnListener listener, Context context, TextView tv) {
        this.mListener = listener;
        this.mContext = context;
        this.mCounterTV = tv;
    }

    @Override
    protected Void doInBackground(Integer... integers) {

//        Display the counter at full alpha for an amount of time
        setDisplayBeforeFade(1000);

//        Check to see if this is the last turn of Counter Activity.  If so, do not
//        fade the new counter at "0.00" in.  We only have a fade out.
        if (integers[0] == LAST_TURN_RESET_BEFORE_NEW_ACTIVITY) {
            fadeTextOut(getTextColor(mCounterTV), 2000);
        }
        if (integers[0] == NORMAL_TURN_RESET) {
            fadeTextOut(getTextColor(mCounterTV), 2000);
            mHasFadeIn = true;
            fadeTextIn(mContext.getResources().getColor(R.color.white), 2000);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... integers) {
        super.onProgressUpdate(integers);
        if (mHasFadeIn) {
            mCounterTV.setText(mContext.getText(R.string.zero_point_zero));
        }
        mCounterTV.setTextColor(Color.argb(integers[0], integers[1], integers[2], integers[3]));
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mListener.onNextTurnReset();
    }

    private void setDisplayBeforeFade(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void fadeTextOut(int[] rgbValues, long fadeTime) {
        int alpha = rgbValues[0];
        int red = rgbValues[1];
        int green = rgbValues[2];
        int blue = rgbValues[3];

        int futureValue;
        int currentValue = alpha;
        long elapsedTime;

        long startTime = SystemClock.elapsedRealtime();

        while ((elapsedTime = SystemClock.elapsedRealtime() - startTime) < fadeTime) {
            if (currentValue > 0) {
                futureValue = 255 - ((int) ((double) (elapsedTime * 255 / fadeTime)));
                if (futureValue < currentValue) {
                    currentValue = futureValue;
                    publishProgress(currentValue, red, green, blue);
                }
            }
        }
    }

    private void fadeTextIn(int colorToBe, long fadeTime) {
        int alpha = Color.alpha(colorToBe);
        int red = Color.alpha(colorToBe);
        int green = Color.green(colorToBe);
        int blue = Color.blue(colorToBe);

        int futureValue;
        int currentValue = 0;
        long elapsedTime;

        long startTime = SystemClock.elapsedRealtime();
        while ((elapsedTime = SystemClock.elapsedRealtime() - startTime) < fadeTime) {
            if (currentValue < alpha) {
                futureValue = (int ) ((double) (elapsedTime * 255) / fadeTime);
                if (futureValue > currentValue) {
                    currentValue = futureValue;
                    publishProgress(currentValue, red, green, blue);
                }
            }
        }
    }
}
