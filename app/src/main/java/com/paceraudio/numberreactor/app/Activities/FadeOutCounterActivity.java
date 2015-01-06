package com.paceraudio.numberreactor.app.activities;

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

import com.paceraudio.numberreactor.app.application.ApplicationState;
import com.paceraudio.numberreactor.app.db.DBHelper;
import com.paceraudio.numberreactor.app.util.GameInfoDisplayer;
import com.paceraudio.numberreactor.app.R;
import com.paceraudio.numberreactor.app.util.ResetNextTurnAsync;
import com.paceraudio.numberreactor.app.util.ResetNextTurnListener;


public class FadeOutCounterActivity extends FragmentActivity implements
        ResetNextTurnListener {

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

    private boolean mIsStartClickable;
    private boolean mIsStopClickable;


    private long mStartTime;
    private long mElapsedMillis;
    private double mElapsedSeconds;
    private long mDuration;
    private double mTarget;
    private double mFadeIncrement;
    private double mRunningFadeTime;
    private int mFadeCounterColor;
    private int mAlphaValue;
    private int mRedValue;
    private int mGreenValue;
    private int mBlueValue;

    private boolean mIsGettingExtraLife = false;

    private static final String EXTRA_LIFE_FROM_FADE_COUNTER_ROUND =
            "extraLifeFromFadeCounterRound";
    private static final String FROM_FADE_COUNTER_ACTIVITY = "fromFadeCounterActivity";

    //    private static final String LEVEL_COMPLETED = "levelCompleted";

    private static final int LAST_TURN_RESET_BEFORE_NEW_ACTIVITY = -1;

    private static final int DOUBLE_LIVES_GAINED = 2;
    private static final int LIFE_GAINED = 1;
    private static final int LIFE_NEUTRAL = 0;
    private static final int LIFE_LOST = -1;

    private static final double DEFAULT_FADE_COUNTER_TARGET = 10.00;
    private double mCounterCeiling;
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
        mFadeStartButton = (Button) findViewById(R.id.b_fade_start);
        mFadeStopButton = (Button) findViewById(R.id.b_fade_stop);
        mFadeStartFrame = (FrameLayout) findViewById(R.id.f_l_for_fade_b_start);
        mFadeStopFrame = (FrameLayout) findViewById(R.id.f_l_for_fade_b_stop);
        mGameInfoDisplayer = new GameInfoDisplayer(this);
        setStateTargetBasedOnLevel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //        This is when the counter should have completely faded out.
        double fadeRange = DEFAULT_FADE_COUNTER_TARGET * DEFAULT_FADE_RATIO;
        //        This assumes the alpha value of the color is at its max
        mFadeIncrement = fadeRange / 255;
        mRunningFadeTime = mFadeIncrement;
        mFadeCounterColor = mTvFadeCounter.getCurrentTextColor();
        mAlphaValue = Color.alpha(mFadeCounterColor);
        mRedValue = Color.red(mFadeCounterColor);
        mGreenValue = Color.green(mFadeCounterColor);
        mBlueValue = Color.blue(mFadeCounterColor);
        mGameInfoDisplayer.displayAllGameInfo(mTvFadeTarget, mTvFadeAccuracy, mTvFadeLives,
                mTvFadeScore, mTvFadeLevel, FROM_FADE_COUNTER_ACTIVITY);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsStartClickable = true;
        mIsStopClickable = false;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        mFadeStartButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    if (mIsStartClickable) {
                        mStartTime = SystemClock.elapsedRealtime();
                        mGameInfoDisplayer.showStartButtonEngaged(mFadeStartButton,
                                mFadeStartFrame);
                        Log.d(DEBUG_TAG, "start clicked at: " + mStartTime);
                        mFadeCounterThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mDuration = 10;
                                while (mElapsedMillis < mCounterCeiling &&
                                        !mFadeCounterThread.isInterrupted()) {
                                    mElapsedMillis = SystemClock.elapsedRealtime() - mStartTime;

                                    if (mElapsedMillis >= mDuration) {
                                        mElapsedSeconds = mElapsedMillis / 1000d;

                                        mHandler.postAtTime(new Runnable() {
                                            @Override
                                            public void run() {
                                                mTvFadeCounter.setText(String.format("%.2f",
                                                        mElapsedSeconds));
                                                if (mAlphaValue > 0 && mElapsedSeconds >=
                                                        mRunningFadeTime) {
                                                    mAlphaValue--;
                                                    int color = Color.argb(mAlphaValue,
                                                            mRedValue, mGreenValue, mBlueValue);
                                                    mTvFadeCounter.setTextColor(color);
                                                    mRunningFadeTime += mFadeIncrement;
                                                }
                                            }
                                        }, 0);
                                        mDuration += 10;
                                    }

                                }
                                if (!mFadeCounterThread.isInterrupted()) {
                                    mFadeCounterThread.interrupt();
                                }
                                if (mFadeCounterThread.isInterrupted()) {
                                    mHandler.postAtTime(new Runnable() {
                                        @Override
                                        public void run() {
                                            onFadeCountStopped(mElapsedMillis);
                                        }
                                    }, 0);
                                }

                            }
                        });
                        mFadeCounterThread.start();
                        mIsStartClickable = false;
                        mIsStopClickable = true;
                    }
                }
                return false;
            }
        });
        mFadeStopButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    if (mIsStopClickable) {

                        mFadeCounterThread.interrupt();

                        mIsStopClickable = false;
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

    public void onFadeCountStopped(long millis) {

        mGameInfoDisplayer.showStopButtonEngaged(mFadeStopButton, mFadeStopFrame);
        mGameInfoDisplayer.showStartButtonNotEngaged(mFadeStartButton,
                mFadeStartFrame);

        //      Round the elapsed accelerated count to 2 decimal places
        double roundedCount = mState.roundElapsedCountLong(millis, FROM_FADE_COUNTER_ACTIVITY, mCounterCeiling);

//        //TODO TESTING ONLY!!!!!!!!!!!!!!
//        roundedCount = mTarget;

        //      Convert rounded value to a String to display
        String roundedCountStr = String.format("%.2f", roundedCount);

        //        calc the accuracy
        int accuracy = mState.calcAccuracy(mTarget, roundedCount);

        mState.setTurnAccuracy(accuracy);

        if (roundedCount == mTarget) {
            mTvFadeCounter.setTextColor(getResources().getColor(R.color.green));
        } else {
            mTvFadeCounter.setTextColor(mFadeCounterColor);
        }
        mTvFadeCounter.setText(roundedCountStr);
        mGameInfoDisplayer.displayImmediateGameInfoAfterFadeCountTurn(mTvFadeAccuracy);

        ResetNextTurnAsync resetNextTurnAsync = new ResetNextTurnAsync(this, this,
                mTvFadeCounter, mTvFadeLives, mTvFadeScore);

        int param2;
        int param3;

        // See if there was a life gained or lost
        int livesGained = mState.numOfLivesGainedOrLost();
        if (livesGained == DOUBLE_LIVES_GAINED) {
            param2 = DOUBLE_LIVES_GAINED;
        } else if (livesGained == LIFE_GAINED) {
            param2 = LIFE_GAINED;

        } else {
            param2 = LIFE_NEUTRAL;
        }

        // No score is awarded in this activity, so set the score to dummy value -1, so that the
        // score textview does not blink in the reset turn async
        param3 = -1;
        resetNextTurnAsync.execute(LAST_TURN_RESET_BEFORE_NEW_ACTIVITY, param2, param3);
    }

    private void setStateTargetBasedOnLevel() {
        mTarget = DEFAULT_FADE_COUNTER_TARGET + mState.getLevel() - 1;
        mCounterCeiling = (mTarget + 10) * 1000;
        mState.setTarget(mTarget);
    }

    //    Listener method runs after ResetNextTurnAsync is finished
    @Override
    public void onNextTurnReset() {
        Log.d(DEBUG_TAG, "ResetNextTurnAsync returning to FadeCounter!!!!");
        Intent intent = new Intent(this, CounterActivity.class);
        setResult(RESULT_OK, intent);
        finish();
    }
}
