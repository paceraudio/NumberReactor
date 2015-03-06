package com.paceraudio.numberreactor.app.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.paceraudio.numberreactor.app.R;
import com.paceraudio.numberreactor.app.application.ApplicationState;
import com.paceraudio.numberreactor.app.db.DBHelper;
import com.paceraudio.numberreactor.app.util.ButtonDrawableView;
import com.paceraudio.numberreactor.app.util.GameInfoDisplayer;
import com.paceraudio.numberreactor.app.util.ResetNextTurnAsync;
import com.paceraudio.numberreactor.app.util.ResetNextTurnListener;

/**
 * Created by jeffwconaway on 2/27/15.
 */
public abstract class TimeCounter extends FragmentActivity {

    public static final String DEBUG_TAG = "jwc";

    protected static boolean isStartClickable;
    protected static boolean isStartFlashing;
    protected static boolean isStopClickable;
    protected static boolean isStopFlashing;

    /*protected static TextView tvCounter;
    protected static TextView tvTarget;
    protected static TextView tvAccuracy;
    protected static TextView tvLives;
    protected static TextView tvScore;
    protected static TextView tvLevel;*/

    /*protected static Button startButton;
    protected static Button stopButton;*/

    protected static LayerDrawable startButtonDisengaged;
    protected static LayerDrawable startButtonEngaged;
    protected static LayerDrawable startButtonArmed;
    protected static LayerDrawable stopButtonDisengaged;
    protected static LayerDrawable stopButtonEngaged;
    protected static LayerDrawable stopButtonArmed;

    protected static ApplicationState state;
    protected static GameInfoDisplayer gameInfoDisplayer;
    protected DBHelper mDbHelper;

    protected static final int BEGINNING_TARGET_LEVEL_ONE = 2;
    protected static final int TURNS_PER_LEVEL = 2;

    protected static final int LAST_TURN_RESET_BEFORE_NEW_ACTIVITY = -1;
    protected static final int NORMAL_TURN_RESET = 0;

    protected static final int DOUBLE_LIVES_GAINED = 2;
    protected static final int LIFE_GAINED = 1;
    protected static final int LIFE_NEUTRAL = 0;
    protected static final int LIFE_LOST = -1;

    protected static final long COUNTER_INCREMENT_MILLIS = 10;
    protected static final double MILLIS_IN_SECONDS = 1000;
    protected static final String DOUBLE_FORMAT = "%.2f";

    // Constants for armed button flashing
    protected static final long ARMED_START_BUTTON_FLASH_DURATION = 500;
    protected static final long ARMED_STOP_BUTTON_FLASH_DURATION = 90;

    protected static final int SCORE_DOUBLE_THRESHOLD = 98;
    protected static final int SCORE_QUADRUPLE_THRESHOLD = 99;
    protected static final int TWO = 2;
    protected static final int FOUR = 4;


    protected void initButtonDrawables() {
        ButtonDrawableView buttonDrawableView = new ButtonDrawableView(this);
        startButtonDisengaged = buttonDrawableView.mStartDisengagedDrawables;
        startButtonEngaged = buttonDrawableView.mStartEngagedDrawables;
        startButtonArmed = buttonDrawableView.mStartArmed;
        stopButtonArmed = buttonDrawableView.mStopArmed;
        stopButtonDisengaged = buttonDrawableView.mStopDisengagedDrawables;
        stopButtonEngaged = buttonDrawableView.mStopEngagedDrawables;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state = (ApplicationState) getApplicationContext();
        gameInfoDisplayer = new GameInfoDisplayer(ApplicationState.getAppContext());
        mDbHelper = new DBHelper(this);
        initButtonDrawables();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(DEBUG_TAG, "onRestart() end " + getLocalClassName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(DEBUG_TAG, "onStart() end " + getLocalClassName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //initButtons();
        isStartClickable = true;
        isStopClickable = false;
        Log.d(DEBUG_TAG, "onResume() end " + getLocalClassName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "onPause() end " + getLocalClassName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(DEBUG_TAG, "onStop() end " + getLocalClassName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG_TAG, "onDestroy() end " + getLocalClassName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.time_counter, menu);
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

    /*protected void initButtons() {
        startButton = (Button) findViewById(R.id.b_start);
        stopButton = (Button) findViewById(R.id.b_stop);
    }*/

    //protected abstract void onCounterStopped(long elapsedCount);

    protected static double calculateRoundedCount(long elapsedCount, double counterCeiling) {
        //return state.roundElapsedCountLong(elapsedCount, fromActivity,counterCeiling);
        return state.roundElapsedCount(elapsedCount, counterCeiling);
    }

    protected static String generateRoundedCountStr(double roundedCount) {
        return String.format(DOUBLE_FORMAT, roundedCount);
    }

    /*protected abstract int calculateAccuracy(double target, double elapsedCount);*/

    protected static void updateStateScore(int score) {
        state.setmTurnPoints(score);
        state.updateRunningScoreTotal(score);
    }

    protected static void changeCounterColorIfDeadOn(double roundedCount, double target,
                                              TextView tvCounter) {
        if (roundedCount == target) {
            tvCounter.setTextColor(ApplicationState.getAppContext().getResources().getColor(R.color.glowGreen));
        }
    }

    protected static void launchResetNextTurnAsync(ResetNextTurnListener listener, Context context,
                                            TextView tvCounter, TextView tvLives,
                                            TextView tvScore, int accuracy,
                                            boolean lifeLossPossible) {

        ResetNextTurnAsync resetNextTurnAsync = new ResetNextTurnAsync(listener, context,
                tvCounter, tvLives, tvScore);

        int param0 = checkIfLastTurn();
        int param1 = state.numOfLivesGainedOrLost(accuracy, lifeLossPossible);
        int param2 = state.getmTurnPoints();

        resetNextTurnAsync.execute(param0, param1, param2);
    }

    protected static int checkIfLastTurn() {
        if (state.getmTurn() == TURNS_PER_LEVEL) {
            return LAST_TURN_RESET_BEFORE_NEW_ACTIVITY;
        } else {
            return NORMAL_TURN_RESET;
        }
    }

    protected static void flashStartButtonArmed(Button button) {
        if (isStartClickable) {
            if (isStartFlashing) {
                gameInfoDisplayer.showButtonState(button, startButtonDisengaged);
                isStartFlashing = false;
            } else {
                gameInfoDisplayer.showButtonState(button, startButtonArmed);
                isStartFlashing = true;
            }
        }
    }

    protected static void flashStopButtonArmed(Button button) {
        if (isStopClickable) {
            if (isStopFlashing) {
                gameInfoDisplayer.showButtonState(button, stopButtonDisengaged);
                isStopFlashing = false;
            } else {
                gameInfoDisplayer.showButtonState(button, stopButtonArmed);
                isStopFlashing = true;

            }
        }
    }

/*
    protected abstract void updateCounter(long elapsedCount, int alpha);
*/
}
