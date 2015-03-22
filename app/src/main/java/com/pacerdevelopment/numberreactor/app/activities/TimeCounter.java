package com.pacerdevelopment.numberreactor.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.pacerdevelopment.numberreactor.app.R;
import com.pacerdevelopment.numberreactor.app.application.ApplicationState;
import com.pacerdevelopment.numberreactor.app.db.DBHelper;
import com.pacerdevelopment.numberreactor.app.util.ButtonDrawableView;
import com.pacerdevelopment.numberreactor.app.util.GameInfoDisplayer;
import com.pacerdevelopment.numberreactor.app.util.ResetNextTurnAsync;
import com.pacerdevelopment.numberreactor.app.util.ResetNextTurnListener;

/**
 * Created by jeffwconaway on 2/27/15.
 */
public abstract class TimeCounter extends FragmentActivity {

    public static final String DEBUG_TAG = "jwc";

    protected static boolean isStartClickable;
    protected static boolean isStartFlashing;
    protected static boolean isStopClickable;
    protected static boolean isStopFlashing;

    protected static Handler handler;

    protected static LayerDrawable startButtonDisengaged;
    protected static LayerDrawable startButtonEngaged;
    protected static LayerDrawable startButtonArmed;
    protected static LayerDrawable stopButtonDisengaged;
    protected static LayerDrawable stopButtonEngaged;
    protected static LayerDrawable stopButtonArmed;

    protected static ApplicationState state;
    protected static GameInfoDisplayer gameInfoDisplayer;
    protected DBHelper mDbHelper;

    protected static long startTime;

    protected static SharedPreferences prefs;
    protected static PackageInfo versionInfo;
    protected static String appNamePlusVersion;
    protected static String dbNotNullPrefsKey;


    protected static final int BEGINNING_TARGET_LEVEL_ONE = 2;
    protected static final int TURNS_PER_LEVEL = 4;

    protected static final int LAST_TURN_RESET_BEFORE_NEW_ACTIVITY = -1;
    protected static final int NORMAL_TURN_RESET = 0;

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
        handler = new Handler();
        initButtonDrawables();
        initSharedPrefsElements();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isStartClickable = true;
        isStopClickable = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.time_counter, menu);
        return true;
    }


    protected PackageInfo getPackageInfo() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo;
    }

    private void initSharedPrefsElements() {
        versionInfo = getPackageInfo();
        appNamePlusVersion = getString(R.string.app_name) + versionInfo.versionName;
        dbNotNullPrefsKey = getString(R.string.prefs_db_not_null_key);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    protected boolean checkSharedPrefsForPreviousInstall() {
        return prefs.getBoolean(appNamePlusVersion, false);
    }

    protected void setSharedPrefsGameInstalled(boolean isInstalled) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(appNamePlusVersion, isInstalled);
        editor.commit();
    }

    protected static boolean checkSharedPrefsForDbNotNull() {
        return prefs.getBoolean(dbNotNullPrefsKey, false);
    }

    protected static void setSharedPrefsDbNotNull() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(dbNotNullPrefsKey, true);
        editor.commit();
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
            if (checkSharedPrefsForDbNotNull()) {
                Intent intent = new Intent(this, ViewStatsActivity.class);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    protected static double calculateRoundedCount(long elapsedCount, double counterCeiling) {
        return state.roundElapsedCount(elapsedCount, counterCeiling);
    }

    protected static String generateRoundedCountStr(double roundedCount) {
        return String.format(DOUBLE_FORMAT, roundedCount);
    }

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


    /*******************************************************************/

    protected static long retrieveElapsedTime() {
        return SystemClock.elapsedRealtime() - startTime;
    }

    protected static boolean checkElapsedTimeAgainstDuration(double duration) {
        return retrieveElapsedTime() >= duration;
    }

    protected static long incrementElapsedCount(long elapsedCount) {
        return elapsedCount + COUNTER_INCREMENT_MILLIS;
    }

    protected static long showStopButtonArmed(long elapsed, long runningDur, Button stopButton) {
        if (elapsed >= runningDur) {
            FlashStopButtonRunnable runnable = new FlashStopButtonRunnable(stopButton);
            handler.post(runnable);
            return ARMED_STOP_BUTTON_FLASH_DURATION;
        }
        return 0;
    }

    protected static boolean isCounterAtMaxValue(long elapsedCount, long maxCounterValue) {
        return elapsedCount >= maxCounterValue && isStopClickable;
    }




    protected static class StartButtonArmedRunnable implements Runnable {
        Button mStartButton;
        Handler mHandler;

        public StartButtonArmedRunnable(Button startButton) {
            this.mStartButton = startButton;
            mHandler = new Handler();
        }
        @Override
        public void run() {
            showStartButtonArmed(mStartButton);
        }

        private void showStartButtonArmed(Button startButton) {
            long startTime = SystemClock.elapsedRealtime();
            long elapsedTime;
            long runningFlashDuration = ARMED_START_BUTTON_FLASH_DURATION;
            while (isStartClickable) {
                elapsedTime = SystemClock.elapsedRealtime() - startTime;
                if (elapsedTime >= runningFlashDuration) {
                    FlashStartButtonRunnable runnable = new FlashStartButtonRunnable(startButton);
                    mHandler.post(runnable);
                    runningFlashDuration += ARMED_START_BUTTON_FLASH_DURATION;
                }
            }
        }
    }

    protected static class FlashStartButtonRunnable implements  Runnable {

        Button mStartButton;

        public FlashStartButtonRunnable(Button startButton) {
            this.mStartButton = startButton;
        }
        @Override
        public void run() {
            flashStartButtonArmed(mStartButton);
        }
    }


    protected static class FlashStopButtonRunnable implements  Runnable {

        Button mStopButton;
        public FlashStopButtonRunnable(Button stopButton) {
            this.mStopButton = stopButton;
        }
        @Override
        public void run() {
            flashStopButtonArmed(mStopButton);
        }
    }

}
