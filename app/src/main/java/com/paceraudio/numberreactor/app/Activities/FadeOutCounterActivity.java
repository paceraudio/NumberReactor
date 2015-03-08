package com.paceraudio.numberreactor.app.activities;

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
import com.paceraudio.numberreactor.app.application.ApplicationState;
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
    private static double target;
    private double mFadeIncrement;
    private double mRunningFadeTime;

    private static int fadeCounterColor;
    private static int alphaValue;
    private static int redValue;
    private static int greenValue;
    private static int blueValue;

    private static int straightAccuracy;

    private static ResetNextTurnListener resetNextTurnListener;

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

        gameInfoDisplayer.showButtonState(fadeStopButton, stopButtonDisengaged);

        initResetNextTurnListener();

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
        fadeCounterColor = tvFadeCounter.getCurrentTextColor();
        alphaValue = Color.alpha(fadeCounterColor);
        redValue = Color.red(fadeCounterColor);
        greenValue = Color.green(fadeCounterColor);
        blueValue = Color.blue(fadeCounterColor);
    }

    //@Override
    protected static void onCounterStopped(long elapsedCount) {

        gameInfoDisplayer.showButtonState(fadeStopButton, stopButtonEngaged);
        gameInfoDisplayer.showButtonState(fadeStartButton, startButtonDisengaged);
        isStopClickable = false;

        double roundedCount = calculateRoundedCount(elapsedCount, counterCeilingSeconds);
        //roundedCount = target;
        String roundedCountStr = generateRoundedCountStr(roundedCount);

        straightAccuracy = calculateAccuracy(target, roundedCount);

        resetCounterColorAfterFadeOut();
        changeCounterColorIfDeadOn(roundedCount, target, tvFadeCounter);

        tvFadeCounter.setText(roundedCountStr);
        gameInfoDisplayer.displayImmediateGameInfoAfterFadeCountTurn(tvFadeAccuracy);

        updateStateScore(SCORE_NOT_POSSIBLE);

        launchResetNextTurnAsync(resetNextTurnListener, ApplicationState.getAppContext(), tvFadeCounter, tvFadeLives, tvFadeScore, straightAccuracy, IS_LIFE_LOSS_POSSIBLE);
    }

    private static void resetCounterColorAfterFadeOut() {
        tvFadeCounter.setTextColor(fadeCounterColor);
    }

   // @Override
    protected static int calculateAccuracy(double target, double elapsedCount) {
        int accuracy = state.calcAccuracy(target, elapsedCount);
        state.setmTurnAccuracy(accuracy);
        return accuracy;
    }

    private void setStateTargetBasedOnLevel() {
        target = DEFAULT_FADE_COUNTER_TARGET + (state.getLevel() - 1);
        state.setBaseTarget(target);
        // TODO toggle below for random targets
        //state.setTurnTarget(target);
        //counterCeilingMillis = (long) ((target + BUFFER_OVER_TARGET) * MILLIS_IN_SECONDS);
        counterCeilingSeconds = target + BUFFER_OVER_TARGET;
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
        StartButtonArmedRunnable runnable = new StartButtonArmedRunnable(fadeStartButton);
        Thread startButtonArmedThread = new Thread(runnable);
        startButtonArmedThread.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == fadeStartButton && isStartClickable) {
            gameInfoDisplayer.showButtonState(fadeStartButton, startButtonEngaged);
            FadeCounterRunnable fadeCounterRunnable = new FadeCounterRunnable();
            Thread fadeCounterThread = new Thread(fadeCounterRunnable);
            fadeCounterThread.start();
            isStartClickable = false;
            isStopClickable = true;
        } else if (v == fadeStopButton && isStopClickable) {
            gameInfoDisplayer.showButtonState(fadeStopButton, stopButtonEngaged);
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

    /*@Override
    protected void updateCounter(long elapsedCount, int alpha) {
        if (isStopClickable) {
            double elapsedSeconds = elapsedCount / MILLIS_IN_SECONDS;
            tvFadeCounter.setText(String.format(DOUBLE_FORMAT, elapsedSeconds));
            tvFadeCounter.setTextColor(Color.argb(alpha, redValue, greenValue, blueValue));
            elapsedTimeMillis = elapsedCount;
        }
    }*/

    private void initResetNextTurnListener() {
        resetNextTurnListener = new ResetNextTurnListener() {
            @Override
            public void onNextTurnReset() {
                Log.d(DEBUG_TAG, "ResetNextTurnAsync returning to FadeCounter!!!!");
                launchCounterActivityWithResult();
            }
        };
    }

    private void launchCounterActivityWithResult() {
        Intent intent = new Intent(this, CounterActivity.class);
        setResult(RESULT_OK, intent);
        finish();
    }

    /*static class StartButtonArmedRunnable implements Runnable {

        Handler handler;
        public StartButtonArmedRunnable() {
            handler = new Handler();
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
                    //runOnUiThread(runnable);
                    handler.post(runnable);
                    runningFlashDuration += ARMED_START_BUTTON_FLASH_DURATION;
                }
            }
            gameInfoDisplayer.showButtonState(fadeStartButton, startButtonEngaged);
        }
    }

    static class FlashStartButtonRunnable implements Runnable {
        @Override

        public void run() {
            flashStartButtonArmed(fadeStartButton);
        }
    }
*/

    /*static class FlashStopButtonRunnable implements  Runnable {
        @Override
        public void run() {
            flashStopButtonArmed(fadeStopButton);
        }
    }*/


    static class UpdateCounterAfterTimeoutRunnable implements Runnable {
        long maxElapsedMillis;

        public UpdateCounterAfterTimeoutRunnable(long maxElapsedMillis) {
            this.maxElapsedMillis = maxElapsedMillis;
        }

        @Override
        public void run() {
            onCounterStopped(maxElapsedMillis);
        }
    }


    static class UpdateFadeCounterRunnable implements Runnable {

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

    static class FadeCounterRunnable implements Runnable {

        //Handler handler;
        //long startTime;

        public FadeCounterRunnable() {
            /*handler = new Handler();*/
        }

        @Override
        public void run() {
            runFadeCounter();
        }

        /*private void runFadeCounter() {

            long elapsedMillis = 0;
            long duration = COUNTER_INCREMENT_MILLIS;
            long durationIncrement = duration;
            int totalFadeDuration = (int) ((DEFAULT_FADE_COUNTER_TARGET * DEFAULT_FADE_RATIO) *
                    MILLIS_IN_SECONDS);
            final int fadeDurationIncrement = totalFadeDuration / ALPHA_VALUE_STEPS;
            int fadeDuration = fadeDurationIncrement;
            int alphaValue = ALPHA_VALUE_STEPS;

            // For the flashing of the armed stop button
            long runningFlashDuration = ARMED_STOP_BUTTON_FLASH_DURATION;

            startTime  = SystemClock.elapsedRealtime();

            while (elapsedMillis <= counterCeilingMillis && isStopClickable) {
                //elapsedMillis = SystemClock.elapsedRealtime() - startTime;
                if (checkElapsedTimeAgainstDuration(duration)) {
                    elapsedMillis = incrementElapsedCount(elapsedMillis);
                    //if (elapsedMillis >= fadeDuration && alphaValue > 0) {
                    if(checkElapsedTimeAgainstFadeDuration(fadeDuration)
                            && isAlphaAboveZero(alphaValue)){
                        //alphaValue--;
                        alphaValue = decrementAlphaValue(alphaValue);
                        //fadeDuration += fadeDurationIncrement;
                        fadeDuration = incrementFadeDuration(fadeDuration, fadeDurationIncrement);
                    }
                   *//* UpdateFadeCounterRunnable updateFadeCounterRunnable = new
                            UpdateFadeCounterRunnable(elapsedMillis, alphaValue);
                    //runOnUiThread(updateFadeCounterRunnable);
                    handler.post(updateFadeCounterRunnable);*//*
                    postUpdateFadeCounterRunnable(elapsedMillis, alphaValue);
                    //duration += durationIncrement;
                    duration = incrementDuration(duration, durationIncrement);

                    // Flashes stop button to show its armed
                    runningFlashDuration += showStopButtonArmed(elapsedMillis, runningFlashDuration);
                }
            }
            //if (elapsedMillis >= counterCeilingMillis && isStopClickable) {
            if (isCounterAtMaxValue(elapsedMillis)) {
                *//*UpdateCounterAfterTimeoutRunnable runnable = new UpdateCounterAfterTimeoutRunnable(elapsedMillis);
                handler.post(runnable);
                Log.d("jwc", "elapsed Millis from runnable: " + elapsedMillis);*//*
                postUpdateCounterAfterTimeoutRunnable(elapsedMillis);
            }
        }*/

        private void runFadeCounter() {

            long elapsedMillis = 0;
            long duration = COUNTER_INCREMENT_MILLIS;
            long durationIncrement = duration;
            int totalFadeDuration = (int) ((DEFAULT_FADE_COUNTER_TARGET * DEFAULT_FADE_RATIO) *
                    MILLIS_IN_SECONDS);
            final int fadeDurationIncrement = totalFadeDuration / ALPHA_VALUE_STEPS;
            int fadeDuration = fadeDurationIncrement;
            int alphaValue = ALPHA_VALUE_STEPS;

            // For the flashing of the armed stop button
            long runningFlashDuration = ARMED_STOP_BUTTON_FLASH_DURATION;

            startTime = SystemClock.elapsedRealtime();

            while (elapsedMillis <= counterCeilingMillis && isStopClickable) {
                if (checkElapsedTimeAgainstDuration(duration)) {
                    elapsedMillis = incrementElapsedCount(elapsedMillis);
                    if(checkElapsedTimeAgainstFadeDuration(fadeDuration)
                            && isAlphaAboveZero(alphaValue)){
                        alphaValue = decrementAlphaValue(alphaValue);
                        fadeDuration = incrementFadeDuration(fadeDuration, fadeDurationIncrement);
                    }
                    postUpdateFadeCounterRunnable(elapsedMillis, alphaValue);
                    duration = incrementDuration(duration, durationIncrement);

                    // Flashes stop button to show its armed
                    runningFlashDuration += showStopButtonArmed(elapsedMillis, runningFlashDuration, fadeStopButton);
                }
            }
            if (isCounterAtMaxValue(elapsedMillis, counterCeilingMillis)) {
                postUpdateCounterAfterTimeoutRunnable(elapsedMillis);
            }
        }

        /*private long retrieveElapsedTime() {
            return SystemClock.elapsedRealtime() - startTime;
        }*/

        /*private boolean checkElapsedTimeAgainstDuration(double duration) {
            return retrieveElapsedTime() >= duration;
        }*/

        /*private long incrementElapsedCount(long elapsedCount) {
            return elapsedCount + COUNTER_INCREMENT_MILLIS;
        }*/

        private long incrementDuration(long duration, long durationIncrement) {
            return duration + durationIncrement;
        }

        private boolean checkElapsedTimeAgainstFadeDuration(double fadeDuration) {
            return retrieveElapsedTime() >= fadeDuration;
        }

        private boolean isAlphaAboveZero(int alpha) {
            return alpha > 0;
        }

        private int decrementAlphaValue(int alpha) {
            return alpha - 1;
        }

        private int incrementFadeDuration(int fadeDuration, int fadeDurationIncrement) {
            return fadeDuration + fadeDurationIncrement;
        }

        private void postUpdateFadeCounterRunnable(long elapsedCount, int alpha) {
            UpdateFadeCounterRunnable updateFadeCounterRunnable = new
                    UpdateFadeCounterRunnable(elapsedCount, alpha);
            handler.post(updateFadeCounterRunnable);
        }

        /*private long showStopButtonArmed(long elapsed, long runningDur, Button stopButton) {
            if (elapsed >= runningDur) {
                FlashStopButtonRunnable runnable = new FlashStopButtonRunnable(stopButton);
                handler.post(runnable);
                return ARMED_STOP_BUTTON_FLASH_DURATION;
            }
            return 0;
        }*/

        /*private boolean isCounterAtMaxValue(long elapsedCount) {
            return elapsedCount >= counterCeilingMillis && isStopClickable;
        }*/

        private void postUpdateCounterAfterTimeoutRunnable(long elapsedCount) {
            UpdateCounterAfterTimeoutRunnable runnable;
            runnable = new UpdateCounterAfterTimeoutRunnable(elapsedCount);
            handler.post(runnable);
        }
    }
}
