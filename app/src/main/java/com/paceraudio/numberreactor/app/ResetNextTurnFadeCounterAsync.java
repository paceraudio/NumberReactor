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

        int color = mTextView.getCurrentTextColor();
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        int currentValue;
        int lastValue = alpha;
        long startTime = SystemClock.elapsedRealtime();
        long elapsedTime = 0;
        publishProgress(lastValue);
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
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mTextView.setTextColor(Color.argb(values[0], values[1], values[2], values[3]));

    }
}
