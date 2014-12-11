package com.paceraudio.numberreactor.app;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.TextView;

/**
 * Created by jeffwconaway on 12/10/14.
 */
public class ResetNextTurnFadeCounterAsync extends AsyncTask<Void, Integer, Void> {

    ResetNextTurnFadeCounterListener mListener;
    Context mContext;
    TextView mTextView;

    public ResetNextTurnFadeCounterAsync(ResetNextTurnFadeCounterListener listener, Context context, TextView textView) {
        this.mListener = listener;
        this.mContext = context;
        this.mTextView = textView;
    }

    @Override
    protected Void doInBackground(Void... voids) {

//        Do nothing for 1 second
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        int color = mTextView.getCurrentTextColor();
        int color = mContext.getResources().getColor(R.color.blackRed);
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        int currentValue;
        int lastValue = alpha;
        long startTime = SystemClock.elapsedRealtime();
        long elapsedTime = 0;
//        publishProgress(lastValue, red, green, blue);
        while ((elapsedTime = (SystemClock.elapsedRealtime() - startTime)) < 3000) {

                    currentValue = 255 - (int) ((float) (elapsedTime * 255) / 3000);
                    if (currentValue < lastValue) {
                        lastValue = currentValue;
                        publishProgress(lastValue, red, green, blue);
                    }
            }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mListener.onFadeCounterTurnReset();
//        TODO have FadeCounterActivity implement this method, put the intent to return to CounterActivity there
    }

    @Override
    protected void onProgressUpdate(Integer ...integers) {
        super.onProgressUpdate(integers);
        mTextView.setTextColor(Color.argb(integers[0], integers[1], integers[2], integers[3]));

    }
}
