package com.paceraudio.numberreactor.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;


public class MainActivity extends FragmentActivity implements CounterListener, OutOfLivesDialogFragment.OnFragmentInteractionListener {

    static final String DEBUG_TAG = "jwc";

    private long startTime;
    private boolean startIsClickable;

    private Random gen;
    public static double target;
    public int maxTarget = 8;
    private int minTarget = 3;
    private int lifeLossThreshhold = 85;

    private TextView tvCounter;
    private TextView tvTarget;
    private TextView tvAccuracy;
    private TextView tvLivesRemaining;
    private TextView tvScore;

    private final static String OUT_OF_LIVES_DIALOG = "outOfLivesDialog";
    private DialogFragment mDialogFragment;

    CounterAsync mCounterAsync;

    public int mCount;

    ApplicationState state;


    int currentTurn;
    private static final int NUM_OF_TURNS_PER_LEVEL = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentTurn = 0;

    }

    @Override
    protected void onResume() {
        super.onResume();

        state = (ApplicationState) getApplicationContext();

        switch (state.getLevel()) {
            case 1:
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
        tvCounter = (TextView) findViewById(R.id.t_v_counter);
        tvAccuracy = (TextView) findViewById(R.id.t_v_accuracy_rating);
        tvLivesRemaining = (TextView) findViewById(R.id.t_v_lives_remaining);
        tvLivesRemaining.setText(this.getString(R.string.lives_remaining) + " " + state.getLivesRemaining());
        tvScore = (TextView) findViewById(R.id.t_v_score);
        tvScore.setText(this.getString(R.string.score) + " " + state.getRunningScoreTotal());
        startIsClickable = true;

        displayTarget(generateTarget());


        Button startButton = (Button) findViewById(R.id.b_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (startIsClickable) {
                    startTime = System.currentTimeMillis();
                    mCounterAsync = new CounterAsync(MainActivity.this, MainActivity.this);
                    // pass the system time when the button is clicked to the async task
                    mCounterAsync.execute(startTime, (long) target);
                    startIsClickable = false;
                    currentTurn++;
                    //Log.d(DEBUG_TAG, "Start clicked!");
                }
            }
        });

        Button stopButton = (Button) findViewById(R.id.b_stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // prevents null pointer if stop button is clicked with no async task running
                if (mCounterAsync.isCancelled()) {
                   // Log.d(DEBUG_TAG, "stop clicked, async task already cancelled");
                } else {
                    long elapsedTimeMillis = System.currentTimeMillis() - startTime;
                   // Log.d(DEBUG_TAG, "stop clicked while async task is running");
                    Log.d(DEBUG_TAG, "onStopClicked elapsed time: " + elapsedTimeMillis);

                    // cancel the async task
                    mCounterAsync.cancel(true);

                }
            }
        });

        Button resetButton = (Button) findViewById(R.id.b_reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTurn == NUM_OF_TURNS_PER_LEVEL) {
                    Intent intent = new Intent(MainActivity.this, FadeOutCounterActivity.class);
                    startActivity(intent);
                }
                //Log.d(DEBUG_TAG, "reset clicked");
                displayTarget(generateTarget());
                resetCounter();
            }
        });
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
    public void onCounterComplete(Double seconds) {
        tvCounter.setTextColor(0xffff0000);

        tvCounter.setText(String.format("%.2f", seconds));
    }

    // runs when CounterAsync is cancelled
    @Override
    public void onCounterCancelled(Double accelCount, int count) {
        //Log.d(DEBUG_TAG, "Main Activity: Accelerated Count via onCounterCancelled: " + accelCount);

        // assign a score
        int accuracy = calcAccuracy(target, accelCount);
        int score = accuracy;

        long onCounterCancelledElapsedTime = System.currentTimeMillis() - startTime;
        Log.d(DEBUG_TAG, "onCountCancElapsedTime: " + Long.toString(onCounterCancelledElapsedTime));

        // subtract a life if score is poor
        checkAccuracyAgainstLives(accuracy);

        // check to see we aren't out of lives
        checkLivesLeft(state.getLivesRemaining());



        // set the text color of the counter based on the score
        if (accuracy >= 99) {
            score+= 100;

            tvCounter.setTextColor(getResources().getColor(R.color.green));
        } else if (accuracy > lifeLossThreshhold && accuracy < 99) {
            tvCounter.setTextColor(getResources().getColor(R.color.orange));
        } else {
            tvCounter.setTextColor(getResources().getColor(R.color.red));
        }
        tvCounter.setText(String.format("%.2f", accelCount));

        // add the score to the ArrayList of scores
        addScore(score);

    }

    private double generateTarget() {
        target = gen.nextInt((maxTarget + 1) - minTarget) + minTarget;
        return target;
    }

    private void displayTarget(double target) {
        tvTarget = (TextView) findViewById(R.id.t_v_target);
        tvTarget.setText(getString(R.string.target) + " " + String.format("%.2f", target));
    }

    private void resetCounter() {
        tvCounter.setText(getString(R.string.zero_point_zero));
        tvCounter.setTextColor(getResources().getColor(R.color.white));
        startIsClickable = true;
    }

    private void resetLives() {
        int numOfLivesPerLevel = state.getNumOfLivesPerLevel();
        state.setLivesRemaining(numOfLivesPerLevel);
        tvLivesRemaining.setText(getString(R.string.lives_remaining) + " " + Integer.toString(numOfLivesPerLevel));
    }

    private void resetScore() {
        for (int i = 0; i < state.getScoreList().size(); i++) {
            if (i == 0) {
                state.getScoreList().set(i, 0);
            }
            else {
                state.getScoreList().remove(i);
            }
            tvScore.setText(getString(R.string.score) + " " + (getString(R.string.zero)));
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
        //for (int score: state.getScoreList()) {
        //    Log.d(DEBUG_TAG, Integer.toString(score));
        //}
        return accuracyI;
    }

    private void checkAccuracyAgainstLives(int accuracy) {
        int lives = state.getLivesRemaining();
        if (accuracy < lifeLossThreshhold) {
            state.setLivesRemaining(lives - 1);
        }
        else if (accuracy >= 99) {
            state.setLivesRemaining(lives +1);
        }
        tvLivesRemaining.setText(getString(R.string.lives_remaining) + " " + state.getLivesRemaining());
    }

    private void addScore(int newScore) {
        state.setRunningScoreTotal(newScore);
        tvScore.setText(getString(R.string.score) + " " + state.getRunningScoreTotal());
    }

    private void checkLivesLeft(int lives) {
        if (lives == 0) {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mDialogFragment = new OutOfLivesDialogFragment();
            mDialogFragment.show(ft, OUT_OF_LIVES_DIALOG);
        }
    }

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
