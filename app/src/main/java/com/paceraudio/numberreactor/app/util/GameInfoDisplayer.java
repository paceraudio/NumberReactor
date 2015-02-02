package com.paceraudio.numberreactor.app.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.paceraudio.numberreactor.app.application.ApplicationState;
import com.paceraudio.numberreactor.app.R;

/**
 * Created by jeffwconaway on 12/17/14.
 */
public class GameInfoDisplayer {

    Context mContext;
    ApplicationState mState;

    private static final String FROM_COUNTER_ACTIVITY = "fromCounterActivity";
    private static final String FROM_FADE_COUNTER_ACTIVITY = "fromFadeCounterActivity";

    private static final String DEBUG_TAG = "jwc";


    public GameInfoDisplayer(Context context) {
        this.mContext = context;
        mState = (ApplicationState) mContext.getApplicationContext();
    }

    /*public void showStartButtonEngaged(Button start, FrameLayout frameStart) {
        frameStart.setBackgroundColor(mContext.getResources().getColor(R.color.green));
        start.setBackgroundDrawable(new ButtonDrawableView(mContext).mStartTriangleDisengaged);
        start.setTextColor(mContext.getResources().getColor(R.color.green));
    }*/

    public void showStartButtonEngaged(Button start, ShapeDrawable triangle) {
        start.setBackgroundDrawable(triangle);
    }

    public void showStopButtonEngaged(Button stop, FrameLayout frameStop) {
        frameStop.setBackgroundColor(mContext.getResources().getColor(R.color.red));
        stop.setTextColor(mContext.getResources().getColor(R.color.red));
    }

    /*public void showStartButtonDisengaged(Button start, FrameLayout frameStart) {
        frameStart.setBackgroundColor(mContext.getResources().getColor(R.color.brown));
        start.setTextColor(mContext.getResources().getColor(R.color.grey));
    }*/

    public void showStartButtonDisengaged(Button start, ShapeDrawable triangle) {
        start.setBackgroundDrawable(triangle);
    }

    public void showStopButtonNotEngaged(Button stop, FrameLayout frameStop) {
        frameStop.setBackgroundColor(mContext.getResources().getColor(R.color.brown));
        stop.setTextColor(mContext.getResources().getColor(R.color.grey));
    }

    private void displayTarget(TextView tv) {
        tv.setText(mContext.getString(R.string.target) + " " + String.format("%.2f",
                mState.getTarget()));
    }

    private void displayTurnAccuracy(TextView tv) {
        tv.setText(mContext.getString(R.string.accuracy) + " " + mState.getmTurnAccuracy() + "%");
    }

    private void displayOverallAccuracy(TextView tv) {
        tv.setText(mContext.getString(R.string.accuracy) +  " 0%");
    }

    private void displayLives(TextView tv) {
        tv.setText(mContext.getString(R.string.lives_remaining) + " " + mState
                .getLives());
    }

    private void displayTurnPoints(TextView tv) {
        tv.setText(mContext.getString(R.string.points) + " " + mState.getmTurnPoints());
    }

    private void displayScore(TextView tv) {
        tv.setText(mContext.getString(R.string.score) + " " + mState.getmRunningScoreTotal());
    }

    private void displayLevel(TextView tv) {
        tv.setText(mContext.getString(R.string.level) + " " + mState.getLevel());
    }

    public void displayImmediateGameInfoAfterTurn(TextView accuracy) {
        displayTurnAccuracy(accuracy);
//        displayLives(lives);
//        displayTurnPoints(score);
    }

    public void displayImmediateGameInfoAfterFadeCountTurn(TextView accuracy) {
        displayTurnAccuracy(accuracy);
    }

    public void displayAllGameInfo(TextView target, TextView accuracy, TextView lives, TextView score, TextView level, String fromActivity) {
        displayTarget(target);
        displayOverallAccuracy(accuracy);
        if (fromActivity.equals(FROM_COUNTER_ACTIVITY)){
            lives.setTextColor(mContext.getResources().getColor(R.color.red));
            score.setTextColor(mContext.getResources().getColor(R.color.red));
        }
        if (fromActivity.equals(FROM_FADE_COUNTER_ACTIVITY)){
            lives.setTextColor(mContext.getResources().getColor(R.color.lightBlue));
            score.setTextColor(mContext.getResources().getColor(R.color.lightBlue));
        }

        displayLives(lives);
        displayScore(score);
        displayLevel(level);
        Log.d(DEBUG_TAG, "displayAllGameInfo ()********" +
                "\n Level: " + mState.getLevel() +
                "\n Turn: " + mState.getmTurn() +
                "\n Lives: " + mState.getLives() +
                "\n Score: " + mState.getmRunningScoreTotal() +
                "\n Target: " + mState.getTarget() +
                "\n Duration: " + mState.getDuration());
    }

    public void resetCounterToZero(TextView counter) {
        counter.setText(mContext.getString(R.string.zero_point_zero));
        counter.setTextColor(mContext.getResources().getColor(R.color.red));
    }
}
