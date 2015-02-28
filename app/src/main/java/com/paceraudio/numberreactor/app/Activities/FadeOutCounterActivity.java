package com.paceraudio.numberreactor.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.paceraudio.numberreactor.app.R;
import com.paceraudio.numberreactor.app.util.ResetNextTurnListener;


public class FadeOutCounterActivity extends TimeCounter implements
        ResetNextTurnListener, View.OnTouchListener {

    private static TextView tvFadeCounter;
    private static TextView tvFadeTarget;
    private static TextView tvFadeAccuracy;
    private static TextView tvFadeLives;
    private static TextView tvFadeScore;
    private static TextView tvFadeLevel;

    private static Button fadeStartButton;
    private static Button fadeStopButton;

    private static long elapsedTimeMillis;
    private double mTarget;
    private double mFadeIncrement;
    private double mRunningFadeTime;

    private int mFadeCounterColor;
    private static int alphaValue;
    private static int redValue;
    private static int greenValue;
    private static int blueValue;

    private static int straightAccuracy;

    private static final int SCORE_NOT_POSSIBLE = 0;
    private static boolean IS_LIFE_LOSS_POSSIBLE = false;
    private static final String FROM_FADE_COUNTER_ACTIVITY = "fromFadeCounterActivity";


    private static final double DEFAULT_FADE_COUNTER_TARGET = 10.00;
    private static final int BUFFER_OVER_TARGET = 10;
    private static long counterCeilingMillis;
    private static double counterCeilingSeconds;
    private static final double DEFAULT_FADE_RATIO = .60;
    private static final int ALPHA_VALUE_STEPS = 255;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fade_out_counter);
        //state = (ApplicationState) getApplicationContext();
        tvFadeCounter = (TextView) findViewById(R.id.t_v_fade_counter);
        tvFadeTarget = (TextView) findViewById(R.id.t_v_fade_target);
        tvFadeAccuracy = (TextView) findViewById(R.id.t_v_fade_accuracy_rating);
        tvFadeLives = (TextView) findViewById(R.id.t_v_fade_lives_remaining);
        tvFadeScore = (TextView) findViewById(R.id.t_v_fade_score);
        tvFadeLevel = (TextView) findViewById(R.id.t_v_fade_level);
        //initButtons();
        fadeStartButton = (Button) findViewById(R.id.b_fade_start);
        fadeStopButton = (Button) findViewById(R.id.b_fade_stop);
        //fadeStartButton = (Button) findViewById(R.id.b_start);
        //fadeStopButton = (Button) findViewById(R.id.b_stop);

        gameInfoDisplayer.showButtonState(fadeStopButton, stopButtonDisengagedDrawables);

        initFadeVariables();
        fadeStartButton.setOnTouchListener(this);
        fadeStopButton.setOnTouchListener(this);

        setStateTargetBasedOnLevel();
        Log.d(DEBUG_TAG, "onCreate() end " + getLocalClassName());

    }


    @Override
    protected void onResume() {
        super.onResume();
        gameInfoDisplayer.displayAllGameInfo(tvFadeTarget, tvFadeAccuracy, tvFadeLives,
                tvFadeScore, tvFadeLevel, FROM_FADE_COUNTER_ACTIVITY);
        flashStartButton();
    }

    private void initFadeVariables() {
        double fadeRange = DEFAULT_FADE_COUNTER_TARGET * DEFAULT_FADE_RATIO;
        //        This assumes the alpha value of the color is at its max
        mFadeIncrement = fadeRange / 255;
        mRunningFadeTime = mFadeIncrement;
        mFadeCounterColor = tvFadeCounter.getCurrentTextColor();
        alphaValue = Color.alpha(mFadeCounterColor);
        redValue = Color.red(mFadeCounterColor);
        greenValue = Color.green(mFadeCounterColor);
        blueValue = Color.blue(mFadeCounterColor);
    }

    @Override
    protected void onCounterStopped(long elapsedCount) {

        gameInfoDisplayer.showButtonState(fadeStopButton, stopButtonEngagedDrawables);
        gameInfoDisplayer.showButtonState(fadeStartButton, startButtonDisengagedDrawables);
        isStopClickable = false;

        double roundedCount = calculateRoundedCount(elapsedCount, counterCeilingSeconds);
        //roundedCount = mTarget;
        String roundedCountStr = generateRoundedCountStr(roundedCount);

        straightAccuracy = calculateAccuracy(mTarget, roundedCount);

        resetCounterColorAfterFadeOut();
        changeCounterColorIfDeadOn(roundedCount, mTarget, tvFadeCounter);

        tvFadeCounter.setText(roundedCountStr);
        gameInfoDisplayer.displayImmediateGameInfoAfterFadeCountTurn(tvFadeAccuracy);

        updateStateScore(SCORE_NOT_POSSIBLE);

        launchResetNextTurnAsync(this, this, tvFadeCounter, tvFadeLives, tvFadeScore, straightAccuracy, IS_LIFE_LOSS_POSSIBLE);
    }

    private void resetCounterColorAfterFadeOut() {
        tvFadeCounter.setTextColor(mFadeCounterColor);
    }

    @Override
    protected int calculateAccuracy(double target, double elapsedCount) {
        int accuracy = state.calcAccuracy(target, elapsedCount);
        state.setmTurnAccuracy(accuracy);
        return accuracy;
    }

    private void setStateTargetBasedOnLevel() {
        mTarget = DEFAULT_FADE_COUNTER_TARGET + (state.getLevel() - 1);
        state.setBaseTarget(mTarget);
        // TODO toggle below for random targets
        //state.setTurnTarget(mTarget);
        //counterCeilingMillis = (long) ((mTarget + BUFFER_OVER_TARGET) * MILLIS_IN_SECONDS);
        counterCeilingSeconds = mTarget + BUFFER_OVER_TARGET;
        counterCeilingMillis = (long) (counterCeilingSeconds * MILLIS_IN_SECONDS);
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
            onCounterStopped(elapsedTimeMillis);
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
                gameInfoDisplayer.showButtonState(fadeStartButton, startButtonDisengagedDrawables);
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
            onCounterStopped(maxElapsedMillis);
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
