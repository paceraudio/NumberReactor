package com.pacerdevelopment.numberreactor.app.counter.vp_counter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Typeface;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.pacerdevelopment.numberreactor.app.R;
import com.pacerdevelopment.numberreactor.app.application.NrApp;
import com.pacerdevelopment.numberreactor.app.model.db.DBHelper;
import com.pacerdevelopment.numberreactor.app.model.GameState;
import com.pacerdevelopment.numberreactor.app.util.ButtonDrawableView;
import com.pacerdevelopment.numberreactor.app.util.CustomTypeface;
import com.pacerdevelopment.numberreactor.app.util.GameInfoDisplayer;
import com.pacerdevelopment.numberreactor.app.util.ResetNextTurnAsync;
import com.pacerdevelopment.numberreactor.app.util.ResetNextTurnListener;
import com.pacerdevelopment.numberreactor.app.vp_settings.SettingsActivity;
import com.pacerdevelopment.numberreactor.app.vp_stats.ViewStatsActivity;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by jeffwconaway on 2/27/15.
 */
public abstract class TimeCounter extends Activity implements CounterContract.View {

    protected static volatile boolean isStartClickable;
    protected static volatile boolean isStartFlashing;
    protected static volatile boolean isStopClickable;
    protected static volatile boolean isStopFlashing;

    protected static Handler handler;

    protected static LayerDrawable startButtonDisengaged;
    protected static LayerDrawable startButtonEngaged;
    protected static LayerDrawable startButtonArmed;
    protected static LayerDrawable stopButtonDisengaged;
    protected static LayerDrawable stopButtonEngaged;
    protected static LayerDrawable stopButtonArmed;

    protected double roundedCount;

    //protected static NrApp nrApp;

    private CounterContract.Presenter presenter;
    protected static GameInfoDisplayer gameInfoDisplayer;
    //protected static GameState gameState;
    protected DBHelper mDbHelper;
    //protected static CustomTypeface customTypeface;

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
    protected static final int ZERO = 0;

    protected static final String FONT_ASSET_PATH = "fonts/Roboto-Regular.ttf";
    protected static final String TEST_FONT_ASSET_PATH = "fonts/Roboto-LightItalic.ttf";


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
        //gameState = NrApp.getModel();
        //nrApp = (NrApp) getApplicationContext();
        gameInfoDisplayer = new GameInfoDisplayer(NrApp.getAppContext());
        //customTypeface = CustomTypeface.getInstance();
        //mDbHelper = new DBHelper(this);
        handler = new Handler();
        initButtonDrawables();
        //initSharedPrefsElements();

        presenter = new CounterPresenter(this, NrApp.getModel());
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
        quitFlashingStartButton();
        StartButtonArmedRunnable.cancelThread();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.time_counter, menu);
        return true;
    }

    /*protected PackageInfo getPackageInfo() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo;
    }*/

    private Typeface obtainTypeface(String assetPath) {
        return CustomTypeface.get(this, assetPath);
    }

    protected void applyTypeface(ArrayList<TextView> tvList) {
        Typeface tf = obtainTypeface(FONT_ASSET_PATH);
        for (TextView tv : tvList) {
            tv.setTypeface(tf);
        }
    }

    protected ArrayList<TextView> makeTvArrayList(TextView counter, TextView target, TextView accuracy,
        TextView lives, TextView score, TextView level) {
        TextView[] tvArray = {counter, target, accuracy, lives, score, level};
        return new ArrayList<>(Arrays.asList(tvArray));
    }

/*    private void initSharedPrefsElements() {
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
    }*/

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
            presenter.onViewGameStats();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void viewStats() {
        Intent intent = new Intent(this, ViewStatsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRoundedCount(double roundedCount) {
        this.roundedCount = roundedCount;
    }

    protected void obtainRoundedCount(long elapsedCount, double counterCeiling) {
        presenter.obtainRoundedCount(elapsedCount, counterCeiling);
    }

    protected static double calculateRoundedCount(long elapsedCount, double counterCeiling) {
        return gameState.roundElapsedCount(elapsedCount, counterCeiling);
    }

    protected static String generateRoundedCountStr(double roundedCount) {
        return String.format(DOUBLE_FORMAT, roundedCount);
    }

    protected static void updateStateScore(int score) {
        gameState.setTurnPoints(score);
        gameState.updateRunningScoreTotal(score);
    }

    protected static void changeCounterColorIfDeadOn(double roundedCount, double target,
                                              TextView tvCounter) {
        if (roundedCount == target) {
            tvCounter.setTextColor(NrApp.getAppContext().getResources().getColor(R.color.glowGreen));
        }
    }

    protected static void launchResetNextTurnAsync(ResetNextTurnListener listener, Context context,
                                            TextView tvCounter, TextView tvLives,
                                            TextView tvScore, int accuracy,
                                            boolean lifeLossPossible) {

        ResetNextTurnAsync resetNextTurnAsync = new ResetNextTurnAsync(listener, context,
                tvCounter, tvLives, tvScore);

        int param0 = checkIfLastTurn();
        int param1 = gameState.numOfLivesGainedOrLost(accuracy, lifeLossPossible);
        int param2 = gameState.getTurnPoints();

        resetNextTurnAsync.execute(param0, param1, param2);
    }

    protected static int checkIfLastTurn() {
        if (gameState.getTurn() == TURNS_PER_LEVEL) {
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

    protected void quitFlashingStartButton() {
        //StartButtonArmedRunnable only runs while isStartClickable = true
        isStartClickable = false;
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
            Thread.currentThread().interrupt();
        }

        public static void cancelThread() {
            Thread.currentThread().interrupt();
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
