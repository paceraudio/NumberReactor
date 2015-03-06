package com.paceraudio.numberreactor.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.paceraudio.numberreactor.app.application.ApplicationState;
import com.paceraudio.numberreactor.app.db.UpdateDbListener;
import com.paceraudio.numberreactor.app.db.UpdateLevelDbAsync;
import com.paceraudio.numberreactor.app.db.UpdateScoreDbAsync;
import com.paceraudio.numberreactor.app.dialogs.OutOfLivesDialogFragment;
import com.paceraudio.numberreactor.app.R;
import com.paceraudio.numberreactor.app.util.ResetNextTurnListener;


public class CounterActivity extends TimeCounter implements /*UpdateDbListener,*/
        /*ResetNextTurnListener,*/ OutOfLivesDialogFragment.OnFragmentInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener, View.OnTouchListener {

    public static double mBaseTarget;
    public double mTurnTarget;

    private static TextView tvCounter;
    private static TextView tvLives;
    private static TextView tvScore;
    private static TextView tvAccuracy;
    private static TextView tvLevel;
    private static TextView tvTarget;

    static Button startButton;
    static Button stopButton;

    private final static String OUT_OF_LIVES_DIALOG = "outOfLivesDialog";
    private DialogFragment mDialogFragment;

    long mStartTime;
    static long elapsedAcceleratedCount;
    static double levelDuration = 10;
    double mDurationIncrement = 9.99;
    int mCurrentTurn;

    private static int weightedAccuracy;
    private static final boolean LIFE_LOSS_POSSIBLE = true;

    //private static final int BEGINNING_TARGET_LEVEL_ONE = 2;
    //private static final int TURNS_PER_LEVEL = 2;


    private static final double BEGINNING_LEVEL_DURATION_LEVEL_ONE_EASY = 30;
    private static final double BEGINNING_LEVEL_DURATION_LEVEL_ONE_NORMAL = 10;
    private static final double BEGINNING_LEVEL_DURATION_LEVEL_ONE_HARD = 7;

    private static final double DURATION_DECREASE_PER_LEVEL_FACTOR = .95;


    // Constants for the accelerated count
    private static final long MAX_COUNTER_VALUE_MILLIS = 100010;
    private static final double MAX_DISPLAYED_COUNTER_VALUE = 99.99;
    private static final double DURATION_DECREASE_CUTOFF = 1;
    private static final double DURATION_DECREASE_UPDATE = 3;
    private static final double DURATION_DECREASE_FACTOR = 0.999;


    private static final String FROM_COUNTER_ACTIVITY = "fromCounterActivity";
    //private static final String LOG_UPDATE_COUNTER = "update counter: ";

    //    RequestCode for starting FadeCounter for a result
    private static final int FADE_COUNTER_REQUEST_CODE = 1;

    private boolean mIsListeningForSharedPrefChanges = false;

    private static UpdateDbListener updateDbListener;
    private static ResetNextTurnListener resetNextTurnListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
        //        Define all the UI elements
        tvCounter = (TextView) findViewById(R.id.t_v_counter);
        tvTarget = (TextView) findViewById(R.id.t_v_target);
        tvAccuracy = (TextView) findViewById(R.id.t_v_accuracy_rating);
        tvLives = (TextView) findViewById(R.id.t_v_lives_remaining);
        tvScore = (TextView) findViewById(R.id.t_v_score);
        tvLevel = (TextView) findViewById(R.id.t_v_level);
        //initButtons();
        startButton = (Button) findViewById(R.id.b_start);
        stopButton = (Button) findViewById(R.id.b_stop);

        gameInfoDisplayer.showButtonState(stopButton, stopButtonDisengaged);

        initResetNextTurnListener();
        initUpdateDbListener();

        Log.d(DEBUG_TAG, "\n--------------------**********NEW GAME*********--------------------");

        if (!mIsListeningForSharedPrefChanges) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.registerOnSharedPreferenceChangeListener(this);
            mIsListeningForSharedPrefChanges = true;
            Log.d(DEBUG_TAG, "SharedPref Listener registered: " + mIsListeningForSharedPrefChanges);
        }

        //        TODO see if this works to always run this in onCreate() without checking the
        // Intent
        setInitialTimeValuesLevelOne();
        startButton.setOnTouchListener(this);
        stopButton.setOnTouchListener(this);
        Log.d(DEBUG_TAG, "onCreate() end " + getLocalClassName());

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsListeningForSharedPrefChanges) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.unregisterOnSharedPreferenceChangeListener(this);
            mIsListeningForSharedPrefChanges = false;
            Log.d(DEBUG_TAG, "SharedPref Listener registered: " + mIsListeningForSharedPrefChanges);
        }
    }

    //@Override
    protected static void onCounterStopped(long elapsedCount) {
        gameInfoDisplayer.showButtonState(stopButton, stopButtonEngaged);
        gameInfoDisplayer.showButtonState(startButton, startButtonDisengaged);
        isStopClickable = false;

        double roundedCount = calculateRoundedCount(elapsedCount, MAX_DISPLAYED_COUNTER_VALUE);
        //roundedCount = mBaseTarget;

        String roundedCountStr = generateRoundedCountStr(roundedCount);
        tvCounter.setText(roundedCountStr);

        weightedAccuracy = calculateAccuracy(mBaseTarget, roundedCount);
        int score = calculateScore(weightedAccuracy);
        updateStateScore(score);
        changeCounterColorIfDeadOn(roundedCount, mBaseTarget, tvCounter);

        Log.d(DEBUG_TAG, "**********onAccelCounterStopped()**********" +
                "\n  elapsed accelerated count: " + roundedCount +
                "\n elapsed accelerated string: " + roundedCountStr +
                "\n                     target: " + mBaseTarget +
                "\n                   accuracy: " + weightedAccuracy + "%");

        gameInfoDisplayer.displayImmediateGameInfoAfterTurn(tvAccuracy);
        UpdateScoreDbAsync updateScoreDbAsync = new UpdateScoreDbAsync(ApplicationState
                .getAppContext(), updateDbListener);
        updateScoreDbAsync.execute(state.getmRunningScoreTotal());
    }

    //@Override
    protected static int calculateAccuracy(double target, double elapsedCount) {
        int weightedAccuracy = state.calcWeightedAccuracy(target, elapsedCount);
        state.setmWeightedAccuracy(weightedAccuracy);
        return weightedAccuracy;
    }

    private static int calculateScore(int accuracy) {
        int score = state.calcScore(accuracy);
        if (accuracy > SCORE_QUADRUPLE_THRESHOLD) {
            score *= FOUR;
        } else if (accuracy > SCORE_DOUBLE_THRESHOLD) {
            score *= TWO;
        }
        return score;
    }

    private void resetBasicTimeValues() {
        elapsedAcceleratedCount = 0;
        mDurationIncrement = levelDuration;
    }

    private void setInitialTimeValuesLevelOne() {
        state.setDuration(checkSharedPrefsDifficulty());
        state.resetGameStatsForNewGame();
        resetTargetToStateTarget();
        // TODO uncomment below for random targets
        //state.setTurnTarget(state.randomizeTarget(mBaseTarget));
        //mTurnTarget = state.randomizeTarget(mBaseTarget);
        resetDurationToStateDuration();
        resetBasicTimeValues();
        resetTurnToStateTurn();
        isStartClickable = true;
        gameInfoDisplayer.displayAllGameInfo(tvTarget, tvAccuracy, tvLives,
                tvScore, tvLevel, FROM_COUNTER_ACTIVITY);
        //        TODO put this in async task
        mDbHelper.insertNewGameRowInDb();
        Log.d(DEBUG_TAG, "newest game number in db: " + Integer.toString(mDbHelper
                .queryNewestDbEntry()));
        flashStartButton();
    }

    private double checkSharedPrefsDifficulty() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String difficultyLevel = prefs.getString(getString(R.string.prefs_difficulty_key), "2");
        state.setmDifficulty(Integer.parseInt(difficultyLevel));
        if (difficultyLevel.equals(getString(R.string.prefs_difficulty_values_1))) {
            return BEGINNING_LEVEL_DURATION_LEVEL_ONE_EASY;
        }
        else if (difficultyLevel.equals(getString(R.string.prefs_difficulty_values_2))) {
            return BEGINNING_LEVEL_DURATION_LEVEL_ONE_NORMAL;
        }
        else  {
            return BEGINNING_LEVEL_DURATION_LEVEL_ONE_HARD;
        }
    }

    private void resetTimeValuesBetweenTurns() {
        resetDurationToStateDuration();
        resetBasicTimeValues();
        state.setBaseTarget(mBaseTarget + 1);
        mBaseTarget = state.getBaseTarget();
        // TODO toggle below for random targets
        //state.setTurnTarget(state.randomizeTarget(mBaseTarget));
        state.setmTurn(mCurrentTurn + 1);
        mCurrentTurn = state.getmTurn();
        gameInfoDisplayer.displayAllGameInfo(tvTarget, tvAccuracy, tvLives,
                tvScore, tvLevel, FROM_COUNTER_ACTIVITY);
        flashStartButton();
    }

    private void setGameValuesForNextLevel() {
        int currentLevel = state.getLevel();
        state.setLevel(currentLevel + 1);
        // Set target and accelerator to their beginning levels,
        // and increase them based on the
        // current level. Check the SharedPreferences for the difficulty level.
        updateDifficultyBasedOnPreferencesAndLevel(/*prefs*/);
        resetDurationToStateDuration();
        resetTargetBasedOnLevel();
        resetTurnsToFirstTurn();
        resetBasicTimeValues();

        // reset Counter text color to red, it is still at 0 alpha from the ResetNextTurnAsync
        // because we didn't fade the counter back in before launching FadeCounter
        gameInfoDisplayer.resetCounterToZero(tvCounter);
        isStartClickable = true;
        gameInfoDisplayer.showButtonState(stopButton, stopButtonDisengaged);
        gameInfoDisplayer.displayAllGameInfo(tvTarget, tvAccuracy, tvLives,
                tvScore, tvLevel, FROM_COUNTER_ACTIVITY);
        flashStartButton();

    }

    private void resetTurnsToFirstTurn() {
        state.setmTurn(1);
        mCurrentTurn = state.getmTurn();
    }

    private void resetDurationToStateDuration() {
        levelDuration = state.getDuration();
    }

    private void resetTurnToStateTurn() {mCurrentTurn = state.getmTurn();}

    private void resetTargetToStateTarget() {mBaseTarget = state.getBaseTarget();}

    private void resetTargetBasedOnLevel() {
        int target = BEGINNING_TARGET_LEVEL_ONE;
        int level = state.getLevel();
        for (int i = 1; i < level; i++) {
            target++;
        }
        state.setBaseTarget(target);
        mBaseTarget = state.getBaseTarget();
        // TODO toggle below for random targets
        state.setTurnTarget(state.randomizeTarget(mBaseTarget));
    }

    private boolean checkIfLivesLeft() {
        return state.getLives() > 0;
    }

    private void launchOutOfLivesDialog() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mDialogFragment = OutOfLivesDialogFragment.newInstance();
        mDialogFragment.show(ft, OUT_OF_LIVES_DIALOG);
    }

    private void launchFadeCounterActivity() {
        Intent intent = new Intent(CounterActivity.this, FadeOutCounterActivity.class);
        startActivityForResult(intent, FADE_COUNTER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == FADE_COUNTER_REQUEST_CODE) {
            setGameValuesForNextLevel();
            UpdateLevelDbAsync updateLevelDbAsync = new UpdateLevelDbAsync(this);
            updateLevelDbAsync.execute(state.getLevel());
        }
    }

    //  Dialog fragment interaction methods
    //    TODO make sure a new row is created in db, because we are starting a new game
    @Override
    public void onOkClicked() {
        mDialogFragment.dismiss();
        setInitialTimeValuesLevelOne();
        gameInfoDisplayer.showButtonState(stopButton, stopButtonDisengaged);
        gameInfoDisplayer.resetCounterToZero(tvCounter);
    }

    @Override
    public void onExitClicked() {
        mDialogFragment.dismiss();
        finish();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.d(DEBUG_TAG, "Shared Pref Changed Listener running.");
        updateDifficultyBasedOnPreferencesAndLevel(/*sharedPreferences*/);
    }

    private void updateDifficultyBasedOnPreferencesAndLevel() {
        int level = state.getLevel();
        double tempDuration = checkSharedPrefsDifficulty();
            for (int i = 1; i < level; i++) {
                tempDuration *= DURATION_DECREASE_PER_LEVEL_FACTOR;
            }
            state.setDuration(tempDuration);
            resetDurationToStateDuration();

            Log.d(DEBUG_TAG, "updateDifficultyBasedOnPreferencesAndLevel() running" +
                    "\n difficulty: " + state.getmDifficulty() + "\n new duration: " + state
                    .getDuration());
    }

    private void flashStartButton() {
        StartButtonArmedRunnable runnable = new StartButtonArmedRunnable(startButton);
        Thread startButtonArmedThread = new Thread(runnable);
        startButtonArmedThread.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == startButton && isStartClickable) {
            gameInfoDisplayer.showButtonState(startButton, startButtonEngaged);
            CounterRunnable counterRunnable = new CounterRunnable();
            Thread counterThread = new Thread(counterRunnable);
            counterThread.start();
            isStartClickable = false;
            isStopClickable = true;
        } else if (v == stopButton && isStopClickable) {
            gameInfoDisplayer.showButtonState(stopButton, stopButtonEngaged);
            gameInfoDisplayer.showButtonState(startButton, startButtonDisengaged);
            isStopClickable = false;
            onCounterStopped(elapsedAcceleratedCount);
        }
        return false;
    }

    static void updateCounter(long elapsedCount) {
        if (isStopClickable) {
            double elapsedCountDouble = elapsedCount / MILLIS_IN_SECONDS;
            tvCounter.setText(String.format(DOUBLE_FORMAT, elapsedCountDouble));
            elapsedAcceleratedCount = elapsedCount;
        }
    }



    private void initResetNextTurnListener() {
        resetNextTurnListener = new ResetNextTurnListener() {
            @Override
            public void onNextTurnReset() {
                if (checkIfLivesLeft()) {
                    if (checkIfLastTurn() == LAST_TURN_RESET_BEFORE_NEW_ACTIVITY) {
                        launchFadeCounterActivity();
                    } else {
                        resetTimeValuesBetweenTurns();
                        gameInfoDisplayer.showButtonState(stopButton, stopButtonDisengaged);
                        isStartClickable = true;
                    }
                } else {
                    launchOutOfLivesDialog();
                }
            }
        };
    }

    private void initUpdateDbListener() {
        updateDbListener = new UpdateDbListener() {
            @Override
            public void onDbScoreUpdatedEndOfTurn() {
                launchResetNextTurnAsync(resetNextTurnListener, ApplicationState.getAppContext(),
                        tvCounter, tvLives, tvScore, weightedAccuracy, LIFE_LOSS_POSSIBLE);
            }
        };
    }




    static class UpdateCounterAfterTimeoutRunnable implements Runnable {
        long maxAccelCount;

        public UpdateCounterAfterTimeoutRunnable(long maxAccelCount) {
            this.maxAccelCount = maxAccelCount;
        }

        @Override
        public void run() {
            onCounterStopped(maxAccelCount);
        }
    }


    static class UpdateCounterRunnable implements Runnable {
        long accelCount;

        public UpdateCounterRunnable(long accelCount) {
            this.accelCount = accelCount;
        }

        @Override
        public void run() {
            updateCounter(accelCount);
        }
    }


    static class CounterRunnable implements Runnable {

        Handler mHandler;
        long mStartTime;

        public CounterRunnable() {
            mHandler = new Handler();
        }


        @Override
        public void run() {
            runCounter();
        }

        private void runCounter() {
            int postCount = 1;
            long elapsedCount = 0;
            double duration = levelDuration;
            double durationIncrement = duration;
            // For the flashing of the armed stop button
            long runningFlashDuration = ARMED_STOP_BUTTON_FLASH_DURATION;

            mStartTime = SystemClock.elapsedRealtime();

            while (elapsedCount <= MAX_COUNTER_VALUE_MILLIS && isStopClickable) {
                if (checkElapsedTimeAgainstDuration(duration)) {

                    elapsedCount = incrementElapsedCount(elapsedCount);
                    durationIncrement = changeDurationIfAboveDecreaseCutoff(durationIncrement);
                    duration = incrementDuration(duration, durationIncrement);
                    postCount = incrementPostCount(postCount);

                    if (isTimeToPostCount(durationIncrement, postCount)) {
                        postUpdateCounterRunnable(elapsedCount);

                        // Flashes stop button to show its armed
                        runningFlashDuration += showStopButtonArmed(retrieveElapsedTime(),
                                runningFlashDuration);
                    }
                }
            }
            if (isCounterAtMaxValue(elapsedCount)) {
                postUpdateCounterAfterTimeoutRunnable(elapsedCount);
            }
        }


        private long retrieveElapsedTime() {
            return SystemClock.elapsedRealtime() - mStartTime;
        }

        private boolean checkElapsedTimeAgainstDuration(double duration) {
            return retrieveElapsedTime() >= duration;
        }

        private long incrementElapsedCount(long elapsedCount) {
            return elapsedCount += COUNTER_INCREMENT_MILLIS;
        }

        private double changeDurationIfAboveDecreaseCutoff(double durationIncrement) {
            if (durationIncrement >= DURATION_DECREASE_CUTOFF) {
                durationIncrement *= DURATION_DECREASE_FACTOR;
            }
            return durationIncrement;
        }

        private double incrementDuration(double duration, double durationIncrement) {
            return duration += durationIncrement;
        }

        private int incrementPostCount(int postCount) {
            return postCount + 1;
        }

        private boolean isTimeToPostCount(double durationIncrement, int postCount) {
            return (durationIncrement >= DURATION_DECREASE_UPDATE) ||
                    (durationIncrement < DURATION_DECREASE_UPDATE && postCount % 3 == 0);
        }

        private void postUpdateCounterRunnable(long elapsedCount) {
            UpdateCounterRunnable updateCounterRunnable = new UpdateCounterRunnable
                    (elapsedCount);
            mHandler.post(updateCounterRunnable);
        }

        private long showStopButtonArmed(long elapsed, long runningDur) {
            if (elapsed >= runningDur) {
                FlashStopButtonRunnable runnable = new FlashStopButtonRunnable(stopButton);
                mHandler.post(runnable);
                return ARMED_STOP_BUTTON_FLASH_DURATION;
            }
            return 0;
        }

        private boolean isCounterAtMaxValue(long elapsedCount) {
            return elapsedCount >= MAX_COUNTER_VALUE_MILLIS && isStopClickable;
        }

        private void postUpdateCounterAfterTimeoutRunnable(long elapsedCount) {
            UpdateCounterAfterTimeoutRunnable runnable;
            runnable = new UpdateCounterAfterTimeoutRunnable(elapsedCount);
            mHandler.post(runnable);
        }
    }
}
