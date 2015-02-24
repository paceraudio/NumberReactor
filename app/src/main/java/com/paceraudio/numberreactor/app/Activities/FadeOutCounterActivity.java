package com.paceraudio.numberreactor.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.paceraudio.numberreactor.app.application.ApplicationState;
import com.paceraudio.numberreactor.app.db.DBHelper;
import com.paceraudio.numberreactor.app.util.ButtonDrawableView;
import com.paceraudio.numberreactor.app.util.GameInfoDisplayer;
import com.paceraudio.numberreactor.app.R;
import com.paceraudio.numberreactor.app.util.ResetNextTurnAsync;
import com.paceraudio.numberreactor.app.util.ResetNextTurnListener;


public class FadeOutCounterActivity extends FragmentActivity implements
        ResetNextTurnListener, View.OnTouchListener {

    static final String DEBUG_TAG = "jwc";

    private static TextView tvFadeCounter;
    private TextView mTvFadeTarget;
    private TextView mTvFadeAccuracy;
    private TextView mTvFadeLives;
    private TextView mTvFadeScore;
    private TextView mTvFadeLevel;

    private static Button fadeStartButton;
    private static Button fadeStopButton;
    private static FrameLayout fadeStartFrame;
    private static FrameLayout fadeStopFrame;

    //TODO see if this works
    private static LayerDrawable startButtonDisengagedDrawables;
    private static LayerDrawable startButtonEngagedDrawables;
    private static LayerDrawable startButtonArmedDrawables;
    private static LayerDrawable stopButtonDisengagedDrawables;
    private static LayerDrawable stopButtonEngagedDrawables;
    private static LayerDrawable stopButtonArmedDrawables;

    private static boolean isStartClickable;
    private static boolean isStartFlashing;
    private static boolean isStopFlashing;
    private static boolean isStopClickable;


    private static long elapsedTimeMillis;
    private double mTarget;
    private double mFadeIncrement;
    private double mRunningFadeTime;

    private int mFadeCounterColor;
    private static int alphaValue;
    private static int redValue;
    private static int greenValue;
    private static int blueValue;


    private static final String FROM_FADE_COUNTER_ACTIVITY = "fromFadeCounterActivity";

    private static final int LAST_TURN_RESET_BEFORE_NEW_ACTIVITY = -1;

    private static final int DOUBLE_LIVES_GAINED = 2;
    private static final int LIFE_GAINED = 1;
    private static final int LIFE_NEUTRAL = 0;

    private static final double DEFAULT_FADE_COUNTER_TARGET = 10.00;
    private static final int BUFFER_OVER_TARGET = 10;
    private static final double MILLIS_IN_SECONDS = 1000;
    private static final String DOUBLE_FORMAT = "%.2f";
    private static long counterCeilingMillis;
    private static final double DEFAULT_FADE_RATIO = .60;
    private static final int ALPHA_VALUE_STEPS = 255;

    //private static final long MAX_COUNTER_VALUE_MILLIS = 99999;
    private static final long COUNTER_INCREMENT_MILLIS = 10;

    // Constants for armed button flashing
    private static final long ARMED_START_BUTTON_FLASH_DURATION = 500;
    private static final long ARMED_STOP_BUTTON_FLASH_DURATION = 90;


    DBHelper mDbHelper;
    ApplicationState mState;
    static GameInfoDisplayer gameInfoDisplayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fade_out_counter);
        mState = (ApplicationState) getApplicationContext();
        mDbHelper = new DBHelper(this);
        tvFadeCounter = (TextView) findViewById(R.id.t_v_fade_counter);
        mTvFadeTarget = (TextView) findViewById(R.id.t_v_fade_target);
        mTvFadeAccuracy = (TextView) findViewById(R.id.t_v_fade_accuracy_rating);
        mTvFadeLives = (TextView) findViewById(R.id.t_v_fade_lives_remaining);
        mTvFadeScore = (TextView) findViewById(R.id.t_v_fade_score);
        mTvFadeLevel = (TextView) findViewById(R.id.t_v_fade_level);
        fadeStartButton = (Button) findViewById(R.id.b_fade_start);
        fadeStopButton = (Button) findViewById(R.id.b_fade_stop);
        fadeStartFrame = (FrameLayout) findViewById(R.id.f_l_for_fade_b_start);
        fadeStopFrame = (FrameLayout) findViewById(R.id.f_l_for_fade_b_stop);

        gameInfoDisplayer = new GameInfoDisplayer(this, this);

        ButtonDrawableView buttonDrawableView = new ButtonDrawableView(this);
        startButtonDisengagedDrawables = buttonDrawableView.mStartDisengagedDrawables;
        startButtonEngagedDrawables = buttonDrawableView.mStartEngagedDrawables;
        startButtonArmedDrawables = buttonDrawableView.mStartArmed;

        stopButtonDisengagedDrawables = buttonDrawableView.mStopDisengagedDrawables;
        stopButtonEngagedDrawables = buttonDrawableView.mStopEngagedDrawables;
        stopButtonArmedDrawables = buttonDrawableView.mStopArmed;

        gameInfoDisplayer.showButtonState(fadeStopButton, stopButtonDisengagedDrawables);

        setStateTargetBasedOnLevel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //        This is when the counter should have completely faded out.
        double fadeRange = DEFAULT_FADE_COUNTER_TARGET * DEFAULT_FADE_RATIO;
        //        This assumes the alpha value of the color is at its max
        mFadeIncrement = fadeRange / 255;
        mRunningFadeTime = mFadeIncrement;
        mFadeCounterColor = tvFadeCounter.getCurrentTextColor();
        alphaValue = Color.alpha(mFadeCounterColor);
        redValue = Color.red(mFadeCounterColor);
        greenValue = Color.green(mFadeCounterColor);
        blueValue = Color.blue(mFadeCounterColor);
        fadeStartButton.setOnTouchListener(this);
        fadeStopButton.setOnTouchListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameInfoDisplayer.displayAllGameInfo(mTvFadeTarget, mTvFadeAccuracy, mTvFadeLives,
                mTvFadeScore, mTvFadeLevel, FROM_FADE_COUNTER_ACTIVITY);
        isStartClickable = true;
        isStopClickable = false;
        flashStartButton();
 
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fade_out_counter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_view_game_stats) {
            Intent intent = new Intent(this, ViewStatsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onFadeCountStopped(long millis) {

        gameInfoDisplayer.showButtonState(fadeStopButton, stopButtonEngagedDrawables);
        gameInfoDisplayer.showButtonState(fadeStartButton, startButtonDisengagedDrawables);

        double counterCeilingSeconds = (millis / 1000d);

        //      Round the elapsed accelerated count to 2 decimal places
        double roundedCount = mState.roundElapsedCountLong(millis, FROM_FADE_COUNTER_ACTIVITY, counterCeilingSeconds);
        Log.d("jwc", "onFadeCountStopped millis: " + millis);
        Log.d("jwc", "onFadeCountStopped roundedCount: " + roundedCount);

        //      Convert rounded value to a String to display
        String roundedCountStr = String.format("%.2f", roundedCount);

        //        calc the accuracy
        int accuracy = mState.calcAccuracy(mTarget, roundedCount);

        mState.setmTurnAccuracy(accuracy);

        if (roundedCount == mTarget) {
            tvFadeCounter.setTextColor(getResources().getColor(R.color.glowGreen));
        } else {
            tvFadeCounter.setTextColor(mFadeCounterColor);
        }
        tvFadeCounter.setText(roundedCountStr);
        gameInfoDisplayer.displayImmediateGameInfoAfterFadeCountTurn(mTvFadeAccuracy);

        ResetNextTurnAsync resetNextTurnAsync = new ResetNextTurnAsync(this, this,
                tvFadeCounter, mTvFadeLives, mTvFadeScore);

        int param2;
        int param3;

        // See if there was a life gained or lost
        int livesGained = mState.numOfBonusLivesFadeCount();
        if (livesGained == DOUBLE_LIVES_GAINED) {
            param2 = DOUBLE_LIVES_GAINED;
        } else if (livesGained == LIFE_GAINED) {
            param2 = LIFE_GAINED;

        } else {
            param2 = LIFE_NEUTRAL;
        }

        // No score is awarded in this activity, so set the score to dummy value -1, so that the
        // score textview does not blink in the reset turn async
        param3 = -1;
        resetNextTurnAsync.execute(LAST_TURN_RESET_BEFORE_NEW_ACTIVITY, param2, param3);
    }

    private void setStateTargetBasedOnLevel() {
        mTarget = DEFAULT_FADE_COUNTER_TARGET + (mState.getLevel() - 1);
        mState.setTurnTarget(mTarget);
        counterCeilingMillis = (long) ((mTarget + BUFFER_OVER_TARGET) * MILLIS_IN_SECONDS);
    }

    //    Listener method runs after ResetNextTurnAsync is finished
    @Override
    public void onNextTurnReset() {
        Log.d(DEBUG_TAG, "ResetNextTurnAsync returning to FadeCounter!!!!");
        Intent intent = new Intent(this, CounterActivity.class);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void flashStartButton() {
        StartButtonArmedRunnable runnable = new StartButtonArmedRunnable();
        Thread startButtonArmedThread = new Thread(runnable);
        startButtonArmedThread.start();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == fadeStartButton && isStartClickable) {
            gameInfoDisplayer.showButtonState(fadeStartButton, startButtonEngagedDrawables);
            FadeCounterRunnable fadeCounterRunnable = new FadeCounterRunnable(this);
            Thread fadeCounterThread = new Thread(fadeCounterRunnable);
            fadeCounterThread.start();
            isStartClickable = false;
            isStopClickable = true;
        } else if (v == fadeStopButton && isStopClickable) {
            gameInfoDisplayer.showButtonState(fadeStopButton, stopButtonEngagedDrawables);
            isStopClickable = false;
            onFadeCountStopped(elapsedTimeMillis);
        }
        return false;
    }

    private static void updateFadeCounter(long elapsedMillis, int alpha) {
        if (isStopClickable) {
            double elapsedSeconds = elapsedMillis / MILLIS_IN_SECONDS;
            tvFadeCounter.setText(String.format(DOUBLE_FORMAT, elapsedSeconds));
            tvFadeCounter.setTextColor(Color.argb(alpha, redValue, greenValue, blueValue));
            elapsedTimeMillis = elapsedMillis;
        }
    }


    static void flashStartButtonArmed() {
        if (isStartClickable) {
            if (isStartFlashing) {
                gameInfoDisplayer.showButtonState(fadeStartButton, startButtonDisengagedDrawables );
                isStartFlashing = false;
            } else {
                gameInfoDisplayer.showButtonState(fadeStartButton, startButtonArmedDrawables);
                isStartFlashing = true;
            }
        }
    }

    static void flashStopButtonArmed() {
        if (isStopClickable) {
            if (!isStopFlashing) {
                gameInfoDisplayer.showButtonState(fadeStopButton, stopButtonArmedDrawables);
                isStopFlashing = true;
            } else {
                gameInfoDisplayer.showButtonState(fadeStopButton, stopButtonDisengagedDrawables);
                isStopFlashing = false;
            }
        }
    }


    class StartButtonArmedRunnable implements Runnable {

        public StartButtonArmedRunnable() {
            /*this.activity = activity;*/
        }

        @Override
        public void run() {
            showStartButtonArmed();
        }

        private void showStartButtonArmed() {
            long startTime = SystemClock.elapsedRealtime();
            long elapsedTime;
            //long flashDuration = ARMED_START_BUTTON_FLASH_DURATION;
            long runningFlashDuration = ARMED_START_BUTTON_FLASH_DURATION;
            while(isStartClickable) {
                elapsedTime = SystemClock.elapsedRealtime() - startTime;
                if (elapsedTime >= runningFlashDuration) {
                    FlashStartButtonRunnable runnable = new FlashStartButtonRunnable();
                    runOnUiThread(runnable);
                    runningFlashDuration += ARMED_START_BUTTON_FLASH_DURATION;
                }
            }
            gameInfoDisplayer.showButtonState(fadeStartButton, startButtonEngagedDrawables);
        }
    }


    class FlashStartButtonRunnable implements Runnable {
        @Override
        public void run() {
            flashStartButtonArmed();
        }
    }


    class FlashStopButtonRunnable implements  Runnable {
        @Override
        public void run() {
            flashStopButtonArmed();
        }
    }


    class UpdateCounterAfterTimeoutRunnable implements Runnable {
        long maxElapsedMillis;

        public UpdateCounterAfterTimeoutRunnable(long maxElapsedMillis) {
            this.maxElapsedMillis = maxElapsedMillis;
        }

        @Override
        public void run() {
            onFadeCountStopped(maxElapsedMillis);
        }
    }


    class UpdateFadeCounterRunnable implements Runnable {

        long elapsedMillis;
        int alphaValue;

        public UpdateFadeCounterRunnable(long elapsedMillis, int alphaValue) {
            this.elapsedMillis = elapsedMillis;
            this.alphaValue = alphaValue;
        }

        @Override
        public void run() {
            updateFadeCounter(elapsedMillis, alphaValue);
        }
    }

    class FadeCounterRunnable implements Runnable {

        Activity activity;

        public FadeCounterRunnable(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void run() {
            runFadeCounter();
        }

        private void runFadeCounter() {

            long elapsedMillis = 0;
            long duration = COUNTER_INCREMENT_MILLIS;
            long durationIncrement = duration;
            int totalFadeDuration = (int) ((DEFAULT_FADE_COUNTER_TARGET * DEFAULT_FADE_RATIO) *
                    MILLIS_IN_SECONDS);
            int fadeDurationIncrement = totalFadeDuration / ALPHA_VALUE_STEPS;
            int fadeDuration = fadeDurationIncrement;
            int alphaValue = ALPHA_VALUE_STEPS;

            // For the flashing of the armed stop button
            long runningFlashDuration = ARMED_STOP_BUTTON_FLASH_DURATION;

            long startTime = SystemClock.elapsedRealtime();

            while (elapsedMillis <= counterCeilingMillis && isStopClickable) {
                elapsedMillis = SystemClock.elapsedRealtime() - startTime;

                if (elapsedMillis >= duration) {
                    if (elapsedMillis >= fadeDuration && alphaValue > 0) {
                        alphaValue--;
                        fadeDuration += fadeDurationIncrement;
                    }
                    UpdateFadeCounterRunnable updateFadeCounterRunnable = new
                            UpdateFadeCounterRunnable(elapsedMillis, alphaValue);
                    runOnUiThread(updateFadeCounterRunnable);
                    duration += durationIncrement;

                    // Flashes stop button to show its armed
                    runningFlashDuration += showStopButtonArmed(elapsedMillis, runningFlashDuration);
                }
            }
            if (elapsedMillis >= counterCeilingMillis && isStopClickable) {
                UpdateCounterAfterTimeoutRunnable runnable = new UpdateCounterAfterTimeoutRunnable(elapsedMillis);
                runOnUiThread(runnable);
                Log.d("jwc", "elapsed Millis from runnable: " + elapsedMillis);
            }
        }

        private long showStopButtonArmed(long elapsed, long runningDur) {
            if (elapsed >= runningDur) {
                FlashStopButtonRunnable runnable = new FlashStopButtonRunnable();
                runOnUiThread(runnable);
                return ARMED_STOP_BUTTON_FLASH_DURATION;
            }
            return 0;
        }
    }
}
