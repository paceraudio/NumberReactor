package com.paceraudio.numberreactor.app.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
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

import com.paceraudio.numberreactor.app.State.ApplicationState;
import com.paceraudio.numberreactor.app.DB.DBHelper;
import com.paceraudio.numberreactor.app.DB.UpdateDbListener;
import com.paceraudio.numberreactor.app.DB.UpdateLevelDbAsync;
import com.paceraudio.numberreactor.app.DB.UpdateScoreDbAsync;
import com.paceraudio.numberreactor.app.Utility.GameInfoDisplayer;
import com.paceraudio.numberreactor.app.Dialogs.OutOfLivesDialogFragment;
import com.paceraudio.numberreactor.app.R;
import com.paceraudio.numberreactor.app.Utility.ResetNextTurnAsync;
import com.paceraudio.numberreactor.app.Utility.ResetNextTurnListener;


public class CounterActivity extends FragmentActivity implements UpdateDbListener,
        ResetNextTurnListener, OutOfLivesDialogFragment.OnFragmentInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String DEBUG_TAG = "jwc";


    //    private long startTime;
    private boolean mIsStartClickable;
    private boolean mIsStopCLickable;

    public double mTarget;

    private TextView mTvCounter;
    private TextView mTvLivesRemaining;
    private TextView mTvScore;
    private TextView mTvAccuracy;
    private TextView mTvLevel;
    private TextView mTvTarget;

    private Button mStartButton;
    private FrameLayout mFrameStartButton;
    private Button mStopButton;
    private FrameLayout mFrameStopButton;

    private final static String OUT_OF_LIVES_DIALOG = "outOfLivesDialog";
    private DialogFragment mDialogFragment;

    Handler mHandler;
    Thread mCounterThread;
    ApplicationState mState;
    DBHelper mDbHelper;
    GameInfoDisplayer mGameInfoDisplayer;

    long mStartTime;
    long mElapsedTimeMillis;
    long mUptimeMillis;
    long mElapsedAcceleratedCount;
    double mElapsedAccelCountDouble;
    long mNextCount = 10;
    long mNextWholeCount = 1000;
    double mDuration = 10;
    double mDurationIncrement = 9.99;
    int mCount;
    int mPostCount;
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

    private static final String FROM_COUNTER_ACTIVITY = "fromCounterActivity";

    //    RequestCode for starting FadeCounter for a result
    private static final int FADE_COUNTER_REQUEST_CODE = 1;


    private boolean mIsListeningForSharedPrefChanges = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
        mState = (ApplicationState) getApplicationContext();
        mDbHelper = new DBHelper(this);
        mGameInfoDisplayer = new GameInfoDisplayer(this);

        //        Define all the UI elements
        mTvCounter = (TextView) findViewById(R.id.t_v_counter);
        mTvTarget = (TextView) findViewById(R.id.t_v_target);
        mTvAccuracy = (TextView) findViewById(R.id.t_v_accuracy_rating);
        mTvLivesRemaining = (TextView) findViewById(R.id.t_v_lives_remaining);
        mTvScore = (TextView) findViewById(R.id.t_v_score);
        mTvLevel = (TextView) findViewById(R.id.t_v_level);
        mStartButton = (Button) findViewById(R.id.b_start);
        mFrameStartButton = (FrameLayout) findViewById(R.id.f_l_for_b_start);
        mStopButton = (Button) findViewById(R.id.b_stop);
        mFrameStopButton = (FrameLayout) findViewById(R.id.f_l_for_b_stop);

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

        mIsStartClickable = true;
        mIsStopCLickable = false;

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

            }
        };
        mStartButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mIsStartClickable) {
                        mStartTime = SystemClock.elapsedRealtime();
                        mGameInfoDisplayer.showStartButtonEngaged(mStartButton, mFrameStartButton);
                        mPostCount = 1;
                        mCounterThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (mElapsedAcceleratedCount < 99999 &&
                                        !mCounterThread.isInterrupted()) {

                                    mElapsedTimeMillis = SystemClock.elapsedRealtime
                                            () - mStartTime;

                                    if (mElapsedTimeMillis >= mDuration) {

                                        /*Log.d(DEBUG_TAG, "\n- - - - - - - - - - - - - - - - - -
                                         -");

                                        Log.d(DEBUG_TAG, "elapsed time millis: " +
                                                mElapsedTimeMillis);
                                        Log.d(DEBUG_TAG, "mDuration: " + mDuration);*/

                                        mElapsedAcceleratedCount += 10;
                                        if (mDurationIncrement >= 1) {
                                            mDurationIncrement *= 0.999;
                                        }
                                        mDuration += mDurationIncrement;
                                        mPostCount++;

                                        if (mDurationIncrement >= 3 || (mDurationIncrement < 3 &&
                                                mPostCount % 3 == 0)) {

                                            mElapsedAccelCountDouble = mElapsedAcceleratedCount /
                                                    1000d;

                                            if (!mCounterThread.isInterrupted()) {
                                                mHandler.postAtTime(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTvCounter.setText(String.format("%.2f",
                                                                mElapsedAccelCountDouble));
                                                        Log.d(DEBUG_TAG,
                                                                "posting: " +
                                                                mElapsedAccelCountDouble);
                                                    }
                                                }, 0);
                                            }

                                            /*Log.d(DEBUG_TAG, "mElapsedAcceleratedCount: " +
                                                    mElapsedAcceleratedCount);
                                            Log.d(DEBUG_TAG, "mElapsedAccelCountDouble: " +
                                                    mElapsedAccelCountDouble);
                                            Log.d(DEBUG_TAG, "- - - - - - - - - - - - - - - -
                                            -\n");*/

                                        }
                                        mCount++;
                                    }
                                }
                                if (!mCounterThread.isInterrupted()) {
                                    mCounterThread.interrupt();
                                }

                                if (mCounterThread.isInterrupted()) {
                                    mHandler.postAtTime(new Runnable() {
                                        @Override
                                        public void run() {
                                            onCounterStopped(mElapsedAcceleratedCount, mCount);
                                        }
                                    }, 0);
                                }
                            }
                        });
                        mCounterThread.start();
                        mIsStartClickable = false;
                        mIsStopCLickable = true;
                        Log.d(DEBUG_TAG, "Start clicked!");
                    }
                }
                return false;
            }
        });

        mStopButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mIsStopCLickable) {
                        mCounterThread.interrupt();
                        long stopClickMillis = SystemClock.elapsedRealtime() - mStartTime;
                        Log.d(DEBUG_TAG, String.format("Stop onClick elapsed millis: %5d \ncount " +
                                "of " +
                                "background thread cycles: %5d", stopClickMillis, mCount));

                    }
                }
                return false;
            }
        });
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
    public void onCounterStopped(long accelCount, int count) {

        mGameInfoDisplayer.showStopButtonEngaged(mStopButton, mFrameStopButton);
        mGameInfoDisplayer.showStartButtonNotEngaged(mStartButton, mFrameStartButton);
        mIsStopCLickable = false;

        //      Round the elapsed accelerated count to 2 decimal places, give double param value 0,
        //        we aren't going to use it from this Activity
        double roundedCount = mState.roundElapsedCountLong(accelCount, FROM_COUNTER_ACTIVITY, 0);

//        // TODO TESTING ONLY!!!!!!!!!!!!
//        roundedCount = mTarget;

        //      Convert rounded value to a String to display
        String roundedCountStr = String.format("%.2f", roundedCount);

        //        calc the accuracy
        int accuracy = mState.calcAccuracy(mTarget, roundedCount);
        mState.setTurnAccuracy(accuracy);

        //        calc the score
        int score = mState.calcScore(accuracy);

        //  TODO write a method for setting the color based on accuracy
        // set the text color of the counter based on the score
        if (accuracy > 98) {
            score *= 2;
        }

        if (roundedCount == mTarget) {
            mTvCounter.setTextColor(getResources().getColor(R.color.green));
            score *= 2;
        }

        mTvCounter.setText(roundedCountStr);
        Log.d(DEBUG_TAG, "**********onCounterStopped()**********" +
                "\n  elapsed accelerated count: " + roundedCount +
                "\n elapsed accelerated string: " + roundedCountStr +
                "\n                     target: " + mTarget +
                "\n                   accuracy: " + accuracy + "%");

        mState.setTurnPoints(score);
        mState.setRunningScoreTotal(score);
        long onCounterCancelledElapsedTime = SystemClock.elapsedRealtime() - mStartTime;
        Log.d(DEBUG_TAG, "onCounterStopped() elapsed millis: " + Long.toString
                (onCounterCancelledElapsedTime));

        //        TODO make this info display when the turn resets
        mGameInfoDisplayer.displayImmediateGameInfoAfterTurn(mTvAccuracy);
        //        async task updating the db score.  onDbScoreUpdatedEndOfTurn() runs in
        // onPostExecute.
        //        There we check to see if we have lives left, if we do,
        // we start the  ResetNextTurnAsync,
        //        if not, we launch the OutOfLives Dialog
        UpdateScoreDbAsync updateScoreDbAsync = new UpdateScoreDbAsync(this, this);
        updateScoreDbAsync.execute(mState.getRunningScoreTotal());
    }

    private void resetBasicTimeValues() {
        mElapsedAcceleratedCount = 0;
        mElapsedTimeMillis = 0;
        mDurationIncrement = mDuration;
        mNextWholeCount = 1000;
        mNextCount = 10;
        mCount = 0;
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
        mState.setTurn(1);
        mCurrentTurn = mState.getTurn();
        mState.setLevel(1);
        mState.resetScoreForNewGame();
        mState.resetLivesForNewGame();
        mIsStartClickable = true;
        mGameInfoDisplayer.displayAllGameInfo(mTvTarget, mTvAccuracy, mTvLivesRemaining,
                mTvScore, mTvLevel, FROM_COUNTER_ACTIVITY);
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
        mState.setTurn(mCurrentTurn + 1);
        mCurrentTurn = mState.getTurn();
        mGameInfoDisplayer.displayAllGameInfo(mTvTarget, mTvAccuracy, mTvLivesRemaining,
                mTvScore, mTvLevel, FROM_COUNTER_ACTIVITY);
    }

    private void setGameValuesForNextLevel() {
        int currentLevel = mState.getLevel();
        mState.setLevel(currentLevel + 1);
        //        Set target and accelerator to their beginning levels,
        // and increase them based on the
        //        current level. Check the SharedPreferences for the difficulty level.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        updateDifficultyBasedOnPreferencesAndLevel(prefs);
        resetDurationToStateDuration();
        resetTargetBasedOnLevel();
        resetTurnsToFirstTurn();
        resetBasicTimeValues();

        //        reset Counter text color to red, it is still at 0 alpha from the
        // ResetNextTurnAsync
        //        because we didn't fade the counter back in before launching FadeCounter
        mGameInfoDisplayer.resetCounterToZero(mTvCounter);
        mIsStartClickable = true;
        //        "disengage" the stop button
        mGameInfoDisplayer.showStopButtonNotEngaged(mStopButton, mFrameStopButton);
        mGameInfoDisplayer.displayAllGameInfo(mTvTarget, mTvAccuracy, mTvLivesRemaining,
                mTvScore, mTvLevel, FROM_COUNTER_ACTIVITY);
    }

    private void resetTurnsToFirstTurn() {
        mState.setTurn(1);
        mCurrentTurn = mState.getTurn();
    }

    private void resetDurationToStateDuration() {
        mDuration = mState.getDuration();
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
        if (mState.getLives() == 0) {
            return false;
        }
        return true;
    }

    private void launchOutOfLivesDialog() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        //        mDialogFragment = new OutOfLivesDialogFragment();
        mDialogFragment = OutOfLivesDialogFragment.newInstance();
        mDialogFragment.show(ft, OUT_OF_LIVES_DIALOG);
    }

    private boolean isOutOfTurnsState() {
        if (mState.getTurn() == TURNS_PER_LEVEL) {
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
        param3 = mState.getTurnPoints();

        ResetNextTurnAsync resetNextTurnAsync = new ResetNextTurnAsync(this, this,
                mTvCounter, mTvLivesRemaining, mTvScore);
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
                mGameInfoDisplayer.showStopButtonNotEngaged(mStopButton, mFrameStopButton);
                mIsStartClickable = true;
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
        //        mDbHelper.insertNewGameRowInDb();
        setInitialTimeValuesLevelOne();
        mGameInfoDisplayer.showStopButtonNotEngaged(mStopButton, mFrameStopButton);
        mGameInfoDisplayer.resetCounterToZero(mTvCounter);
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
            //double tempAccelerator = 0;
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
            mDurationIncrement = mDuration;
            Log.d(DEBUG_TAG, "updateDifficultyBasedOnPreferencesAndLevel() running" +
                    "\n difficulty: " + difficulty + "\n new duration: " + mState
                    .getDuration());
        } else {
            Log.e(DEBUG_TAG, "Shared Prefs not working!!!!!!!!!!");
        }
    }
}
