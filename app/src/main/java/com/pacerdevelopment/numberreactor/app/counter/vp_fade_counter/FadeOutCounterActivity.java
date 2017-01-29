package com.pacerdevelopment.numberreactor.app.counter.vp_fade_counter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pacerdevelopment.numberreactor.app.R;
import com.pacerdevelopment.numberreactor.app.application.NrApp;
import com.pacerdevelopment.numberreactor.app.counter.vp_accel_counter.CounterActivity;
import com.pacerdevelopment.numberreactor.app.counter.vp_counter.TimeCounter;
import com.pacerdevelopment.numberreactor.app.util.ResetNextTurnListener;


public class FadeOutCounterActivity extends TimeCounter implements View.OnTouchListener {

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
    //private double mFadeIncrement;
    //private double mRunningFadeTime;

    private static int fadeCounterColor;
    //private static int alphaValue;
    private static int redValue;
    private static int greenValue;
    private static int blueValue;

    private static long counterCeilingMillis;
    private static double counterCeilingSeconds;

    private static int straightAccuracy;

    private static ResetNextTurnListener resetNextTurnListener;

    private static final int SCORE_NOT_POSSIBLE = 0;
    private static boolean IS_LIFE_LOSS_POSSIBLE = false;
    private static final String FROM_FADE_COUNTER_ACTIVITY = "fromFadeCounterActivity";

    private static final double DEFAULT_FADE_COUNTER_TARGET = 10.00;
    private static final int BUFFER_OVER_TARGET = 10;
    private static final double DEFAULT_FADE_RATIO = .60;
    private static final int ALPHA_VALUE_STEPS = 255;

    private static final int ONE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fade_out_counter);
        tvFadeCounter = (TextView) findViewById(R.id.t_v_fade_counter);
        tvFadeTarget = (TextView) findViewById(R.id.t_v_fade_target);
        tvFadeAccuracy = (TextView) findViewById(R.id.t_v_fade_accuracy_rating);
        tvFadeLives = (TextView) findViewById(R.id.t_v_fade_lives_remaining);
        tvFadeScore = (TextView) findViewById(R.id.t_v_fade_score);
        tvFadeLevel = (TextView) findViewById(R.id.t_v_fade_level);
        fadeStartButton = (Button) findViewById(R.id.b_fade_start);
        fadeStopButton = (Button) findViewById(R.id.b_fade_stop);

        applyTypeface(makeTvArrayList(tvFadeCounter, tvFadeTarget, tvFadeAccuracy, tvFadeLives,
                tvFadeScore, tvFadeLevel));

        gameInfoDisplayer.showButtonState(fadeStopButton, stopButtonDisengaged);

        initResetNextTurnListener();

        initFadeVariables();
        fadeStartButton.setOnTouchListener(this);
        fadeStopButton.setOnTouchListener(this);

        setStateTargetBasedOnLevel();

    }


    @Override
    protected void onResume() {
        super.onResume();
        gameInfoDisplayer.displayAllGameInfo(tvFadeTarget, tvFadeAccuracy, tvFadeLives,
                tvFadeScore, tvFadeLevel, FROM_FADE_COUNTER_ACTIVITY);
        flashStartButton();
    }


    private void initFadeVariables() {
        fadeCounterColor = tvFadeCounter.getCurrentTextColor();
        redValue = Color.red(fadeCounterColor);
        greenValue = Color.green(fadeCounterColor);
        blueValue = Color.blue(fadeCounterColor);
    }

    private static void onCounterStopped(long elapsedCount) {

        gameInfoDisplayer.showButtonState(fadeStopButton, stopButtonEngaged);
        gameInfoDisplayer.showButtonState(fadeStartButton, startButtonDisengaged);
        isStopClickable = false;

        double roundedCount = calculateRoundedCount(elapsedCount, counterCeilingSeconds);
        String roundedCountStr = generateRoundedCountStr(roundedCount);

        straightAccuracy = calculateAccuracy(target, roundedCount);

        resetCounterColorAfterFadeOut();
        changeCounterColorIfDeadOn(roundedCount, target, tvFadeCounter);

        tvFadeCounter.setText(roundedCountStr);
        gameInfoDisplayer.displayImmediateGameInfoAfterFadeCountTurn(tvFadeAccuracy);

        updateStateScore(SCORE_NOT_POSSIBLE);

        launchResetNextTurnAsync(resetNextTurnListener, NrApp.getAppContext(), tvFadeCounter, tvFadeLives, tvFadeScore, straightAccuracy, IS_LIFE_LOSS_POSSIBLE);
    }

    private static void resetCounterColorAfterFadeOut() {
        tvFadeCounter.setTextColor(fadeCounterColor);
    }

    protected static int calculateAccuracy(double target, double elapsedCount) {
        int accuracy = gameState.calcAccuracy(target, elapsedCount);
        gameState.setTurnAccuracy(accuracy);
        return accuracy;
    }

    private void setStateTargetBasedOnLevel() {
        target = DEFAULT_FADE_COUNTER_TARGET + (gameState.getLevel() - ONE);
        gameState.setBaseTarget(target);
        counterCeilingSeconds = target + BUFFER_OVER_TARGET;
        counterCeilingMillis = (long) (counterCeilingSeconds * MILLIS_IN_SECONDS);
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
            StartButtonArmedRunnable.cancelThread();
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

    private void initResetNextTurnListener() {
        resetNextTurnListener = new ResetNextTurnListener() {
            @Override
            public void onNextTurnReset() {
                launchCounterActivityWithResult();
            }
        };
    }

    private void launchCounterActivityWithResult() {
        Intent intent = new Intent(this, CounterActivity.class);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        StartButtonArmedRunnable.cancelThread();
        quitFlashingStartButton();
        launchCounterActivityWithResult();
    }

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

        public FadeCounterRunnable() {
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

        private void postUpdateCounterAfterTimeoutRunnable(long elapsedCount) {
            UpdateCounterAfterTimeoutRunnable runnable;
            runnable = new UpdateCounterAfterTimeoutRunnable(elapsedCount);
            handler.post(runnable);
        }
    }
}
