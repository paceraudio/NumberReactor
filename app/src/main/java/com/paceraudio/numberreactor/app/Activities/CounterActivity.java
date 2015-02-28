package com.paceraudio.numberreactor.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.paceraudio.numberreactor.app.db.UpdateDbListener;
import com.paceraudio.numberreactor.app.db.UpdateLevelDbAsync;
import com.paceraudio.numberreactor.app.db.UpdateScoreDbAsync;
import com.paceraudio.numberreactor.app.dialogs.OutOfLivesDialogFragment;
import com.paceraudio.numberreactor.app.R;
import com.paceraudio.numberreactor.app.util.ResetNextTurnListener;


public class CounterActivity extends TimeCounter implements UpdateDbListener,
        ResetNextTurnListener, OutOfLivesDialogFragment.OnFragmentInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener, View.OnTouchListener {

    public double mBaseTarget;
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


        gameInfoDisplayer.showButtonState(stopButton, stopButtonDisengagedDrawables);

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



    // runs when mCounter thread is  is cancelled
   /* public void onAccelCounterStopped(long elapsedCount) {

        gameInfoDisplayer.showButtonState(stopButton, stopButtonEngagedDrawables);
        gameInfoDisplayer.showButtonState(startButton, startButtonDisengagedDrawables);
        isStopClickable = false;

        double roundedCount = calculateRoundedCount(elapsedCount, MAX_DISPLAYED_COUNTER_VALUE);

        String roundedCountStr = generateRoundedCountStr(roundedCount);
        tvCounter.setText(roundedCountStr);

        int weightedAccuracy = calculateAccuracy(mBaseTarget, roundedCount);
        int score = calculateScore(weightedAccuracy);
        updateStateScore(score);
        changeCounterColorIfDeadOn(roundedCount, mBaseTarget, tvCounter);

        Log.d(DEBUG_TAG, "**********onAccelCounterStopped()**********" +
                "\n  elapsed accelerated count: " + roundedCount +
                "\n elapsed accelerated string: " + roundedCountStr +
                "\n                     target: " + mBaseTarget +
                "\n                   accuracy: " + weightedAccuracy + "%");

        gameInfoDisplayer.displayImmediateGameInfoAfterTurn(tvAccuracy);
        UpdateScoreDbAsync updateScoreDbAsync = new UpdateScoreDbAsync(this, this);
        updateScoreDbAsync.execute(state.getmRunningScoreTotal());
    }*/

    @Override
    protected void onCounterStopped(long elapsedCount) {
        gameInfoDisplayer.showButtonState(stopButton, stopButtonEngagedDrawables);
        gameInfoDisplayer.showButtonState(startButton, startButtonDisengagedDrawables);
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
        UpdateScoreDbAsync updateScoreDbAsync = new UpdateScoreDbAsync(this, this);
        updateScoreDbAsync.execute(state.getmRunningScoreTotal());
    }

    @Override
    protected int calculateAccuracy(double target, double elapsedCount) {
        int weightedAccuracy = state.calcWeightedAccuracy(target, elapsedCount);
        state.setmWeightedAccuracy(weightedAccuracy);
        return weightedAccuracy;
    }

    private int calculateScore(int accuracy) {
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
        //        Get shared prefs and see what the difficulty level is set to.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String difficultyLevel = prefs.getString(getString(R.string.prefs_difficulty_key), "2");
        if (difficultyLevel.equals(getString(R.string.prefs_difficulty_values_1))) {
            state.setDuration(BEGINNING_LEVEL_DURATION_LEVEL_ONE_EASY);
        }
        if (difficultyLevel.equals(getString(R.string.prefs_difficulty_values_2))) {
            state.setDuration(BEGINNING_LEVEL_DURATION_LEVEL_ONE_NORMAL);
        }
        if (difficultyLevel.equals(getString(R.string.prefs_difficulty_values_3))) {
            state.setDuration(BEGINNING_LEVEL_DURATION_LEVEL_ONE_HARD);
        }

        state.setBaseTarget(BEGINNING_TARGET_LEVEL_ONE);
        mBaseTarget = state.getBaseTarget();
        // TODO uncomment below for random targets
        //state.setTurnTarget(state.randomizeTarget(mBaseTarget));
        //mTurnTarget = state.randomizeTarget(mBaseTarget);
        resetDurationToStateDuration();
        resetBasicTimeValues();
        mCurrentTurn = state.getmTurn();
        state.resetScoreForNewGame();
        state.resetLivesForNewGame();
        isStartClickable = true;
        gameInfoDisplayer.displayAllGameInfo(tvTarget, tvAccuracy, tvLives,
                tvScore, tvLevel, FROM_COUNTER_ACTIVITY);
        //        TODO put this in async task
        mDbHelper.insertNewGameRowInDb();
        Log.d(DEBUG_TAG, "newest game number in db: " + Integer.toString(mDbHelper
                .queryNewestDbEntry()));
        flashStartButton();
    }

    private void resetTimeValuesBetweenTurns() {
        resetDurationToStateDuration();
        resetBasicTimeValues();
        state.setBaseTarget(mBaseTarget + 1);
        mBaseTarget = state.getBaseTarget();
        // TODO toggle below for random targets
        state.setTurnTarget(state.randomizeTarget(mBaseTarget));
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        updateDifficultyBasedOnPreferencesAndLevel(prefs);
        resetDurationToStateDuration();
        resetTargetBasedOnLevel();
        resetTurnsToFirstTurn();
        resetBasicTimeValues();

        // reset Counter text color to red, it is still at 0 alpha from the ResetNextTurnAsync
        // because we didn't fade the counter back in before launching FadeCounter
        gameInfoDisplayer.resetCounterToZero(tvCounter);
        isStartClickable = true;
        gameInfoDisplayer.showButtonState(stopButton, stopButtonDisengagedDrawables);
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

    //    Listener methods for AsyncTasks
    //
    // After the db is updated, we reset for a new turn if we have lives left. We check if
    // the turn just played was the last, if so, we send a different param to the ResetNextTurnAsync
    // to avoid fading in a "0.00" fresh counter before launching the new activity.
    @Override
    public void onDbScoreUpdatedEndOfTurn() {
        Log.d(DEBUG_TAG, "updated score from update db async: " + Integer.toString(mDbHelper
                .queryScoreFromDb()));

        launchResetNextTurnAsync(this, this, tvCounter, tvLives, tvScore, weightedAccuracy, LIFE_LOSS_POSSIBLE);
    }

    // Runs in onPostExecute of ResetNextTurnAsync
    @Override
    public void onNextTurnReset() {
        if (checkIfLivesLeft()) {
            if (checkIfLastTurn() == LAST_TURN_RESET_BEFORE_NEW_ACTIVITY) {
                launchFadeCounterActivity();
            } else {
                resetTimeValuesBetweenTurns();
                gameInfoDisplayer.showButtonState(stopButton, stopButtonDisengagedDrawables);
                isStartClickable = true;
            }
        } else {
            launchOutOfLivesDialog();
        }
    }

    //  Dialog fragment interaction methods
    //    TODO make sure a new row is created in db, because we are starting a new game
    @Override
    public void onOkClicked() {
        mDialogFragment.dismiss();
        setInitialTimeValuesLevelOne();
        gameInfoDisplayer.showButtonState(stopButton, stopButtonDisengagedDrawables);
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
        updateDifficultyBasedOnPreferencesAndLevel(sharedPreferences);
    }

    private void updateDifficultyBasedOnPreferencesAndLevel(SharedPreferences prefs) {
        int level = state.getLevel();
        String difficulty = prefs.getString(getString(R.string.prefs_difficulty_key), "-1");
        if (!difficulty.equals("-1")) {
            double tempDuration = 0;
            if (difficulty.equals(getString(R.string.prefs_difficulty_values_1))) {
                tempDuration = BEGINNING_LEVEL_DURATION_LEVEL_ONE_EASY;
            }
            if (difficulty.equals(getString(R.string.prefs_difficulty_values_2))) {
                tempDuration = BEGINNING_LEVEL_DURATION_LEVEL_ONE_NORMAL;
            }
            if (difficulty.equals(getString(R.string.prefs_difficulty_values_3))) {
                tempDuration = BEGINNING_LEVEL_DURATION_LEVEL_ONE_HARD;
            }

            for (int i = 1; i < level; i++) {
                tempDuration *= DURATION_DECREASE_PER_LEVEL_FACTOR;
            }
            state.setDuration(tempDuration);
            resetDurationToStateDuration();

            Log.d(DEBUG_TAG, "updateDifficultyBasedOnPreferencesAndLevel() running" +
                    "\n difficulty: " + difficulty + "\n new duration: " + state
                    .getDuration());
        } else {
            Log.e(DEBUG_TAG, "Shared Prefs not working!!!!!!!!!!");
        }
    }

    private void flashStartButton() {
        StartButtonArmedRunnable runnable = new StartButtonArmedRunnable();
        Thread startButtonArmedThread = new Thread(runnable);
        startButtonArmedThread.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == startButton && isStartClickable) {
            gameInfoDisplayer.showButtonState(startButton, startButtonEngagedDrawables);
            CounterRunnable counterRunnable = new CounterRunnable(this);
            Thread counterThread = new Thread(counterRunnable);
            counterThread.start();
            isStartClickable = false;
            isStopClickable = true;
        } else if (v == stopButton && isStopClickable) {
            gameInfoDisplayer.showButtonState(stopButton, stopButtonEngagedDrawables);
            gameInfoDisplayer.showButtonState(startButton, startButtonDisengagedDrawables);
            isStopClickable = false;
            onCounterStopped(elapsedAcceleratedCount);
        }
        return false;
    }

     static void updateCounter(long accelCount) {
        if (isStopClickable) {
            double elapsedAccelCountDouble = accelCount / MILLIS_IN_SECONDS;
            tvCounter.setText(String.format(DOUBLE_FORMAT, elapsedAccelCountDouble));
            /*Log.d(DEBUG_TAG, LOG_UPDATE_COUNTER + String.format(DOUBLE_FORMAT,
                    elapsedAccelCountDouble));*/
            elapsedAcceleratedCount = accelCount;
        }
    }

    static void flashStartButtonArmed() {
        if (isStartClickable) {
            if (isStartFlashing) {
                gameInfoDisplayer.showButtonState(startButton, startButtonDisengagedDrawables );
                isStartFlashing = false;
            } else {
                gameInfoDisplayer.showButtonState(startButton, startButtonArmedDrawables);
                isStartFlashing = true;
            }
        }
    }

    static void flashStopButtonArmed() {
        if (isStopClickable) {
            if (!isStopFlashing) {
                gameInfoDisplayer.showButtonState(stopButton, stopButtonArmedDrawables);
                isStopFlashing = true;
            } else {
                gameInfoDisplayer.showButtonState(stopButton, stopButtonDisengagedDrawables);
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
            gameInfoDisplayer.showButtonState(startButton, startButtonEngagedDrawables);
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
        long maxAccelCount;

        public UpdateCounterAfterTimeoutRunnable(long maxAccelCount) {
            this.maxAccelCount = maxAccelCount;
        }

        @Override
        public void run() {
            onCounterStopped(maxAccelCount);
        }
    }





    class UpdateCounterRunnable implements Runnable {
        long accelCount;

        public UpdateCounterRunnable(long accelCount) {
            this.accelCount = accelCount;
        }

        @Override
        public void run() {
            updateCounter(accelCount);
        }
    }




    class CounterRunnable implements Runnable {

        Activity activity;
//        Handler handler;

        public CounterRunnable(Activity activity) {
            this.activity = activity;
//            this.handler = handler;
        }


        @Override
        public void run() {
            runCounter();

        }

        private void runCounter() {

            long postCount = 1;
            long elapsedTimeMillis;
            long elapsedAcceleratedCount = 0;
            double duration = levelDuration;
            double durationIncrement = duration;

            // For the flashing of the armed stop button
            long runningFlashDuration = ARMED_STOP_BUTTON_FLASH_DURATION;

            long startTime = SystemClock.elapsedRealtime();

            while (elapsedAcceleratedCount <= MAX_COUNTER_VALUE_MILLIS && isStopClickable) {
                elapsedTimeMillis = SystemClock.elapsedRealtime() - startTime;
                if (elapsedTimeMillis >= duration) {
                    elapsedAcceleratedCount += COUNTER_INCREMENT_MILLIS;

                    if (durationIncrement >= DURATION_DECREASE_CUTOFF) {
                        durationIncrement *= DURATION_DECREASE_FACTOR;
                    }
                    duration += durationIncrement;
                    postCount++;

                    if (durationIncrement >= DURATION_DECREASE_UPDATE ||
                            (durationIncrement < DURATION_DECREASE_UPDATE && postCount % 3 == 0)) {
                        UpdateCounterRunnable updateCounterRunnable = new UpdateCounterRunnable
                                (elapsedAcceleratedCount);
                        runOnUiThread(updateCounterRunnable);

                        // Flashes stop button to show its armed
                        runningFlashDuration += showStopButtonArmed(elapsedTimeMillis, runningFlashDuration);
                    }
                }
            }
            if (elapsedAcceleratedCount >= MAX_COUNTER_VALUE_MILLIS && isStopClickable) {
                UpdateCounterAfterTimeoutRunnable runnable;
                runnable = new UpdateCounterAfterTimeoutRunnable(elapsedAcceleratedCount);
                runOnUiThread(runnable);
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
