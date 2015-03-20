package com.pacerdevelopment.numberreactor.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pacerdevelopment.numberreactor.app.R;
import com.pacerdevelopment.numberreactor.app.application.ApplicationState;
import com.pacerdevelopment.numberreactor.app.db.InsertNewGameRowInDbAsync;
import com.pacerdevelopment.numberreactor.app.db.InsertNewGameRowListener;
import com.pacerdevelopment.numberreactor.app.db.UpdateDbScoreListener;
import com.pacerdevelopment.numberreactor.app.db.UpdateLevelDbAsync;
import com.pacerdevelopment.numberreactor.app.db.UpdateScoreDbAsync;
import com.pacerdevelopment.numberreactor.app.dialogs.OutOfLivesDialogFragment;
import com.pacerdevelopment.numberreactor.app.util.ResetNextTurnListener;


public class CounterActivity extends TimeCounter implements
         OutOfLivesDialogFragment.OnFragmentInteractionListener,
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

    static long elapsedAcceleratedCount;
    static double levelDuration = 10;
    double mDurationIncrement = 9.99;
    int mCurrentTurn;

    private static int weightedAccuracy;
    private static final boolean LIFE_LOSS_POSSIBLE = true;

    private static final double BEGINNING_LEVEL_DURATION_LEVEL_ONE_EASY = 16;
    private static final double BEGINNING_LEVEL_DURATION_LEVEL_ONE_NORMAL =12;
    private static final double BEGINNING_LEVEL_DURATION_LEVEL_ONE_HARD = 9;

    private static final double DURATION_DECREASE_PER_LEVEL_FACTOR = .97;


    // Constants for the accelerated count
    private static final long MAX_COUNTER_VALUE_MILLIS = 100010;
    private static final double MAX_DISPLAYED_COUNTER_VALUE = 99.99;
    private static final double DURATION_DECREASE_CUTOFF = 1;
    private static final double DURATION_DECREASE_UPDATE = 3;
    private static final double DURATION_DECREASE_FACTOR = 0.99925;


    private static final String FROM_COUNTER_ACTIVITY = "fromCounterActivity";

    //    RequestCode for starting FadeCounter for a result
    private static final int FADE_COUNTER_REQUEST_CODE = 1;

    private boolean mIsListeningForSharedPrefChanges = false;

    private static InsertNewGameRowListener newGameRowListener;
    private static UpdateDbScoreListener updateDbScoreListener;
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
        startButton = (Button) findViewById(R.id.b_start);
        stopButton = (Button) findViewById(R.id.b_stop);

        gameInfoDisplayer.showButtonState(stopButton, stopButtonDisengaged);

        initNewGameRowListener();
        initResetNextTurnListener();
        initUpdateDbListener();

        if (!mIsListeningForSharedPrefChanges) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.registerOnSharedPreferenceChangeListener(this);
            mIsListeningForSharedPrefChanges = true;
        }

        setInitialTimeValuesLevelOne();
        startButton.setOnTouchListener(this);
        stopButton.setOnTouchListener(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsListeningForSharedPrefChanges) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.unregisterOnSharedPreferenceChangeListener(this);
            mIsListeningForSharedPrefChanges = false;
        }
    }




    protected static void onCounterStopped(long elapsedCount) {

        gameInfoDisplayer.showButtonState(stopButton, stopButtonEngaged);
        gameInfoDisplayer.showButtonState(startButton, startButtonDisengaged);

        double roundedCount = calculateRoundedCount(elapsedCount, MAX_DISPLAYED_COUNTER_VALUE);

        String roundedCountStr = generateRoundedCountStr(roundedCount);
        tvCounter.setText(roundedCountStr);

        weightedAccuracy = calculateAccuracy(mBaseTarget, roundedCount);
        int score = calculateScore(weightedAccuracy);
        updateStateScore(score);
        changeCounterColorIfDeadOn(roundedCount, mBaseTarget, tvCounter);

        gameInfoDisplayer.displayImmediateGameInfoAfterTurn(tvAccuracy);

        if (state.isFirstTurnInNewGame()) {
            launchNewGameRowAsync();
            state.setFirstTurnInNewGame(false);
        }
        else {
            launchUpdateScoreAsync();
        }
        /*UpdateScoreDbAsync updateScoreDbAsync = new UpdateScoreDbAsync(ApplicationState
                .getAppContext(), updateDbScoreListener);
        updateScoreDbAsync.execute(state.getmRunningScoreTotal());*/
    }

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
        resetDurationToStateDuration();
        resetBasicTimeValues();
        resetTurnToStateTurn();
        isStartClickable = true;
        gameInfoDisplayer.displayAllGameInfo(tvTarget, tvAccuracy, tvLives,
                tvScore, tvLevel, FROM_COUNTER_ACTIVITY);
        //        TODO put this in async task
        //mDbHelper.insertNewGameRowInDb();
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
        updateDifficultyBasedOnPreferencesAndLevel();
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
        updateDifficultyBasedOnPreferencesAndLevel();
    }

    private void updateDifficultyBasedOnPreferencesAndLevel() {
        int level = state.getLevel();
        double tempDuration = checkSharedPrefsDifficulty();
            for (int i = 1; i < level; i++) {
                tempDuration *= DURATION_DECREASE_PER_LEVEL_FACTOR;
            }
            state.setDuration(tempDuration);
            resetDurationToStateDuration();
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
                        isStartClickable = true;
                        resetTimeValuesBetweenTurns();
                        gameInfoDisplayer.showButtonState(stopButton, stopButtonDisengaged);
                    }
                } else {
                    launchOutOfLivesDialog();
                }
            }
        };
    }

    private void initUpdateDbListener() {
        updateDbScoreListener = new UpdateDbScoreListener() {
            @Override
            public void onDbScoreUpdatedEndOfTurn() {
                launchResetNextTurnAsync(resetNextTurnListener, ApplicationState.getAppContext(),
                        tvCounter, tvLives, tvScore, weightedAccuracy, LIFE_LOSS_POSSIBLE);
            }
        };
    }

    private static void launchNewGameRowAsync() {
        InsertNewGameRowInDbAsync newGameRowInDbAsync =
                new InsertNewGameRowInDbAsync(ApplicationState.getAppContext(), newGameRowListener);
        newGameRowInDbAsync.execute();
    }

    private static void launchUpdateScoreAsync() {
        UpdateScoreDbAsync updateScoreDbAsync = new UpdateScoreDbAsync(ApplicationState
                .getAppContext(), updateDbScoreListener);
        updateScoreDbAsync.execute(state.getmRunningScoreTotal());
    }

    private void initNewGameRowListener() {
        newGameRowListener = new InsertNewGameRowListener() {
            @Override
            public void onNewRowInsertedInDb() {
                launchUpdateScoreAsync();
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

        public CounterRunnable() {
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

            startTime = SystemClock.elapsedRealtime();

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
                                runningFlashDuration, stopButton);
                    }
                }
            }
            if (isCounterAtMaxValue(elapsedCount, MAX_COUNTER_VALUE_MILLIS)) {
                postUpdateCounterAfterTimeoutRunnable(elapsedCount);
            }
        }


        private double incrementDuration(double duration, double durationIncrement) {
            return duration + durationIncrement;
        }

        private double changeDurationIfAboveDecreaseCutoff(double durationIncrement) {
            if (durationIncrement >= DURATION_DECREASE_CUTOFF) {
                durationIncrement *= DURATION_DECREASE_FACTOR;
            }
            return durationIncrement;
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
            handler.post(updateCounterRunnable);
        }



        private void postUpdateCounterAfterTimeoutRunnable(long elapsedCount) {
            UpdateCounterAfterTimeoutRunnable runnable;
            runnable = new UpdateCounterAfterTimeoutRunnable(elapsedCount);
            handler.post(runnable);
        }
    }
}
