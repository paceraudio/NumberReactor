package com.paceraudio.numberreactor.app.application;

/**
 * Created by jeffwconaway on 9/26/14.
 */

import android.app.Application;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ApplicationState extends Application{

    private int mTurn;
    private double mTarget;
    private int mTurnAccuracy;


    private int mWeightedAccuracy;
    private int mLives;
    private int mTurnPoints;
    private int mRunningScoreTotal;
    private int mLevel;
    private double mDuration;

    private List<Integer> scoreList;
    private List<Integer> accuracyList;

    private static final int BEGINNING_NUMBER_OF_LIVES = 4;
    private static final int LIFE_LOSS_THRESHOLD = 80;

    private static final int MAX_ACCEL_COUNT_VALUE = 99999;
    private static final double MAX_ACCEL_COUNT_DISPLAYED = 99.99;
    private static final double MILLIS_IN_SECONDS = 1000.0;
    private static final int DEC_TO_WHOLE_PERCENTAGE = 100;

    private static final String FROM_FADE_COUNTER_ACTIVITY = "fromFadeCounterActivity";
    private static final String FROM_COUNTER_ACTIVITY = "fromCounterActivity";

    private static final String DEBUG_TAG = "jwc";

    @Override
    public void onCreate() {
        mLevel = 1;
        mRunningScoreTotal = 0;
        scoreList = new ArrayList<Integer>();
        accuracyList = new ArrayList<Integer>();
        mLives = BEGINNING_NUMBER_OF_LIVES;
//        To be set by the Activities when they begin.  We need ApplicationState to keep track of it
//        in order to consolidate the code for updating the displayed game values in each Activity
        mTarget = 0;
        mTurnAccuracy = 0;
        mTurnPoints = 0;
        mTurn = 1;
    }

    public int getLevel() {
        return mLevel;
    }

    public void setLevel(int l) {
        mLevel = l;
    }

    public int getLives() {
        return mLives;
    }

    public void setLives(int lives) {
        this.mLives = lives;
    }

    public double getTarget() {
        return mTarget;
    }

    public void setTarget(double target) {
        this.mTarget = target;
    }

    public int getmTurn() {
        return mTurn;
    }

    public void setmTurn(int mTurn) {
        this.mTurn = mTurn;
    }

    public double getDuration() {
        return mDuration;
    }

    public void setDuration(double duration) {
        this.mDuration = duration;
    }

    public int getmTurnAccuracy() {
        return mTurnAccuracy;
    }

    public void setmTurnAccuracy(int mTurnAccuracy) {
        this.mTurnAccuracy = mTurnAccuracy;
    }

    public int getmWeightedAccuracy() {
        return mWeightedAccuracy;
    }

    public void setmWeightedAccuracy(int mWeightedAccuracy) {
        this.mWeightedAccuracy = mWeightedAccuracy;
    }


    public int getmRunningScoreTotal() {
        return mRunningScoreTotal;
    }

    public int getmTurnPoints() {
        return mTurnPoints;
    }

    public void setmTurnPoints(int mTurnPoints) {
        this.mTurnPoints = mTurnPoints;
    }

    public void updateRunningScoreTotal(int newScore) {
        scoreList.add(newScore);
        mRunningScoreTotal += newScore;
    }

    public void resetScoreForNewGame() {
        scoreList.clear();
        mRunningScoreTotal = 0;
    }

    public void resetLivesForNewGame() {
        mLives = BEGINNING_NUMBER_OF_LIVES;
    }

    public String obtainGameDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat gameDate = new SimpleDateFormat("dd-MMM-yyyy");
        return gameDate.format(c.getTime());
    }

//    Methods for calculating the game values stored in this class

    public double roundElapsedCountLong(long accelCount, String fromActivity, double fadeCountCeiling) {

        Log.d("jwc", "mState roundElapsedCountLong incoming param: " + accelCount);
        Log.d("jwc", "mState roundElapsedCountLon rounded count: " + accelCount / 1000d);

        if (accelCount >= MAX_ACCEL_COUNT_VALUE && fromActivity.equals(FROM_COUNTER_ACTIVITY)) {
            return MAX_ACCEL_COUNT_DISPLAYED;
        }
        if (accelCount >= fadeCountCeiling && fromActivity.equals(FROM_FADE_COUNTER_ACTIVITY)) {
            return fadeCountCeiling;
        }

        return accelCount / MILLIS_IN_SECONDS;
    }

    public int calcAccuracy(double target, double elapsedCount) {
        double error = Math.abs(target - elapsedCount);
        double accuracy = ((target - error) / target) * DEC_TO_WHOLE_PERCENTAGE;
        int accuracyInt = (int) accuracy;
        if (accuracyInt < 0) {
            accuracyInt = 0;
        }
        Log.d(DEBUG_TAG, "calcAccuracy()return accuracy: " + accuracy);
        return accuracyInt;
    }

    public int calcWeightedAccuracy(double target, double elapsedCount) {
        if (target > 2) {
            double notCalculated = target - 2;
            target -= notCalculated;
            elapsedCount -= notCalculated;
        }
        double error = Math.abs(target - elapsedCount);
        double accuracy = ((target - error) / target) * DEC_TO_WHOLE_PERCENTAGE;
        int accuracyInt = (int) accuracy;
        if (accuracyInt < 0) {
            accuracyInt = 0;
        }
        Log.d(DEBUG_TAG, "calcWeightedAccuracy()return accuracy: " + accuracy);
        return accuracyInt;
    }



   public int numOfLivesGainedOrLost() {
       int livesGained = 0;
       /*if (mTurnAccuracy == 100) {
           livesGained = 2;
       }
       else if (mTurnAccuracy > 98) {
           livesGained = 1;
       }
       else if (mTurnAccuracy <= LIFE_LOSS_THRESHOLD) {
           livesGained = -1;
       }*/
       if (mWeightedAccuracy == 100) {
           livesGained = 2;
       }
       else if (mWeightedAccuracy > 98) {
           livesGained = 1;
       }
       else if (mWeightedAccuracy <= LIFE_LOSS_THRESHOLD) {
           livesGained = -1;
       }
       mLives += livesGained;
       return livesGained;
   }

    public int numOfBonusLivesFadeCount() {
        int livesGained = 0;
        if (mTurnAccuracy == 100) {
           livesGained = 2;
        }
        else if (mTurnAccuracy > 98) {
            livesGained = 1;
        }
        mLives += livesGained;
        return livesGained;
    }

    public int calcScore(int accuracy) {
        int score = 0;
        int scoreToCalc = accuracy - LIFE_LOSS_THRESHOLD;
        if (scoreToCalc > 0) {
            score = scoreToCalc;
        }
        return score;
    }
}
