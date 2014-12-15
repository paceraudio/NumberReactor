package com.paceraudio.numberreactor.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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

    private long mStartTime;
    private double mElapsedSeconds;
    private double mNextCount;
    private int mTarget = 10;
    private int mOverTargetRange = mTarget + 5;

    private double mFadeRange;
    private double mFadeIncrement;
    private double mRunningFadeTime;
    private int mFadeCounterColor;
    private int mAlphaValue;
    private int mRedValue;
    private int mGreenValue;
    private int mBlueValue;

    private static final String FADE_COUNT_DIALOG_FRAGMENT = "customFadeCountDialogFragment";
    private static final String EXTRA_LIFE_FROM_FADE_COUNTER_ROUND = "extraLifeFromFadeCounterRound";
    private static final String LEVEL_COMPLETED = "levelCompleted";

    private static final int LAST_TURN_RESET_BEFORE_NEW_ACTIVITY = -1;
    private static final int NORMAL_TURN_RESET = 0;


//    DialogFragment mDialogFragment;
    Handler mHandler;
    Thread mFadeCounterThread;
    DBHelper mDbHelper;
    ApplicationState mState;


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
    }

    @Override
    protected void onStart() {
        super.onStart();
        UpdateLevelDbAsync updateLevelDbAsync = new UpdateLevelDbAsync(this);
        updateLevelDbAsync.execute(mState.getLevel());
//        This is when the counter should have completely faded out, at half way through the fade
        mFadeRange = mTarget * .65;
//        This assumes the alpha value of the color is at its max
        mFadeIncrement = mFadeRange / 255;
        mRunningFadeTime = mFadeIncrement;
        mNextCount = .01;
        mFadeCounterColor = mTvFadeCounter.getCurrentTextColor();
        mAlphaValue = Color.alpha(mFadeCounterColor);
        mRunningFadeTime = Color.red(mFadeCounterColor);
        mGreenValue = Color.green(mFadeCounterColor);
        mBlueValue = Color.blue(mFadeCounterColor);


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
                    Log.d(DEBUG_TAG, "start clicked at: " + mStartTime);
//                    mFadeOutCounterAsync = new FadeOutCounterAsync(FadeOutCounterActivity.this, FadeOutCounterActivity.this);
//                    mFadeOutCounterAsync.execute(mStartTime, mTarget);
                    mFadeCounterThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (mElapsedSeconds < mOverTargetRange && !mFadeCounterThread.isInterrupted()) {
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

                                return;
                            }
                            if (mElapsedSeconds >= mOverTargetRange) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        onFadeCountComplete(mElapsedSeconds);
                                    }
                                });

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
                        onFadeCountCancelled(mElapsedSeconds);
                        mFadeCounterThread.interrupt();
//                        String mTarget = mTvFadeTarget.getText().toString();
//                        String userValue = mTvFadeCounter.getText().toString();
//                        compareUserValueToTarget(userValue, mTarget, mTvFadeCounter);
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

    public void onFadeCountCancelled(Double seconds) {
        mTvFadeCounter.setText(String.format("%.2f", seconds));
        mTvFadeCounter.setTextColor(mFadeCounterColor);
        int level = mState.getLevel();
        ResetNextTurnAsync resetNextTurnAsync = new ResetNextTurnAsync(this, this, mTvFadeCounter);
        resetNextTurnAsync.execute(LAST_TURN_RESET_BEFORE_NEW_ACTIVITY);
    }

//    Listener method runs after ResetNextTurnAsync is finished
    @Override
    public void onNextTurnReset() {
        Log.d(DEBUG_TAG, "ResetNextTurnAsync returning to FadeCounter!!!!");
        Intent intent = new Intent(this, CounterActivity.class);
//        TODO make this t or f based on performance
        intent.putExtra(EXTRA_LIFE_FROM_FADE_COUNTER_ROUND, true);
        intent.putExtra(LEVEL_COMPLETED, true);
        startActivity(intent);
    }

    @Override
    public void onFadeCountComplete(Double seconds) {
        mTvFadeCounter.setText(String.format("%.2f", seconds));
        mTvFadeCounter.setTextColor(0xffff0000);
    }

//    @Override
//    public void onFragmentInteraction() {
//        Log.d(DEBUG_TAG, "onFragmentInteraction running in FadeOutCounterActivity");
//        mDialogFragment.dismiss();
//    }

    private void compareUserValueToTarget(String userValue, String target, TextView tv) {
        if (userValue.equals(target)) {
            tv.setTextColor(getResources().getColor(R.color.green));
            int chancesLeft = mState.getLivesRemaining();
            mState.setLivesRemaining(chancesLeft + 1);
        } else {
            tv.setTextColor(getResources().getColor(R.color.red));
        }
    }
}
