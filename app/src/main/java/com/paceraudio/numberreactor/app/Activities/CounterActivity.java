package com.paceraudio.numberreactor.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.paceraudio.numberreactor.app.db.UpdateDbListener;
import com.paceraudio.numberreactor.app.db.UpdateLevelDbAsync;
import com.paceraudio.numberreactor.app.db.UpdateScoreDbAsync;
import com.paceraudio.numberreactor.app.util.ButtonDrawableView;
import com.paceraudio.numberreactor.app.util.GameInfoDisplayer;
import com.paceraudio.numberreactor.app.dialogs.OutOfLivesDialogFragment;
import com.paceraudio.numberreactor.app.R;
import com.paceraudio.numberreactor.app.util.ResetNextTurnAsync;
import com.paceraudio.numberreactor.app.util.ResetNextTurnListener;


public class CounterActivity extends FragmentActivity implements UpdateDbListener,
        ResetNextTurnListener, OutOfLivesDialogFragment.OnFragmentInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener, View.OnTouchListener {

    public static final String DEBUG_TAG = "jwc";


    //    private long startTime;
    private static boolean isStartClickable;
    private static boolean isStopCLickable;

    public double mTarget;

    private static TextView tvCounter;
    private static TextView tvLivesRemaining;
    private static TextView tvScore;
    private static TextView tvAccuracy;
    private static TextView tvLevel;
    private static TextView tvTarget;

    static Button startButton;
    static FrameLayout frameStartButton;
    static Button stopButton;
    static FrameLayout frameStopButton;

    private final static String OUT_OF_LIVES_DIALOG = "outOfLivesDialog";
    private DialogFragment mDialogFragment;

    ApplicationState mState;
    DBHelper mDbHelper;
    GameInfoDisplayer mGameInfoDisplayer;

    long mStartTime;
    static long elapsedAcceleratedCount;
    static double levelDuration = 10;
    double mDurationIncrement = 9.99;
    int mCurrentTurn;

    private static final int BEGINNING_TARGET_LEVEL_ONE = 2;
    private static final int TURNS_PER_LEVEL = 4;


    private static final double BEGINNING_LEVEL_DURATION_LEVEL_ONE_EASY = 30;
    private static final double BEGINNING_LEVEL_DURATION_LEVEL_ONE_NORMAL = 10;
    private static final double BEGINNING_LEVEL_DURATION_LEVEL_ONE_HARD = 7;

    private static final double DURATION_DECREASE_PER_LEVEL_FACTOR = .95;

    //    Params for the ResetNextTurnAsync.execute()
    private static final int LAST_TURN_RESET_BEFORE_NEW_ACTIVITY = -1;
    private static final int NORMAL_TURN_RESET = 0;

    private static final int DOUBLE_LIVES_GAINED = 2;
    private static final int LIFE_GAINED = 1;
    private static final int LIFE_NEUTRAL = 0;
    private static final int LIFE_LOST = -1;

    // Constants for the accelerated count
    private static final long MAX_COUNTER_VALUE_MILLIS = 100010;
    private static final long COUNTER_INCREMENT_MILLIS = 10;
    private static final double MILLIS_IN_SECONDS = 1000;
    private static final double DURATION_DECREASE_CUTOFF = 1;
    private static final double DURATION_DECREASE_UPDATE = 3;
    private static final double DURATION_DECREASE_FACTOR = 0.999;

    private static final double MAX_DISPLAYED_ACCEL_COUNT = 99.99;

    private static final String FROM_COUNTER_ACTIVITY = "fromCounterActivity";
    private static final String LOG_UPDATE_COUNTER = "update counter: ";
    private static final String DOUBLE_FORMAT = "%.2f";

    //    RequestCode for starting FadeCounter for a result
    private static final int FADE_COUNTER_REQUEST_CODE = 1;

    private boolean mIsListeningForSharedPrefChanges = false;

    private static boolean isMaxCountReached = false;

    static CounterActivity counterActivity = new CounterActivity();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
        mState = (ApplicationState) getApplicationContext();
        mDbHelper = new DBHelper(this);
        mGameInfoDisplayer = new GameInfoDisplayer(this);

        //        Define all the UI elements
        tvCounter = (TextView) findViewById(R.id.t_v_counter);
        tvTarget = (TextView) findViewById(R.id.t_v_target);
        tvAccuracy = (TextView) findViewById(R.id.t_v_accuracy_rating);
        tvLivesRemaining = (TextView) findViewById(R.id.t_v_lives_remaining);
        tvScore = (TextView) findViewById(R.id.t_v_score);
        tvLevel = (TextView) findViewById(R.id.t_v_level);
        startButton = (Button) findViewById(R.id.b_start);
        startButton.setBackgroundDrawable(new ButtonDrawableView(this).mStartTriangle);
        startButton.setPadding(20, 20, 20, 20);
        frameStartButton = (FrameLayout) findViewById(R.id.f_l_for_b_start);
        stopButton = (Button) findViewById(R.id.b_stop);
        frameStopButton = (FrameLayout) findViewById(R.id.f_l_for_b_stop);


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

        Log.d(DEBUG_TAG, "OnCreate() end");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(DEBUG_TAG, "onRestart() end");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(DEBUG_TAG, "onStart() end");
    }

    @Override
    protected void onResume() {
        super.onResume();

        isStartClickable = true;
        isStopCLickable = false;

        Log.d(DEBUG_TAG, "onResume() end");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "onPause() end");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(DEBUG_TAG, "onStop() end");
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
        Log.d(DEBUG_TAG, "onDestroy() end");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.counter, menu);
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

    // runs when mCounter thread is  is cancelled
    public void onCounterStopped(long accelCount) {

        mGameInfoDisplayer.showStopButtonEngaged(stopButton, frameStopButton);
        mGameInfoDisplayer.showStartButtonNotEngaged(startButton, frameStartButton);
        isStopCLickable = false;

        //      Round the elapsed accelerated count to 2 decimal places, give double param value 0,
        //        we aren't going to use it from this Activity
        double roundedCount = mState.roundElapsedCountLong(accelCount, FROM_COUNTER_ACTIVITY, 0);
        Log.d("jwc", "counter onCounterStopped roundedCount: " + roundedCount);

        //        // TODO TESTING ONLY!!!!!!!!!!!!
        //        roundedCount = mTarget;

        //      Convert rounded value to a String to display
        String roundedCountStr = String.format("%.2f", roundedCount);

        //        calc the accuracy
        int accuracy = mState.calcAccuracy(mTarget, roundedCount);
        mState.setmTurnAccuracy(accuracy);

        //        calc the score
        int score = mState.calcScore(accuracy);

        //  TODO write a method for setting the color based on accuracy
        // set the text color of the counter based on the score
        if (accuracy > 98) {
            score *= 2;
        }

        if (roundedCount == mTarget) {
            tvCounter.setTextColor(getResources().getColor(R.color.green));
            score *= 2;
        }

        tvCounter.setText(roundedCountStr);
        Log.d(DEBUG_TAG, "**********onCounterStopped()**********" +
                "\n  elapsed accelerated count: " + roundedCount +
                "\n elapsed accelerated string: " + roundedCountStr +
                "\n                     target: " + mTarget +
                "\n                   accuracy: " + accuracy + "%");

        mState.setmTurnPoints(score);
        mState.updateRunningScoreTotal(score);
        long onCounterCancelledElapsedTime = SystemClock.elapsedRealtime() - mStartTime;
        Log.d(DEBUG_TAG, "onCounterStopped() elapsed millis: " + Long.toString
                (onCounterCancelledElapsedTime));

        //        TODO make this info display when the turn resets
        mGameInfoDisplayer.displayImmediateGameInfoAfterTurn(tvAccuracy);
        // async task updating the db score.  onDbScoreUpdatedEndOfTurn() runs in onPostExecute.
        // There we check to see if we have lives left, if we do, we start the  ResetNextTurnAsync,
        // if not, we launch the OutOfLives Dialog
        UpdateScoreDbAsync updateScoreDbAsync = new UpdateScoreDbAsync(this, this);
        updateScoreDbAsync.execute(mState.getmRunningScoreTotal());
    }

    private void resetBasicTimeValues() {
        elapsedAcceleratedCount = 0;
        //        mElapsedTimeMillis = 0;
        mDurationIncrement = levelDuration;
        //        mNextWholeCount = 1000;
        //        mNextCount = 10;
        //        mCount = 0;
    }

    private void setInitialTimeValuesLevelOne() {
        //        Get shared prefs and see what the difficulty level is set to.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String difficultyLevel = prefs.getString(getString(R.string.prefs_difficulty_key), "2");
        if (difficultyLevel.equals(getString(R.string.prefs_difficulty_values_1))) {
            mState.setDuration(BEGINNING_LEVEL_DURATION_LEVEL_ONE_EASY);
        }
        if (difficultyLevel.equals(getString(R.string.prefs_difficulty_values_2))) {
            mState.setDuration(BEGINNING_LEVEL_DURATION_LEVEL_ONE_NORMAL);
        }
        if (difficultyLevel.equals(getString(R.string.prefs_difficulty_values_3))) {
            mState.setDuration(BEGINNING_LEVEL_DURATION_LEVEL_ONE_HARD);
        }

        mState.setTarget(BEGINNING_TARGET_LEVEL_ONE);
        mTarget = mState.getTarget();
        resetDurationToStateDuration();
        resetBasicTimeValues();
        mState.setmTurn(1);
        mCurrentTurn = mState.getmTurn();
        mState.setLevel(1);
        mState.resetScoreForNewGame();
        mState.resetLivesForNewGame();
        isStartClickable = true;
        mGameInfoDisplayer.displayAllGameInfo(tvTarget, tvAccuracy, tvLivesRemaining,
                tvScore, tvLevel, FROM_COUNTER_ACTIVITY);
        //        TODO put this in async task
        mDbHelper.insertNewGameRowInDb();
        Log.d(DEBUG_TAG, "newest game number in db: " + Integer.toString(mDbHelper
                .queryNewestDbEntry()));
    }

    private void resetTimeValuesBetweenTurns() {
        resetDurationToStateDuration();
        resetBasicTimeValues();
        mState.setTarget(mTarget + 1);
        mTarget = mState.getTarget();
        mState.setmTurn(mCurrentTurn + 1);
        mCurrentTurn = mState.getmTurn();
        mGameInfoDisplayer.displayAllGameInfo(tvTarget, tvAccuracy, tvLivesRemaining,
                tvScore, tvLevel, FROM_COUNTER_ACTIVITY);
    }

    private void setGameValuesForNextLevel() {
        int currentLevel = mState.getLevel();
        mState.setLevel(currentLevel + 1);
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
        mGameInfoDisplayer.resetCounterToZero(tvCounter);
        isStartClickable = true;
        //        "disengage" the stop button
        mGameInfoDisplayer.showStopButtonNotEngaged(stopButton, frameStopButton);
        mGameInfoDisplayer.displayAllGameInfo(tvTarget, tvAccuracy, tvLivesRemaining,
                tvScore, tvLevel, FROM_COUNTER_ACTIVITY);
    }

    private void resetTurnsToFirstTurn() {
        mState.setmTurn(1);
        mCurrentTurn = mState.getmTurn();
    }

    private void resetDurationToStateDuration() {
        //        mDuration = mState.getDuration();
        levelDuration = mState.getDuration();
    }

    private void resetTargetBasedOnLevel() {
        int target = BEGINNING_TARGET_LEVEL_ONE;
        int level = mState.getLevel();
        for (int i = 1; i < level; i++) {
            target++;
        }
        mState.setTarget(target);
        mTarget = mState.getTarget();
    }

    private boolean checkIfLivesLeft() {
        if (mState.getLives() <= 0) {
            return false;
        }
        return true;
    }

    private void launchOutOfLivesDialog() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mDialogFragment = OutOfLivesDialogFragment.newInstance();
        mDialogFragment.show(ft, OUT_OF_LIVES_DIALOG);
    }

    private boolean isOutOfTurnsState() {
        if (mState.getmTurn() == TURNS_PER_LEVEL) {
            return true;
        }
        return false;
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
            updateLevelDbAsync.execute(mState.getLevel());
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

        int param1;
        int param2;
        int param3;

        // See if this was our last turn
        if (isOutOfTurnsState()) {
            param1 = LAST_TURN_RESET_BEFORE_NEW_ACTIVITY;
        } else {
            param1 = NORMAL_TURN_RESET;
        }

        // See if there was a life gained or lost
        int livesGained = mState.numOfLivesGainedOrLost();
        if (livesGained == DOUBLE_LIVES_GAINED) {
            param2 = DOUBLE_LIVES_GAINED;
        } else if (livesGained == LIFE_GAINED) {
            param2 = LIFE_GAINED;
        } else if (livesGained == LIFE_LOST) {
            param2 = LIFE_LOST;
        } else {
            param2 = LIFE_NEUTRAL;
        }

        // Send the turn points as third param
        param3 = mState.getmTurnPoints();

        ResetNextTurnAsync resetNextTurnAsync = new ResetNextTurnAsync(this, this,
                tvCounter, tvLivesRemaining, tvScore);
        resetNextTurnAsync.execute(param1, param2, param3);

    }

    // Runs in onPostExecute of ResetNextTurnAsync
    @Override
    public void onNextTurnReset() {
        if (checkIfLivesLeft()) {
            if (isOutOfTurnsState()) {
                launchFadeCounterActivity();
            } else {
                resetTimeValuesBetweenTurns();
                mGameInfoDisplayer.showStopButtonNotEngaged(stopButton, frameStopButton);
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
        mGameInfoDisplayer.showStopButtonNotEngaged(stopButton, frameStopButton);
        mGameInfoDisplayer.resetCounterToZero(tvCounter);
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
        int level = mState.getLevel();
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
            mState.setDuration(tempDuration);
            resetDurationToStateDuration();

            Log.d(DEBUG_TAG, "updateDifficultyBasedOnPreferencesAndLevel() running" +
                    "\n difficulty: " + difficulty + "\n new duration: " + mState
                    .getDuration());
        } else {
            Log.e(DEBUG_TAG, "Shared Prefs not working!!!!!!!!!!");
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == startButton && isStartClickable) {
            mGameInfoDisplayer.showStartButtonEngaged(startButton, frameStartButton);
            CounterRunnable counterRunnable = new CounterRunnable(this);
            Thread counterThread = new Thread(counterRunnable);
            counterThread.start();
            isStartClickable = false;
            isStopCLickable = true;
        } else if (v == stopButton && isStopCLickable) {
            mGameInfoDisplayer.showStopButtonEngaged(stopButton, frameStopButton);
            mGameInfoDisplayer.showStartButtonNotEngaged(startButton, frameStartButton);
            isStopCLickable = false;
            onCounterStopped(elapsedAcceleratedCount);
        }
        return false;
    }

     static void updateCounter(long accelCount) {
        if (isStopCLickable) {
            double elapsedAccelCountDouble = accelCount / MILLIS_IN_SECONDS;
            tvCounter.setText(String.format(DOUBLE_FORMAT, elapsedAccelCountDouble));
            /*Log.d(DEBUG_TAG, LOG_UPDATE_COUNTER + String.format(DOUBLE_FORMAT,
                    elapsedAccelCountDouble));*/
            elapsedAcceleratedCount = accelCount;
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
        Activity activity;
        long accelCount;

        public UpdateCounterRunnable(Activity activity,long accelCount) {
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

            final long startTime = SystemClock.elapsedRealtime();

            while (elapsedAcceleratedCount <= MAX_COUNTER_VALUE_MILLIS && isStopCLickable) {
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
                                (activity, elapsedAcceleratedCount);
                        runOnUiThread(updateCounterRunnable);
                    }
                }
            }
            if (elapsedAcceleratedCount >= MAX_COUNTER_VALUE_MILLIS && isStopCLickable) {
                UpdateCounterAfterTimeoutRunnable runnable = new UpdateCounterAfterTimeoutRunnable(elapsedAcceleratedCount);

                runOnUiThread(runnable);
            }
        }
    }
}
