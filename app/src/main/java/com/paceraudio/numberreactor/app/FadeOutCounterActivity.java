package com.paceraudio.numberreactor.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;


public class FadeOutCounterActivity extends FragmentActivity implements FadeCounterListener, ResetNextTurnListener {

    static final String DEBUG_TAG = "jwc";

    private TextView mTvFadeCounter;
    private TextView mTvFadeTarget;
    private TextView mTvFadeAccuracy;
    private TextView mTvFadeLives;
    private TextView mTvFadeScore;
    private TextView mTvFadeLevel;
    private Button mFadeStartButton;
    private Button mFadeStopButton;
    private FrameLayout mFadeStartFrame;
    private FrameLayout mFadeStopFrame;


    private long mStartTime;
    private double mElapsedSeconds;
    private double mNextCount;
    private double mFadeIncrement;
    private double mRunningFadeTime;
    private int mFadeCounterColor;
    private int mAlphaValue;
    private int mRedValue;
    private int mGreenValue;
    private int mBlueValue;

    private boolean mIsGettingExtraLife = false;

    private static final String EXTRA_LIFE_FROM_FADE_COUNTER_ROUND = "extraLifeFromFadeCounterRound";
//    private static final String LEVEL_COMPLETED = "levelCompleted";

    private static final int LAST_TURN_RESET_BEFORE_NEW_ACTIVITY = -1;
    private static final int DEFAULT_FADE_COUNTER_TARGET = 11;
    private static final int DEFAULT_COUNTER_CEILING = DEFAULT_FADE_COUNTER_TARGET + 5;
    private static final double DEFAULT_FADE_RATIO = .60;
    private static final int NORMAL_TURN_RESET = 0;


//    DialogFragment mDialogFragment;
    Handler mHandler;
    Thread mFadeCounterThread;
    DBHelper mDbHelper;
    ApplicationState mState;
    GameInfoDisplayer mGameInfoDisplayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fade_out_counter);
        mState = (ApplicationState) getApplicationContext();
        mDbHelper = new DBHelper(this);
        mTvFadeCounter = (TextView) findViewById(R.id.t_v_fade_counter);
        mTvFadeTarget = (TextView) findViewById(R.id.t_v_fade_target);
        mTvFadeAccuracy = (TextView) findViewById(R.id.t_v_fade_accuracy_rating);
        mTvFadeLives = (TextView) findViewById(R.id.t_v_fade_lives_remaining);
        mTvFadeScore = (TextView) findViewById(R.id.t_v_fade_score);
        mTvFadeLevel = (TextView) findViewById(R.id.t_v_fade_level);
        mFadeStartButton = (Button) findViewById(R.id.fade_b_start);
        mFadeStopButton = (Button) findViewById(R.id.fade_b_stop);
        mFadeStartFrame = (FrameLayout) findViewById(R.id.fade_frame_b_start);
        mFadeStopFrame = (FrameLayout) findViewById(R.id.fade_frame_b_stop);
        mGameInfoDisplayer = new GameInfoDisplayer(this);
        mState.setTarget(DEFAULT_FADE_COUNTER_TARGET);
    }

    @Override
    protected void onStart() {
        super.onStart();
        UpdateLevelDbAsync updateLevelDbAsync = new UpdateLevelDbAsync(this);
        updateLevelDbAsync.execute(mState.getLevel());
//        This is when the counter should have completely faded out.
        double fadeRange  = DEFAULT_FADE_COUNTER_TARGET * DEFAULT_FADE_RATIO;
//        This assumes the alpha value of the color is at its max
        mFadeIncrement = fadeRange / 255;
        mRunningFadeTime = mFadeIncrement;
        mNextCount = .01;
        mFadeCounterColor = mTvFadeCounter.getCurrentTextColor();
        mAlphaValue = Color.alpha(mFadeCounterColor);
        mRedValue= Color.red(mFadeCounterColor);
        mGreenValue = Color.green(mFadeCounterColor);
        mBlueValue = Color.blue(mFadeCounterColor);
        mGameInfoDisplayer.displayAllGameInfo(mTvFadeTarget, mTvFadeAccuracy, mTvFadeLives, mTvFadeScore, mTvFadeLevel);

    }

    @Override
    protected void onResume() {
        super.onResume();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        mFadeStartButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mStartTime = SystemClock.elapsedRealtime();
                   mGameInfoDisplayer.showStartButtonEngaged(mFadeStartButton, mFadeStartFrame );
                    Log.d(DEBUG_TAG, "start clicked at: " + mStartTime);
                    mFadeCounterThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (mElapsedSeconds < DEFAULT_COUNTER_CEILING && !mFadeCounterThread.isInterrupted()) {
                                mElapsedSeconds = TimeCounter.calcElapsedSeconds(mStartTime);
                                if (mElapsedSeconds >= mNextCount) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mTvFadeCounter.setText(String.format("%.2f", mElapsedSeconds));
//                                            Log.d(DEBUG_TAG, "mElapsed time seconds: " + mElapsedSeconds);
                                            if (mAlphaValue > 0 && mElapsedSeconds >= mRunningFadeTime) {
                                                mAlphaValue--;
                                                int color = Color.argb(mAlphaValue, mRedValue, mGreenValue, mBlueValue);
                                                mTvFadeCounter.setTextColor(color);
                                                mRunningFadeTime += mFadeIncrement;
                                            }
                                        }
                                    });
                                    mNextCount+= 0.01;
                                }

                            }
                            if (mFadeCounterThread.isInterrupted()) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onFadeCountStopped(mElapsedSeconds);
                                    }
                                });

                                return;
                            }
                            if (mElapsedSeconds >= DEFAULT_COUNTER_CEILING) {
//                                mHandler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        onFadeCountComplete(mElapsedSeconds);
//                                    }
//                                });
                                mFadeCounterThread.interrupt();
                            }
                        }
                    });
                    mFadeCounterThread.start();
                }
                return false;
            }
        });
        mFadeStopButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mFadeCounterThread.isInterrupted()) {
                        ;
                    } else {

                        mFadeCounterThread.interrupt();
                        mGameInfoDisplayer.showStopButtonEngaged(mFadeStopButton, mFadeStopFrame);
                        mGameInfoDisplayer.showStartButtonNotEngaged(mFadeStartButton, mFadeStartFrame);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fade_out_counter, menu);
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

    public void onFadeCountStopped(Double seconds) {
//        mTvFadeCounter.setText(String.format("%.2f", seconds));
//        mTvFadeCounter.setTextColor(mFadeCounterColor);

        //      Round the elapsed accelerated count to 2 decimal places
        double roundedCount = mState.roundElapAccelCount(seconds);

//      Convert rounded value to a String to display
        String roundedCountStr = String.format("%.2f", roundedCount);

        mTvFadeCounter.setTextColor(mFadeCounterColor);
        mTvFadeCounter.setText(roundedCountStr);

//        calc the accuracy
        int accuracy = mState.calcAccuracy(DEFAULT_FADE_COUNTER_TARGET, roundedCount);
        mState.setTurnAccuracy(accuracy);
        if (accuracy > 98) {
            mIsGettingExtraLife = true;
            int lives = mState.getLives();
            mState.setLives(lives + 1);
        }
//        mTvFadeScore.setTextColor(getResources().getColor(R.color.orange));
        mGameInfoDisplayer.displayImmediateGameInfoAfterFadeCountTurn(mTvFadeAccuracy, mTvFadeLives, mTvFadeScore);
//        mGameInfoDisplayer.displayAllGameInfo(mTvFadeTarget, mTvFadeAccuracy, mTvFadeLives, mTvFadeScore, mTvFadeLevel);
        ResetNextTurnAsync resetNextTurnAsync = new ResetNextTurnAsync(this, this, mTvFadeCounter);
        resetNextTurnAsync.execute(LAST_TURN_RESET_BEFORE_NEW_ACTIVITY);
    }

//    Listener method runs after ResetNextTurnAsync is finished
    @Override
    public void onNextTurnReset() {
        Log.d(DEBUG_TAG, "ResetNextTurnAsync returning to FadeCounter!!!!");
        Intent intent = new Intent(this, CounterActivity.class);
//        TODO make this t or f based on performance

        if (mIsGettingExtraLife) {
            intent.putExtra(EXTRA_LIFE_FROM_FADE_COUNTER_ROUND, true);
            mIsGettingExtraLife = false;
        } else {
            intent.putExtra(EXTRA_LIFE_FROM_FADE_COUNTER_ROUND, false);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onFadeCountComplete(Double seconds) {
        mGameInfoDisplayer.showStopButtonNotEngaged(mFadeStopButton, mFadeStopFrame);
        mTvFadeCounter.setText(String.format("%.2f", seconds));
        mTvFadeCounter.setTextColor(0xffff0000);
        ResetNextTurnAsync resetNextTurnAsync = new ResetNextTurnAsync(this, this, mTvFadeCounter);
        resetNextTurnAsync.execute(LAST_TURN_RESET_BEFORE_NEW_ACTIVITY);
    }
}
