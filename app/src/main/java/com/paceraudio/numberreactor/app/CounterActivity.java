package com.paceraudio.numberreactor.app;

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

import java.util.Random;

// class using Threads and Handlers instead of AsyncTask. Performance seems to be the same.

public class CounterActivity extends FragmentActivity implements UpdateDbListener,
        ResetNextTurnListener, OutOfLivesDialogFragment.OnFragmentInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String DEBUG_TAG = "jwc";


    //    private long startTime;
    private boolean mIsStartClickable;

    private Random mGen;
    public double mTarget;
    public double mLevelTarget;
    public int maxTarget = 8;

    // upper buffer over the mTarget number
    int upperBuffer = 5;

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
//    TimeCounter mTimeCounter;

    Handler mHandler;
    Thread mCounterThread;
    ApplicationState mState;
    DBHelper mDbHelper;

    long mStartTime;
    long mElapsedTimeMillis;
    double mElapsedAcceleratedCount;
    double mNextCount;
    double mAccelerator;
    double mLevelAccelerator;
    int mCount;
    int mCurrentTurn;

    private static final int BEGINNING_TARGET_LEVEL_ONE = 2;
    private static final int TURNS_PER_LEVEL = 2;
    private static final double BEGINNING_LEVEL_ACCELERATOR_LEVEL_ONE_EASY = .3;
    private final static double BEGINNING_ACCELERATOR_LEVEL_ONE_NORMAL = .7;
    private static final double BEGINNING_LEVEL_ACCELERATOR_LEVEL_ONE_HARD = 1.1;
    private static int LIFE_LOSS_THRESHOLD = 85;
    private static double ACCELERATOR_INCREASE_PER_LEVEL_FACTOR = 1.05;
    private static int LIVES_PER_LEVEL = 4;

    //    Params for the ResetNextTurnAsync.execute()
    private static final int LAST_TURN_RESET_BEFORE_NEW_ACTIVITY = -1;
    private static final int NORMAL_TURN_RESET = 0;

    //    RequestCode for starting FadeCounter for a result
    private static final int FADE_COUNTER_REQUEST_CODE = 1;

    private static final String EXTRA_LIFE_FROM_FADE_COUNTER_ROUND =
            "extraLifeFromFadeCounterRound";
    private static final String LEVEL_COMPLETED = "levelCompleted";

    private static final String DIGITAL_7_FONT_PATH =
            "fonts/digital-7-mono.ttf";
    private static final String ROBOTO_THIN_FONT_PATH =
            "fonts/Roboto-Thin.ttf";

    private boolean mIsListeningForSharedPrefChanges = false;


//    private SharedPreferences.OnSharedPreferenceChangeListener mPrefsListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
//        mCurrentTurn = 0;
        mState = (ApplicationState) getApplicationContext();
        mDbHelper = new DBHelper(this);

//        Define all the UI elements
        mTvCounter = (TextView) findViewById(R.id.t_v_counter);
        mTvTarget = (TextView) findViewById(R.id.t_v_target);
        mTvAccuracy = (TextView) findViewById(R.id.t_v_accuracy_rating);
        mTvLivesRemaining = (TextView) findViewById(R.id.t_v_lives_remaining);
        mTvScore = (TextView) findViewById(R.id.t_v_score);
        mTvLevel = (TextView) findViewById(R.id.t_v_level);
        mStartButton = (Button) findViewById(R.id.b_start);
        mFrameStartButton = (FrameLayout) findViewById(R.id.frame_b_start);
        mStopButton = (Button) findViewById(R.id.b_stop);
        mFrameStopButton = (FrameLayout) findViewById(R.id.frame_b_stop);

        Log.d(DEBUG_TAG, "\n**********NEW GAME*********");

//        Intent intent = getIntent();
//        TODO this may not be needed with startActivityForResult no being used to launch
// FadeCounter
//        if (doesOurIntentHaveExtras(intent)) {
//            setGameValuesForNextLevel();
//        } else {
//            setInitialTimeValuesLevelOne();
//        }

        if (!mIsListeningForSharedPrefChanges) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.registerOnSharedPreferenceChangeListener(this);
            mIsListeningForSharedPrefChanges = true;
            Log.d(DEBUG_TAG, "SharedPref Listener registered: " + mIsListeningForSharedPrefChanges);
        }

//        TODO see if this works to always run this in onCreate() without checking the Intent
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


//        MyTypefaces myTypefaces = MyTypefaces.getInstance();

//        Typeface tf = MyTypefaces.get(this, DIGITAL_7_FONT_PATH);
//        mTvCounter.setTypeface(tf);

//        Typeface tf = MyTypefaces.get(this, ROBOTO_THIN_FONT_PATH);
//        mTvCounter.setTypeface(tf);

        mIsStartClickable = true;

//      TODO make sure this doesn't run when user hits back button from seeing game stats.  Level
//      was rising. Set intent to null? Or make a boolean isIntentChecked?
//        Intent intent = getIntent();
//        if (doesOurIntentHaveExtras(intent)) {
//            setGameValuesForNextLevel();
//        } else {
//            setInitialTimeValuesLevelOne();
//        }

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

            }
        };
//      mAccelerator increases with every iteration of timing loop, so we always need to reset its
//      value to the acceleration rate for the level (mLevelAccelerator)
        mAccelerator = mLevelAccelerator;
        mStartButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    if (mIsStartClickable) {
                        mStartTime = SystemClock.elapsedRealtime();
                        showStartButtonEngaged();
                        mCounterThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (mElapsedAcceleratedCount < mTarget + upperBuffer &&
                                        !mCounterThread.isInterrupted()) {
                                    mElapsedAcceleratedCount = TimeCounter
                                            .calcElapsedAcceleratedCount(mStartTime, mAccelerator);


//                                    **********TODO for stepping in debug mode only!!!!!!
//                                    **********TODO comment out for running the app!!!!!!
//                                    mElapsedAcceleratedCount = mNextCount;


                                    if (mElapsedAcceleratedCount >= mNextCount) {
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mElapsedTimeMillis = SystemClock.elapsedRealtime
                                                        () - mStartTime;
                                                mTvCounter.setText(String.format("%.2f",
                                                        mElapsedAcceleratedCount));
                                                if (mCount == 0) {
                                                    Log.d(DEBUG_TAG,
                                                            String.format("Elapsed millis on " +
                                                                            "counter update %5d",
                                                                    mElapsedTimeMillis));
                                                }
                                                mCount++;
                                            }
                                        });
                                        mNextCount += 0.01;
                                        mAccelerator *= 1.0004;
                                    }
                                }
                                if (mCounterThread.isInterrupted()) return;

                                if (mElapsedAcceleratedCount >= mTarget + upperBuffer) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            onCounterMaxRangeHit(mElapsedAcceleratedCount);
                                        }
                                    });
                                }
                            }
                        });
                        mCounterThread.start();
                        mIsStartClickable = false;
//                        mCurrentTurn++;
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
                    long stopClickMillis = SystemClock.elapsedRealtime() - mStartTime;
                    Log.d(DEBUG_TAG, String.format("Stop onClick elapsed millis: %5d \ncount of " +
                            "background thread cycles: %5d", stopClickMillis, mCount));
                    onCounterStopped(mElapsedAcceleratedCount, mCount);
                    showStopButtonEngaged();
                    showStartButtonNotEngaged();
                    mCounterThread.interrupt();
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
        getMenuInflater().inflate(R.menu.main, menu);
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


    public void onCounterMaxRangeHit(Double accelCount) {
        mTvCounter.setTextColor(getResources().getColor(R.color.red));

        mTvCounter.setText(String.format("%.2f", accelCount));
    }

    // runs when mCounter thread is  is cancelled
    public void onCounterStopped(double accelCount, int count) {
        //Log.d(DEBUG_TAG, "Main Activity: Accelerated Count via onCounterStopped: " +
        // accelCount);

        // calc the accuracy
        double accuracy = calcAccuracy(mTarget, accelCount);

//        calc the score
        int score = calcScore(accuracy);

//        Display the accuracy
        displayAccuracyResult(accuracy);

        long onCounterCancelledElapsedTime = SystemClock.elapsedRealtime() - mStartTime;
        Log.d(DEBUG_TAG, "onCountCancElapsedTime: " + Long.toString(onCounterCancelledElapsedTime));

        // subtract a life if score is poor
        checkScoreAgainstLives(score);


//  TODO write a method for setting the color based on accuracy
        // set the text color of the counter based on the score
        if (accuracy >= 99) {
            score += 100;

            mTvCounter.setTextColor(getResources().getColor(R.color.green));
        } else if (accuracy > LIFE_LOSS_THRESHOLD && accuracy < 98) {
            mTvCounter.setTextColor(getResources().getColor(R.color.orange));
        } else {
            mTvCounter.setTextColor(getResources().getColor(R.color.red));
        }
        mTvCounter.setText(String.format("%.2f", accelCount));

        // add the score to the ApplicationState score
        addToStateRunningScore(score);

//        async task updating the db score.  onDbScoreUpdatedEndOfTurn() runs in onPostExecute.
//        There we check to see if we have lives left, if we do, we start the  ResetNextTurnAsync,
//        if not, we launch the OutOfLives Dialog
        UpdateScoreDbAsync updateScoreDbAsync = new UpdateScoreDbAsync(this, this);
        updateScoreDbAsync.execute(mState.getRunningScoreTotal());
    }


//    private double generateTarget() {
////        mTarget = mGen.nextInt((maxTarget + 1) - mBeginningTargetLevelOne) +
////         mBeginningTargetLevelOne;
//        return mTarget;
//    }

    private void showStartButtonEngaged() {
        mFrameStartButton.setBackgroundColor(getResources().getColor(R.color.green));
        mStartButton.setTextColor(getResources().getColor(R.color.green));
    }

    private void showStopButtonEngaged() {
        mFrameStopButton.setBackgroundColor(getResources().getColor(R.color.red));
        mStopButton.setTextColor(getResources().getColor(R.color.red));
    }

    private void showStartButtonNotEngaged() {
        mFrameStartButton.setBackgroundColor(getResources().getColor(R.color.brown));
        mStartButton.setTextColor(getResources().getColor(R.color.grey));
    }

    private void showStopButtonNotEngaged() {
        mFrameStopButton.setBackgroundColor(getResources().getColor(R.color.brown));
        mStopButton.setTextColor(getResources().getColor(R.color.grey));
    }


    private void displayTarget(double target) {
        mTvTarget.setText(getString(R.string.target) + " " + String.format("%.2f", target));
    }

    private void displayAccuracy() {
        mTvAccuracy.setText(getString(R.string.accuracy) + "    ");
    }

    private void displayAccuracyResult(double accuracy) {
        int percentage = (int) accuracy;
        mTvAccuracy.setText(getString(R.string.accuracy) + " " + percentage + "%");
    }

    private void displayLives() {
        mTvLivesRemaining.setText(getString(R.string.lives_remaining) + " " + mState
                .getLivesRemaining());
    }

    private void displayScore() {
        mTvScore.setText(getString(R.string.score) + " " + mState.getRunningScoreTotal());
    }

    private void displayLevel() {
        mTvLevel.setText(getString(R.string.level) + " " + mState.getLevel());
    }

    private void displayAllGameInfo() {
        displayTarget(mTarget);
        displayAccuracy();
        displayLives();
        displayScore();
        displayLevel();
        Log.d(DEBUG_TAG, "displayAllGameInfo ()********" +
                "\n Level: " + mState.getLevel() +
                "\n Target: " + mTarget +
                "\n Accelerator: " + mAccelerator +
                "\n Turn: " + mCurrentTurn);

    }

    private double calcAccuracy(double target, double counter) {
        double error = Math.abs(target - counter);
        return ((target - error) / target) * 100;
    }

    private int calcScore(double accuracy) {
        int score = 0;
        double margin = 80;
        double scoreToCalc = accuracy - margin;
        if (scoreToCalc > 0) {
            score = (int) scoreToCalc * 5;
        }
        return score;
    }


    private void resetBasicTimeValues() {
        mElapsedAcceleratedCount = 0;
        mElapsedTimeMillis = 0;
        mNextCount = 0.01;
        mCount = 0;
    }

    private void setInitialTimeValuesLevelOne() {
//        Get shared prefs and see what the difficulty level is set to.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String difficultyLevel = prefs.getString(getString(R.string.prefs_difficulty_key), "1");
        if (difficultyLevel.equals(getString(R.string.prefs_difficulty_values_1))) {
            mLevelAccelerator = BEGINNING_LEVEL_ACCELERATOR_LEVEL_ONE_EASY;
        }
        if (difficultyLevel.equals(getString(R.string.prefs_difficulty_values_2))) {
            mLevelAccelerator = BEGINNING_ACCELERATOR_LEVEL_ONE_NORMAL;
        }
        if (difficultyLevel.equals(getString(R.string.prefs_difficulty_values_3))) {
            mLevelAccelerator = BEGINNING_LEVEL_ACCELERATOR_LEVEL_ONE_HARD;
        }

        mLevelTarget = BEGINNING_TARGET_LEVEL_ONE;
        mTarget = mLevelTarget;
        resetAcceleratorToLevelAccelerator();
        resetBasicTimeValues();
        mCurrentTurn = 1;
        mState.setLevel(1);
        mState.resetScoreForNewGame();
        mIsStartClickable = true;
        displayAllGameInfo();

        //        TODO put this in async task
        mDbHelper.insertNewGameRowInDb();
        Log.d(DEBUG_TAG, "newest game number in db: " + Integer.toString(mDbHelper
                .queryNewestDbEntry()));
    }

    private void resetTimeValuesBetweenTurns() {
        resetAcceleratorToLevelAccelerator();
        resetBasicTimeValues();
        mTarget++;
        mCurrentTurn++;
        resetCounterToZero();
//        displayTarget(mTarget);
        displayAllGameInfo();
    }

    private void setGameValuesForNextLevel() {
        int currentLevel = mState.getLevel();
        int newLevel = currentLevel + 1;
        mState.setLevel(newLevel);
//        Set target and accelerator to their beginning levels, and increase them based on the
//        current level. Check the SharedPreferences for the difficulty level.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        updateDifficultyBasedOnPreferencesAndLevel(prefs);

        resetAcceleratorToLevelAccelerator();
        resetTargetBasedOnLevel();
        resetTurnsToFirstTurn();
        resetBasicTimeValues();

//        resetLivesToDefaultLivesPerLevel();

//        reset Counter text color to red, it is still at 0 alpha from the ResetNextTurnAsync
//        because we didn't fade the counter back in before launching FadeCounter
        resetCounterToZero();
//        "disengage" the stop button
        showStopButtonNotEngaged();

        displayAllGameInfo();
    }

    //  TODO see if we still need this method
    private boolean doesOurIntentHaveExtras(Intent intent) {
        Bundle extras = intent.getExtras();
        boolean hasExtras = false;
        if (extras != null) {
            if (extras.containsKey(EXTRA_LIFE_FROM_FADE_COUNTER_ROUND) && extras.containsKey
                    (LEVEL_COMPLETED)) {
                hasExtras = true;
            }
        }
        Log.d(DEBUG_TAG, "intent has extras: " + hasExtras);
        return hasExtras;
    }

    //    TODO finish this method for checking for a bonus life from FadeCounter in
    // game-structure branch
    private void checkOurIntentValues(Intent intent) {

    }


    private void resetCounterToZero() {
        mTvCounter.setText(getString(R.string.zero_point_zero));
        mTvCounter.setTextColor(getResources().getColor(R.color.red));
        mIsStartClickable = true;
    }

//    private void resetAccuracy() {
//        mTvAccuracy.setText(getString(R.string.accuracy));
//    }

//    private void resetLivesToDefaultLivesPerLevel() {
//        int numOfLivesPerLevel = LIVES_PER_LEVEL;
//        mState.setLivesRemaining(numOfLivesPerLevel);
//    }

    private void resetScoreToZero() {
        for (int i = 0; i < mState.getScoreList().size(); i++) {
            if (i == 0) {
                mState.getScoreList().set(i, 0);
            } else {
                mState.getScoreList().remove(i);
            }
            mState.setRunningScoreTotal(0);
        }
    }

    private void resetTurnsToFirstTurn() {
        mCurrentTurn = 1;
    }

    //    this needs to happen before every turn because the accelerator increases with every
//    iteration of the timing loop
    private void resetAcceleratorToLevelAccelerator() {
        mAccelerator = mLevelAccelerator;
    }

    private void resetTargetBasedOnLevel() {
        mLevelTarget = BEGINNING_TARGET_LEVEL_ONE;
        int level = mState.getLevel();
        for (int i = 1; i < level; i++) {
            mLevelTarget++;
        }
        mTarget = mLevelTarget;
    }

    private void checkScoreAgainstLives(int accuracy) {
        int lives = mState.getLivesRemaining();
        if (accuracy < LIFE_LOSS_THRESHOLD) {
            mState.setLivesRemaining(lives - 1);
            Log.d(DEBUG_TAG, "should lose life: " + Boolean.toString(accuracy <
                    LIFE_LOSS_THRESHOLD) + " lives remaining: " + lives);
        } else if (accuracy >= 99) {
            mState.setLivesRemaining(lives + 1);
        }
        mTvLivesRemaining.setText(getString(R.string.lives_remaining) + " " + mState
                .getLivesRemaining());
        Log.d(DEBUG_TAG, "checkScoreAgainstLives lives remaining: " + mState.getLivesRemaining
                ());
    }

    private void addToStateRunningScore(int newScore) {
        mState.setRunningScoreTotal(newScore);
    }

    private boolean checkIfLivesLeft(int lives) {
        if (lives == 0) {
            return false;
        }
        return true;
    }

    private void addBonusLifeToApplicationState() {
        int lives = mState.getLivesRemaining();
        mState.setLivesRemaining(lives + 1);
    }

    private void launchOutOfLivesDialog() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mDialogFragment = new OutOfLivesDialogFragment();
        mDialogFragment.show(ft, OUT_OF_LIVES_DIALOG);
    }

//    Checks whether or not to advance to the next Activity. Saves the last target played in mState

    private boolean isOutOfTurns(int currentTurn, int maxTurn) {
        if (currentTurn == maxTurn) {
            mState.setLastTarget(mTarget);
            return true;
        }
        return false;
    }

    private void launchFadeCounterActivity() {
        Intent intent = new Intent(CounterActivity.this, FadeOutCounterActivity.class);
//        startActivity(intent);
        startActivityForResult(intent, FADE_COUNTER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == FADE_COUNTER_REQUEST_CODE) {
            boolean addExtraLife = data.getBooleanExtra(EXTRA_LIFE_FROM_FADE_COUNTER_ROUND, false);
            if (addExtraLife) {
                addBonusLifeToApplicationState();
            }
            setGameValuesForNextLevel();
        }
    }

    //    Listener methods for AsyncTasks
//    After the db is updated, we reset for a new turn if we have lives left. We check if
//    the turn just played was the last, if so, we send a different param to the ResetNextTurnAsync
//    to avoid fading in a "0.00" fresh counter before launching the new activity.
    @Override
    public void onDbScoreUpdatedEndOfTurn() {
        Log.d(DEBUG_TAG, "updated score from update db async: " + Integer.toString(mDbHelper
                .queryScoreFromDb()));

        if (checkIfLivesLeft(mState.getLivesRemaining())) {
            if (isOutOfTurns(mCurrentTurn, TURNS_PER_LEVEL)) {
                ResetNextTurnAsync resetNextTurnAsync = new ResetNextTurnAsync(this, this,
                        mTvCounter);
                resetNextTurnAsync.execute(LAST_TURN_RESET_BEFORE_NEW_ACTIVITY);
            } else {
                ResetNextTurnAsync resetNextTurnAsync = new ResetNextTurnAsync(this, this,
                        mTvCounter);
                resetNextTurnAsync.execute(NORMAL_TURN_RESET);
            }
        } else {
            launchOutOfLivesDialog();
        }

    }

    //    Runs in onPostExecute of ResetNextTurnAsync
    @Override
    public void onNextTurnReset() {
        if (isOutOfTurns(mCurrentTurn, TURNS_PER_LEVEL)) {
            launchFadeCounterActivity();
        } else {
            resetTimeValuesBetweenTurns();
            showStopButtonNotEngaged();
            mIsStartClickable = true;

        }
    }


    //  Dialog fragment interaction methods
    @Override
    public void onOkClicked() {
        mDialogFragment.dismiss();
        setInitialTimeValuesLevelOne();
//        resetCounterToZero();
//        resetLivesToDefaultLivesPerLevel();
//        resetScoreToZero();
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
            if (difficulty.equals(getString(R.string.prefs_difficulty_values_1))) {
                mLevelAccelerator = BEGINNING_LEVEL_ACCELERATOR_LEVEL_ONE_EASY;
            }
            if (difficulty.equals(getString(R.string.prefs_difficulty_values_2))) {
                mLevelAccelerator = BEGINNING_ACCELERATOR_LEVEL_ONE_NORMAL;
            }
            if (difficulty.equals(getString(R.string.prefs_difficulty_values_3))) {
                mLevelAccelerator = BEGINNING_LEVEL_ACCELERATOR_LEVEL_ONE_HARD;
            }
            for (int i = 1; i < level; i++) {
                mLevelAccelerator *= ACCELERATOR_INCREASE_PER_LEVEL_FACTOR;
            }

            Log.d(DEBUG_TAG, "updateDifficultyBasedOnPreferencesAndLevel() running" +
                    "\n difficulty: " + difficulty +  "\n new accelerator: " + mLevelAccelerator);
        } else {
            Log.e(DEBUG_TAG, "Shared Prefs not working!!!!!!!!!!");
        }

    }
}

