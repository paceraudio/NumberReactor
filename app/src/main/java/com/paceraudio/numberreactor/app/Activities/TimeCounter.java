package com.paceraudio.numberreactor.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.paceraudio.numberreactor.app.R;
import com.paceraudio.numberreactor.app.application.ApplicationState;
import com.paceraudio.numberreactor.app.db.DBHelper;
import com.paceraudio.numberreactor.app.util.ButtonDrawableView;
import com.paceraudio.numberreactor.app.util.GameInfoDisplayer;

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

    protected static LayerDrawable startButtonDisengagedDrawables;
    protected static LayerDrawable startButtonEngagedDrawables;
    protected static LayerDrawable startButtonArmedDrawables;
    protected static LayerDrawable stopButtonDisengagedDrawables;
    protected static LayerDrawable stopButtonEngagedDrawables;
    protected static LayerDrawable stopButtonArmedDrawables;

    protected ApplicationState state;
    protected static GameInfoDisplayer gameInfoDisplayer;
    protected DBHelper mDbHelper;

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




    protected void initButtonDrawables() {
        ButtonDrawableView buttonDrawableView = new ButtonDrawableView(this);
        startButtonDisengagedDrawables = buttonDrawableView.mStartDisengagedDrawables;
        startButtonEngagedDrawables = buttonDrawableView.mStartEngagedDrawables;
        startButtonArmedDrawables = buttonDrawableView.mStartArmed;
        stopButtonArmedDrawables = buttonDrawableView.mStopArmed;
        stopButtonDisengagedDrawables = buttonDrawableView.mStopDisengagedDrawables;
        stopButtonEngagedDrawables = buttonDrawableView.mStopEngagedDrawables;
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
        isStopClickable = false;
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
        Log.d(DEBUG_TAG, "onDestroy() end");
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

    protected abstract void onCounterStopped(long elapsedCount);

    protected String generateRoundedCountStr(double roundedCount) {
        return String.format(DOUBLE_FORMAT, roundedCount);
    }
}
