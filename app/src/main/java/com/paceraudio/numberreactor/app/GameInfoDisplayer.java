package com.paceraudio.numberreactor.app;

import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by jeffwconaway on 12/17/14.
 */
public class GameInfoDisplayer {

    Context mContext;
    ApplicationState mState;

    private static final String DEBUG_TAG = "jwc";


    public GameInfoDisplayer(Context context) {
        this.mContext = context;
        mState = (ApplicationState) mContext.getApplicationContext();
    }

    private void showStartButtonEngaged(Button start) {
        start.setBackgroundColor(mContext.getResources().getColor(R.color.green));
        start.setTextColor(mContext.getResources().getColor(R.color.green));
    }

    private void showStopButtonEngaged(Button stop) {
        stop.setBackgroundColor(mContext.getResources().getColor(R.color.red));
        stop.setTextColor(mContext.getResources().getColor(R.color.red));
    }

    private void showStartButtonNotEngaged(Button start) {
        start.setBackgroundColor(mContext.getResources().getColor(R.color.brown));
        start.setTextColor(mContext.getResources().getColor(R.color.grey));
    }

    private void showStopButtonNotEngaged(Button stop) {
        stop.setBackgroundColor(mContext.getResources().getColor(R.color.brown));
        stop.setTextColor(mContext.getResources().getColor(R.color.grey));
    }

    private void displayTarget(TextView tv) {
        tv.setText(mContext.getString(R.string.target) + " " + String.format("%.2f",
                mState.getTarget()));
    }

    private void displayTurnAccuracy(TextView tv) {
        tv.setText(mContext.getString(R.string.accuracy) + " " + mState.getTurnAccuracy() + "%");
    }

    private void displayOverallAccuracy(TextView tv) {
        tv.setText(mContext.getString(R.string.accuracy) + " " + mState.getOverallAccuracy() + "%");
    }

    private void displayLives(TextView tv) {
        tv.setText(mContext.getString(R.string.lives_remaining) + " " + mState
                .getLives());
    }

    private void displayTurnScore(TextView tv) {
        tv.setText(mContext.getString(R.string.score) + " " + mState.getTurnScore());
    }

    private void displayScore(TextView tv) {
        tv.setText(mContext.getString(R.string.score) + " " + mState.getRunningScoreTotal());
    }

    private void displayLevel(TextView tv) {
        tv.setText(mContext.getString(R.string.level) + " " + mState.getLevel());
    }

    public void displayImmediateGameInfoAfterTurn(TextView accuracy, TextView lives, TextView score) {
        accuracy.setTextColor(mContext.getResources().getColor(R.color.lightBlue));
        lives.setTextColor(mContext.getResources().getColor(R.color.lightBlue));
        score.setTextColor(mContext.getResources().getColor(R.color.lightBlue));
        displayTurnAccuracy(accuracy);
        displayLives(lives);
        displayTurnScore(score);
    }

    public void displayAllGameInfo(TextView target, TextView accuracy, TextView lives, TextView score, TextView level) {
        accuracy.setTextColor(mContext.getResources().getColor(R.color.red));
        lives.setTextColor(mContext.getResources().getColor(R.color.red));
        score.setTextColor(mContext.getResources().getColor(R.color.red));
        displayTarget(target);
//        displayTurnAccuracy(accuracy, mState.getTurnAccuracy());
        displayOverallAccuracy(accuracy);
        displayLives(lives);
        displayScore(score);
        displayLevel(level);
        Log.d(DEBUG_TAG, "displayAllGameInfo ()********" +
                "\n Level: " + mState.getLevel() +
                "\n Turn: " + mState.getTurn() +
                "\n Score: " + mState.getRunningScoreTotal() +
                "\n Target: " + mState.getTarget() +
                "\n Accelerator: " + mState.getAccelerator());
    }
}
