package com.paceraudio.numberreactor.app;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
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
import android.widget.TextView;

import java.util.Random;

// class using Threads and Handlers instead of AsyncTask. Performance seems to be the same.

public class CounterActivity extends FragmentActivity implements CounterListener, UpdateScoreDbListener, ResetNextTurnListener, OutOfLivesDialogFragment.OnFragmentInteractionListener {

    public static final String DEBUG_TAG = "jwc";


    private long startTime;
    private boolean startIsClickable;

    private Random gen;
    public static double target;
    public int maxTarget = 8;
    private int minTarget = 3;
    private int lifeLossThreshhold = 85;
    // upper buffer over the target number
    int upperBuffer = 5;

    private TextView mTvCounter;
    private TextView mTvLivesRemaining;
    private TextView mTvScore;

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


    int currentTurn;
    private static final int NUM_OF_TURNS_PER_LEVEL = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentTurn = 0;
        mState = (ApplicationState) getApplicationContext();
        mDbHelper = new DBHelper(this);
        mDbHelper.insertNewGameRowInDb();
        Log.d(DEBUG_TAG, "newest game number in db: " + Integer.toString(mDbHelper.queryNewestDbEntry()));

    }

    @Override
    protected void onResume() {
        super.onResume();
        resetTimeValues();

        switch (mState.getLevel()) {
            case 1:
                mLevelAccelerator = .7;
                break;
            case 2:
                maxTarget += 5;
                minTarget += 5;
                break;
            case 3:
                maxTarget += 10;
                minTarget += 10;
        }

        gen = new Random();
        mTvCounter = (TextView) findViewById(R.id.t_v_counter);
        mTvLivesRemaining = (TextView) findViewById(R.id.t_v_lives_remaining);
        mTvLivesRemaining.setText(this.getString(R.string.lives_remaining) + " " + mState.getLivesRemaining());
        mTvScore = (TextView) findViewById(R.id.t_v_score);
        mTvScore.setText(this.getString(R.string.score) + " " + mState.getRunningScoreTotal());
        startIsClickable = true;

        mAccelerator = mLevelAccelerator;

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {

            }
        };

        displayTarget(generateTarget());

        Button startButton = (Button) findViewById(R.id.b_start);

        startButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    if (startIsClickable) {
                        mStartTime = SystemClock.elapsedRealtime();
//                        local variable so we don't have to reset mAccelerator
                        final double accelerator = mAccelerator;

                        mCounterThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (mElapsedAcceleratedCount < target + upperBuffer && !mCounterThread.isInterrupted()) {
                                    mElapsedAcceleratedCount = TimeCounter.calcElapsedAcceleratedCount(mStartTime, mAccelerator);
                                    if (mElapsedAcceleratedCount >= mNextCount) {
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mElapsedTimeMillis = SystemClock.elapsedRealtime() - mStartTime;
                                                mTvCounter.setText(String.format("%.2f", mElapsedAcceleratedCount));
                                                if (mCount == 0) {
                                                    Log.d(DEBUG_TAG, String.format("Elapsed millis on counter update %5d", mElapsedTimeMillis));
                                                }
                                                mCount++;
                                            }
                                        });
                                        mNextCount += 0.01;
                                        mAccelerator *= 1.0006;
                                    }
                                }
                                if (mCounterThread.isInterrupted()) return;

                                if (mElapsedAcceleratedCount >= target + upperBuffer) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            onCounterComplete(mElapsedAcceleratedCount);
                                        }
                                    });
                                }
                            }
                        });
                        mCounterThread.start();
                        startIsClickable = false;
                        currentTurn++;
                        //Log.d(DEBUG_TAG, "Start clicked!");
                    }

                }
                return false;
            }
        });

        Button stopButton = (Button) findViewById(R.id.b_stop);
        stopButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    long stopClickMillis = SystemClock.elapsedRealtime() - mStartTime;
                    Log.d(DEBUG_TAG, String.format("Stop onClick elapsed millis: %5d \ncount of background thread cycles: %5d", stopClickMillis, mCount));
                    onCounterCancelled(mElapsedAcceleratedCount, mCount);
                    mCounterThread.interrupt();
                    startIsClickable = true;

                }
                return false;
            }
        });

/*
        Button resetButton = (Button) findViewById(R.id.b_reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (advanceToNextLevel(currentTurn, NUM_OF_TURNS_PER_LEVEL)) {

                    //TODO placeholder pause so its not so abrupt.  need to get rid of reset button anyway
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {

                    }
                    launchFadeCounterActivity();
                }
                //Log.d(DEBUG_TAG, "reset clicked");
                displayTarget(generateTarget());
//              reset the value for the time here, after the time counting background thread has been
//              interrupted, so that the textview values don't update when the stop button is pushed
                resetTimeValues();

                resetCounter();
            }
        });
*/
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCounterComplete(Double accelCount) {
        mTvCounter.setTextColor(getResources().getColor(R.color.red));

        mTvCounter.setText(String.format("%.2f", accelCount));
    }

    // runs when mCounter thread is  is cancelled
    @Override
    public void onCounterCancelled(Double accelCount, int count) {
        //Log.d(DEBUG_TAG, "Main Activity: Accelerated Count via onCounterCancelled: " + accelCount);

        // assign a score
        int accuracy = calcAccuracy(target, accelCount);
        int score = accuracy;

        long onCounterCancelledElapsedTime = SystemClock.elapsedRealtime() - mStartTime;
        Log.d(DEBUG_TAG, "onCountCancElapsedTime: " + Long.toString(onCounterCancelledElapsedTime));

        // subtract a life if score is poor
        checkAccuracyAgainstLives(accuracy);

        // check to see we aren't out of lives
        checkLivesLeft(mState.getLivesRemaining());

        // set the text color of the counter based on the score
        if (accuracy >= 99) {
            score += 100;

            mTvCounter.setTextColor(getResources().getColor(R.color.green));
        } else if (accuracy > lifeLossThreshhold && accuracy < 99) {
            mTvCounter.setTextColor(getResources().getColor(R.color.orange));
        } else {
            mTvCounter.setTextColor(getResources().getColor(R.color.red));
        }
        mTvCounter.setText(String.format("%.2f", accelCount));

        // add the score to the ApplicationState score
        addToStateRunningScore(score);

        //TODO update the db
        // async task updating the db score
        UpdateScoreDbAsync updateScoreDbAsync = new UpdateScoreDbAsync(this, this);
        updateScoreDbAsync.execute(mState.getRunningScoreTotal());

//        ResetNextTurnAsync resetNextTurnAsync = new ResetNextTurnAsync(this);
//        resetNextTurnAsync.execute();

    }


    private double generateTarget() {
        target = gen.nextInt((maxTarget + 1) - minTarget) + minTarget;
        return target;
    }

    private void displayTarget(double target) {
        TextView tvTarget = (TextView) findViewById(R.id.t_v_target);
        tvTarget.setText(getString(R.string.target) + " " + String.format("%.2f", target));
    }

    private void resetTimeValues() {
        mElapsedTimeMillis = 0;
        mElapsedAcceleratedCount = 0;
        mAccelerator = mLevelAccelerator;
        mNextCount = 0.01;
        mCount = 0;
    }

    private void resetCounter() {
        mTvCounter.setText(getString(R.string.zero_point_zero));
        mTvCounter.setTextColor(getResources().getColor(R.color.white));
        startIsClickable = true;
    }

    private void resetLives() {
        int numOfLivesPerLevel = mState.getNumOfLivesPerLevel();
        mState.setLivesRemaining(numOfLivesPerLevel);
        mTvLivesRemaining.setText(getString(R.string.lives_remaining) + " " + Integer.toString(numOfLivesPerLevel));
    }

    private void resetScore() {
        for (int i = 0; i < mState.getScoreList().size(); i++) {
            if (i == 0) {
                mState.getScoreList().set(i, 0);
            } else {
                mState.getScoreList().remove(i);
            }
            mTvScore.setText(getString(R.string.score) + " " + (getString(R.string.zero)));
        }
    }

    private int calcAccuracy(double target, double counter) {
        double error = Math.abs(target - counter);
        // base the accuracy off of a range of 2.0 margin on either side of the target.
        // i.e. target is 8.0, a user value of 6.0 is 0, 7.0 is 50, 7.9 is 95.
        double accuracyD = 100 - (error * 50);
        if (accuracyD < 0) {
            accuracyD = 0;
        }
        int accuracyI = (int) Math.round(accuracyD);

        Log.d(DEBUG_TAG, "target: " + target);
        Log.d(DEBUG_TAG, "counter: " + counter);
        Log.d(DEBUG_TAG, "error: " + error);
        Log.d(DEBUG_TAG, "accuracy: " + accuracyD);
        //for (int score: mState.getScoreList()) {
        //    Log.d(DEBUG_TAG, Integer.toString(score));
        //}
        return accuracyI;
    }

    private void checkAccuracyAgainstLives(int accuracy) {
        int lives = mState.getLivesRemaining();
        if (accuracy < lifeLossThreshhold) {
            mState.setLivesRemaining(lives - 1);
            Log.d(DEBUG_TAG, "should lose life: " + Boolean.toString(accuracy < lifeLossThreshhold) + " lives remaining: " + lives);
        } else if (accuracy >= 99) {
            mState.setLivesRemaining(lives + 1);
        }
        mTvLivesRemaining.setText(getString(R.string.lives_remaining) + " " + mState.getLivesRemaining());
        Log.d(DEBUG_TAG, "checkAccuracyAgainstLives lives remaining: " + mState.getLivesRemaining());
    }

    private void addToStateRunningScore(int newScore) {
        mState.setRunningScoreTotal(newScore);
        mTvScore.setText(getString(R.string.score) + " " + mState.getRunningScoreTotal());
    }

    private void checkLivesLeft(int lives) {
        if (lives == 0) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mDialogFragment = new OutOfLivesDialogFragment();
            mDialogFragment.show(ft, OUT_OF_LIVES_DIALOG);
        }
    }

    private boolean advanceToNextLevel(int currentTarget, int maxTarget) {
        if (currentTarget == maxTarget) {
            return true;
        }
        return false;
    }

    private void launchFadeCounterActivity() {
        Intent intent = new Intent(CounterActivity.this, FadeOutCounterActivity.class);
        startActivity(intent);
    }


//  Listener methods for AsyncTasks
    @Override
    public void onDbScoreUpdated() {
        // TODO write method in db helper qureying the updated score and Log it to console
        Log.d(DEBUG_TAG, "updated score from update db async: " + Integer.toString(mDbHelper.queryScoreFromDb()));
        ResetNextTurnAsync resetNextTurnAsync = new ResetNextTurnAsync(this, this, mTvCounter);
        resetNextTurnAsync.execute();

    }

    @Override
    public void onNextTurnReset() {
        // TODO put reset button methods in here, get rid of reset button
        if (advanceToNextLevel(currentTurn, NUM_OF_TURNS_PER_LEVEL)) {

            launchFadeCounterActivity();
        }
        displayTarget(generateTarget());
//              reset the value for the time here, after the time counting background thread has been
//              interrupted, so that the textview values don't update when the stop button is pushed
        resetTimeValues();

        resetCounter();
    }


//  Dialog fragment interaction methods
    @Override
    public void onOkClicked() {
        mDialogFragment.dismiss();
        resetCounter();
        resetLives();
        resetScore();
    }

    @Override
    public void onExitClicked() {
        mDialogFragment.dismiss();
        finish();
    }

}

