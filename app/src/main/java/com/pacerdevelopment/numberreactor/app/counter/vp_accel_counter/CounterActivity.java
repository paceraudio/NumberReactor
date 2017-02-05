package com.pacerdevelopment.numberreactor.app.counter.vp_accel_counter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pacerdevelopment.numberreactor.app.R;
import com.pacerdevelopment.numberreactor.app.application.NrApp;
import com.pacerdevelopment.numberreactor.app.counter.vp_counter.TimeCounter;
import com.pacerdevelopment.numberreactor.app.counter.vp_fade_counter.FadeOutCounterActivity;
import com.pacerdevelopment.numberreactor.app.model.db.InsertNewGameRowInDbAsync;
import com.pacerdevelopment.numberreactor.app.model.db.InsertNewGameRowListener;
import com.pacerdevelopment.numberreactor.app.model.db.UpdateDbScoreListener;
import com.pacerdevelopment.numberreactor.app.model.db.UpdateLevelDbAsync;
import com.pacerdevelopment.numberreactor.app.model.db.UpdateScoreDbAsync;
import com.pacerdevelopment.numberreactor.app.dialogs.OutOfLivesDialogFragment;
import com.pacerdevelopment.numberreactor.app.dialogs.WelcomeDialogFragment;
import com.pacerdevelopment.numberreactor.app.util.ResetNextTurnListener;


public class CounterActivity extends TimeCounter implements
        OutOfLivesDialogFragment.OutOfLivesListener,
        SharedPreferences.OnSharedPreferenceChangeListener, View.OnTouchListener {

    public static double mBaseTarget;

    private static TextView tvCounter;
    private static TextView tvLives;
    private static TextView tvScore;
    private static TextView tvAccuracy;
    private static TextView tvLevel;
    private static TextView tvTarget;

    static Button startButton;
    static Button stopButton;

    private final static String OUT_OF_LIVES_DIALOG = "outOfLivesDialog";
    private final static String WELCOME_DIALOG = "welcomeDialog";
    //private DialogFragment mDialogFragment;

    static long elapsedAcceleratedCount;
    static double levelDuration = 10;
    double mDurationIncrement = 9.99;
    int mCurrentTurn;

    private static int weightedAccuracy;
    private static final boolean LIFE_LOSS_POSSIBLE = true;

    private static final double BEGINNING_LEVEL_DURATION_LEVEL_ONE_EASY = 16;
    private static final double BEGINNING_LEVEL_DURATION_LEVEL_ONE_NORMAL =12;
    private static final double BEGINNING_LEVEL_DURATION_LEVEL_ONE_HARD = 8;

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

    private static final String PREFS_DIFFICULTY_ONE = "1";
    private static final String PREFS_DIFFICULTY_TWO = "2";


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

        applyTypeface(makeTvArrayList(tvCounter, tvTarget, tvAccuracy, tvLives, tvScore, tvLevel));

        gameInfoDisplayer.showButtonState(stopButton, stopButtonDisengaged);


        initNewGameRowListener();
        initResetNextTurnListener();
        initUpdateDbListener();



        if (!mIsListeningForSharedPrefChanges) {
            prefs.registerOnSharedPreferenceChangeListener(this);
            mIsListeningForSharedPrefChanges = true;
        }

        if (!checkSharedPrefsForPreviousInstall()) {
            launchWelcomeDialog();
            setSharedPrefsGameInstalled(true);
        }

        setInitialTimeValuesLevelOne();
        startButton.setOnTouchListener(this);
        stopButton.setOnTouchListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        flashStartButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mIsListeningForSharedPrefChanges) {
            //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.unregisterOnSharedPreferenceChangeListener(this);
            mIsListeningForSharedPrefChanges = false;
        }
    }

    private void onCounterStopped(long elapsedCount) {

        gameInfoDisplayer.showButtonState(stopButton, stopButtonEngaged);
        gameInfoDisplayer.showButtonState(startButton, startButtonDisengaged);

        //double roundedCount = calculateRoundedCount(elapsedCount, MAX_DISPLAYED_COUNTER_VALUE);
        obtainRoundedCount(elapsedCount, MAX_DISPLAYED_COUNTER_VALUE);

        String roundedCountStr = generateRoundedCountStr(roundedCount);
        tvCounter.setText(roundedCountStr);

        weightedAccuracy = calculateAccuracy(mBaseTarget, roundedCount);
        int score = calculateScore(weightedAccuracy);
        updateStateScore(score);
        changeCounterColorIfDeadOn(roundedCount, mBaseTarget, tvCounter);

        gameInfoDisplayer.displayImmediateGameInfoAfterTurn(tvAccuracy);

        if (gameState.isFirstTurnInNewGame()) {
            launchNewGameRowAsync();
            gameState.setFirstTurnInNewGame(false);
            if (!checkSharedPrefsForDbNotNull()) {
                setSharedPrefsDbNotNull();
            }
        }
        else {
            launchUpdateScoreAsync();
        }
    }

    protected static int calculateAccuracy(double target, double elapsedCount) {
        int weightedAccuracy = gameState.calcWeightedAccuracy(target, elapsedCount);
        gameState.setWeightedAccuracy(weightedAccuracy);
        return weightedAccuracy;
    }

    private static int calculateScore(int accuracy) {
        int score = gameState.calcScore(accuracy);
        if (accuracy > SCORE_QUADRUPLE_THRESHOLD) {
            score *= FOUR;
        } else if (accuracy > SCORE_DOUBLE_THRESHOLD) {
            score *= TWO;
        }
        return score;
    }

    private void resetBasicTimeValues() {
        elapsedAcceleratedCount = ZERO;
        mDurationIncrement = levelDuration;
    }

    private void setInitialTimeValuesLevelOne() {
        gameState.setDuration(checkSharedPrefsDifficulty());
        gameState.resetGameStatsForNewGame();
        resetTargetToStateTarget();
        resetDurationToStateDuration();
        resetBasicTimeValues();
        resetTurnToStateTurn();
        isStartClickable = true;
        gameInfoDisplayer.displayAllGameInfo(tvTarget, tvAccuracy, tvLives,
                tvScore, tvLevel, FROM_COUNTER_ACTIVITY);
    }

    private double checkSharedPrefsDifficulty() {
        String difficultyLevel = prefs.getString(getString(R.string.prefs_difficulty_key), PREFS_DIFFICULTY_TWO);
        gameState.setDifficulty(Integer.parseInt(difficultyLevel));
        if (difficultyLevel.equals(PREFS_DIFFICULTY_ONE)) {
            return BEGINNING_LEVEL_DURATION_LEVEL_ONE_EASY;
        }
        else if (difficultyLevel.equals(PREFS_DIFFICULTY_TWO)) {
            return BEGINNING_LEVEL_DURATION_LEVEL_ONE_NORMAL;
        }
        else  {
            return BEGINNING_LEVEL_DURATION_LEVEL_ONE_HARD;
        }
    }

    private void resetTimeValuesBetweenTurns() {
        resetDurationToStateDuration();
        resetBasicTimeValues();
        gameState.setBaseTarget(mBaseTarget + 1);
        mBaseTarget = gameState.getBaseTarget();
        gameState.setTurn(mCurrentTurn + 1);
        mCurrentTurn = gameState.getTurn();
        gameInfoDisplayer.displayAllGameInfo(tvTarget, tvAccuracy, tvLives,
                tvScore, tvLevel, FROM_COUNTER_ACTIVITY);
        flashStartButton();
    }

    private void setGameValuesForNextLevel() {
        int currentLevel = gameState.getLevel();
        gameState.setLevel(currentLevel + 1);
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
        gameState.setTurn(1);
        mCurrentTurn = gameState.getTurn();
    }

    private void resetDurationToStateDuration() {
        levelDuration = gameState.getDuration();
    }

    private void resetTurnToStateTurn() {mCurrentTurn = gameState.getTurn();}

    private void resetTargetToStateTarget() {mBaseTarget = gameState.getBaseTarget();}

    private void resetTargetBasedOnLevel() {
        int target = BEGINNING_TARGET_LEVEL_ONE;
        int level = gameState.getLevel();
        for (int i = 1; i < level; i++) {
            target++;
        }
        gameState.setBaseTarget(target);
        mBaseTarget = gameState.getBaseTarget();
        gameState.setTurnTarget(gameState.randomizeTarget(mBaseTarget));
    }

    private boolean checkIfLivesLeft() {
        return gameState.getLives() > 0;
    }

    private void launchOutOfLivesDialog() {
        android.app.FragmentManager fm = getFragmentManager();
        android.app.FragmentTransaction ft = fm.beginTransaction();
        android.app.DialogFragment dialogFragment = OutOfLivesDialogFragment.newInstance();
        dialogFragment.show(ft, OUT_OF_LIVES_DIALOG);
    }

    private void launchWelcomeDialog() {
        android.app.FragmentManager fm = getFragmentManager();
        android.app.FragmentTransaction ft = fm.beginTransaction();
        android.app.DialogFragment welcomeDialog = WelcomeDialogFragment.newInstance();
        welcomeDialog.show(ft, WELCOME_DIALOG);
    }

    private void launchFadeCounterActivity() {
        Intent intent = new Intent(CounterActivity.this, FadeOutCounterActivity.class);
        startActivityForResult(intent, FADE_COUNTER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == FADE_COUNTER_REQUEST_CODE) {
            //quitFlashingStartButton();
            setGameValuesForNextLevel();
            UpdateLevelDbAsync updateLevelDbAsync = new UpdateLevelDbAsync(this);
            updateLevelDbAsync.execute(gameState.getLevel());
            flashStartButton();
        }
    }

    //  Dialog fragment interaction methods
    @Override
    public void onOkClickedOutOfLivesDialog() {
        // set the Model back to first turn in game to trip a new row entered in db after the first
        // turn in new game is played
        gameState.setFirstTurnInNewGame(true);
        setInitialTimeValuesLevelOne();
        gameInfoDisplayer.showButtonState(stopButton, stopButtonDisengaged);
        gameInfoDisplayer.resetCounterToZero(tvCounter);
    }

    @Override
    public void onExitClickedOutOfLivesDialog() {
        finish();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        if (s.equals(getString(R.string.prefs_difficulty_key))) {
            updateDifficultyBasedOnPreferencesAndLevel();
        }
    }

    private void updateDifficultyBasedOnPreferencesAndLevel() {
        int level = gameState.getLevel();
        double tempDuration = checkSharedPrefsDifficulty();
            for (int i = 1; i < level; i++) {
                tempDuration *= DURATION_DECREASE_PER_LEVEL_FACTOR;
            }
            gameState.setDuration(tempDuration);
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
            StartButtonArmedRunnable.cancelThread();
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
                launchResetNextTurnAsync(resetNextTurnListener, CounterActivity.this,
                        tvCounter, tvLives, tvScore, weightedAccuracy, LIFE_LOSS_POSSIBLE);
            }
        };
    }

    private static void launchNewGameRowAsync() {
        InsertNewGameRowInDbAsync newGameRowInDbAsync =
                new InsertNewGameRowInDbAsync(NrApp.getAppContext(), newGameRowListener);
        newGameRowInDbAsync.execute();
    }

    private static void launchUpdateScoreAsync() {
        UpdateScoreDbAsync updateScoreDbAsync = new UpdateScoreDbAsync(NrApp
                .getAppContext(), updateDbScoreListener);
        updateScoreDbAsync.execute(gameState.getRunningScoreTotal());
    }

    private void initNewGameRowListener() {
        newGameRowListener = new InsertNewGameRowListener() {
            @Override
            public void onNewRowInsertedInDb() {
                launchUpdateScoreAsync();
            }
        };
    }

    @Override
    public void onBackPressed() {
        gameState.setFirstTurnInNewGame(true);
        super.onBackPressed();
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
