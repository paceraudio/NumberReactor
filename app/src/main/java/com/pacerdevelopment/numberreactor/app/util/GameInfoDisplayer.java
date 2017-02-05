package com.pacerdevelopment.numberreactor.app.util;

import android.content.Context;
import android.graphics.drawable.LayerDrawable;
import android.widget.Button;
import android.widget.TextView;

import com.pacerdevelopment.numberreactor.app.R;
import com.pacerdevelopment.numberreactor.app.application.NrApp;
import com.pacerdevelopment.numberreactor.app.model.GameState;

/**
 * Created by jeffwconaway on 12/17/14.
 */
public class GameInfoDisplayer {

    Context context;
    //NrApp mState;
    GameState gameState;

    private static final String SPACE = " ";
    private static final String PERCENT = "%";
    private static final String ZERO = "0";
    private static final String DOUBLE_FORMAT = "%.2f";

    private static final String FROM_COUNTER_ACTIVITY = "fromCounterActivity";
    private static final String FROM_FADE_COUNTER_ACTIVITY = "fromFadeCounterActivity";


    public GameInfoDisplayer(Context context) {
        this.context = context;
        gameState = NrApp.getModel();
        //mState = (NrApp) context.getApplicationContext();
    }

    public void showButtonState(Button button, LayerDrawable layerDrawable) {
        button.setBackground(layerDrawable);
    }

    private void displayTarget(TextView tv) {
        tv.setText(context.getString(R.string.target) + " " + String.format(DOUBLE_FORMAT,
                gameState.getBaseTarget()));
    }

    private void displayTurnAccuracy(TextView tv) {
        tv.setText(context.getString(R.string.accuracy) + SPACE + gameState.getTurnAccuracy() + PERCENT);
    }

    private void displayWeightedAccuracy(TextView tv) {
        tv.setText(context.getString(R.string.accuracy) + SPACE + gameState.getWeightedAccuracy() + PERCENT);
    }

    private void displayOverallAccuracy(TextView tv) {
        tv.setText(context.getString(R.string.accuracy) +  SPACE + ZERO + PERCENT);
    }

    private void displayLives(TextView tv) {
        tv.setText(context.getString(R.string.lives_remaining) + SPACE + gameState.getLives());
    }

    private void displayScore(TextView tv) {
        tv.setText(context.getString(R.string.score) + SPACE + gameState.getRunningScoreTotal());
    }

    private void displayLevel(TextView tv) {
        tv.setText(context.getString(R.string.level) + SPACE + gameState.getLevel());
    }

    public void displayImmediateGameInfoAfterTurn(TextView accuracy) {
        displayWeightedAccuracy(accuracy);
    }

    public void displayImmediateGameInfoAfterFadeCountTurn(TextView accuracy) {
        displayTurnAccuracy(accuracy);
    }

    public void displayAllGameInfo(TextView target, TextView accuracy, TextView lives, TextView score, TextView level, String fromActivity) {
        displayTarget(target);
        displayOverallAccuracy(accuracy);
        if (fromActivity.equals(FROM_COUNTER_ACTIVITY)){
            lives.setTextColor(context.getResources().getColor(R.color.red));
            score.setTextColor(context.getResources().getColor(R.color.red));
        }
        if (fromActivity.equals(FROM_FADE_COUNTER_ACTIVITY)){
            lives.setTextColor(context.getResources().getColor(R.color.lightBlue));
            score.setTextColor(context.getResources().getColor(R.color.lightBlue));
        }
        displayLives(lives);
        displayScore(score);
        displayLevel(level);
    }

    public void resetCounterToZero(TextView counter) {
        counter.setText(context.getString(R.string.zero_point_zero));
        counter.setTextColor(context.getResources().getColor(R.color.red));
    }
}
