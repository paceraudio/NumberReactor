package com.pacerdevelopment.numberreactor.app.util;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.TextView;

import com.pacerdevelopment.numberreactor.app.R;


/**
 * Created by jeffwconaway on 12/4/14.
 */
public class ResetNextTurnAsync extends AsyncTask<Integer, Integer, Void> {


    TextView mCounterTV;
    TextView mLivesTV;
    TextView mScoreTV;
    ResetNextTurnListener mListener;
    Context mContext;

    boolean mIsDisplayingStats = true;
    boolean mIsDoubleLifeGained = false;
    boolean mIsLifeGained = false;
    boolean mIsLiveNeutral = false;
    boolean mIsLifeLost = false;
    boolean mIsFadeOutDone = false;
    int mTurnPoints;

    private static final int LAST_TURN_RESET_BEFORE_NEW_ACTIVITY = -1;
    private static final int NORMAL_TURN_RESET = 0;

    private static final int DOUBLE_LIVES_GAINED = 2;
    private static final int LIFE_GAINED = 1;
    private static final int LIFE_NEUTRAL = 0;
    private static final int LIFE_LOST = -1;

    private static final long SHOW_STATS_DURATION = 1500;
    private static final long NUM_OF_BLINKS = 8;
    private static final long FADE_OUT_DURATION = 1200;
    private static final long FADE_IN_DURATION = 600;
    private static final int MAX_ALPHA = 255;

    private static final int ZERO = 0;
    private static final int TWO = 2;



    public ResetNextTurnAsync(ResetNextTurnListener listener, Context context,
                              TextView tvCounter, TextView tvLives, TextView tvScore) {
        this.mListener = listener;
        this.mContext = context;
        this.mCounterTV = tvCounter;
        this.mLivesTV = tvLives;
        this.mScoreTV = tvScore;
    }

    @Override
    protected Void doInBackground(Integer... integers) {

        if (integers[1] == DOUBLE_LIVES_GAINED) {
            mIsDoubleLifeGained = true;
        } else if (integers[1] == LIFE_GAINED) {
            mIsLifeGained = true;
        } else if (integers[1] == LIFE_LOST) {
            mIsLifeLost = true;
        } else if (integers[2] == LIFE_NEUTRAL) {
            mIsLiveNeutral = true;
        }

        mTurnPoints = integers[2];
        showStatsBeforeCounterReset(SHOW_STATS_DURATION);

        // Check to see if this is the last turn of Counter Activity.  If so, do not
        // fade the new counter at "0.00" in.  We only have a fade out.
        if (integers[0] == LAST_TURN_RESET_BEFORE_NEW_ACTIVITY) {
            fadeTextOut(getTextColor(mCounterTV), FADE_OUT_DURATION);
        }
        if (integers[0] == NORMAL_TURN_RESET) {
            fadeTextOut(getTextColor(mCounterTV), FADE_OUT_DURATION);
            mIsFadeOutDone = true;
            fadeTextIn(mContext.getResources().getColor(R.color.red), FADE_IN_DURATION);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... integers) {
        super.onProgressUpdate(integers);
        // If we are displaying the stats, integers[0] is used to get our on/off blinking by
        // modulo-ing the value.  Even is on, odd is off.  The other three integers[] values
        // are meaningless here.
        if (mIsDisplayingStats) {

            blinkTurnInfo(mIsDoubleLifeGained, mIsLifeGained, mIsLifeLost, integers[0], mTurnPoints);

            // If we are not displaying the stats and, instead, producing the fade out/in,
            // integers[0] is used to set the alpha value of the Counter text.  The other three
            // values in integers[] are the r g b values for the color.
        } else {
            mCounterTV.setTextColor(Color.argb(integers[0], integers[1], integers[2],
                    integers[3]));
            // Set the counter to 0.00 when the alpha value is at 0 before the fade in
            if (mIsFadeOutDone) {
                mCounterTV.setText(mContext.getString(R.string.zero_point_zero));
                mIsFadeOutDone = false;
            }
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mListener.onNextTurnReset();
    }

    private void showStatsBeforeCounterReset(long millis) {
        long elapsedTime;
        int futureValue;
        int currentValue = 0;
        long startTime = SystemClock.elapsedRealtime();

        while ((elapsedTime = SystemClock.elapsedRealtime() - startTime) <= millis) {
            futureValue = (int) (((double) elapsedTime * NUM_OF_BLINKS) / millis);
            if (futureValue > currentValue) {
                currentValue = futureValue;
                publishProgress(currentValue, ZERO, ZERO, ZERO);
            }
        }
        mIsDisplayingStats = false;
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

        while ((elapsedTime = SystemClock.elapsedRealtime() - startTime) <= fadeTime) {
            if (currentValue > 0) {
                futureValue = MAX_ALPHA - ((int) ((double) (elapsedTime * MAX_ALPHA / fadeTime)));
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
        int currentValue = ZERO;
        long elapsedTime;

        long startTime = SystemClock.elapsedRealtime();
        while ((elapsedTime = SystemClock.elapsedRealtime() - startTime) <= fadeTime) {
            if (currentValue < alpha) {
                futureValue = (int) ((double) (elapsedTime * MAX_ALPHA) / fadeTime);
                if (futureValue > currentValue) {
                    currentValue = futureValue;
                    publishProgress(currentValue, red, green, blue);
                }
            }
        }
    }

    private void blinkTurnInfo(boolean plusLives, boolean plusLife, boolean minusLife, int time, int points) {
        int green = mContext.getResources().getColor(R.color.green);
        boolean timeIsEven = (time % TWO == ZERO);
        boolean positivePoints = (points > ZERO);
        if (plusLives || plusLife || minusLife) {

            if (timeIsEven && plusLives) {
                mLivesTV.setTextColor(green);
                mLivesTV.setText(mContext.getString(R.string.lives_remaining) +
                        mContext.getString(R.string.plus_two));
            }
            else if (timeIsEven && plusLife) {
                mLivesTV.setTextColor(green);
                mLivesTV.setText(mContext.getString(R.string.lives_remaining) + mContext.getString(R.string.plus_one));
            }
            else if (timeIsEven) {
                mLivesTV.setText(mContext.getString(R.string.lives_remaining) + mContext.getString(R.string.minus_one));
            }
            if (!timeIsEven) {
                mLivesTV.setText(mContext.getString(R.string.empty_str));
            }
        }
        if (positivePoints) {
            mScoreTV.setTextColor(green);
            if (timeIsEven) {
                mScoreTV.setText(mContext.getText(R.string.points) + mContext.getString(R.string.plus_sign) + points);
            } else {
                mScoreTV.setText(mContext.getString(R.string.empty_str));
            }
        }
    }
}
