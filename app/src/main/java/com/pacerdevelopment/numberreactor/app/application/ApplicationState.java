package com.pacerdevelopment.numberreactor.app.application;

/**
 * Created by jeffwconaway on 9/26/14.
 */

import android.app.Application;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class ApplicationState extends Application{

    private static Context context;

    private int mTurn;
    private double mBaseTarget;
    private double mTurnTarget;

    private int mTurnAccuracy;

    private int mWeightedAccuracy;
    private int mLives;
    private int mTurnPoints;
    private int mRunningScoreTotal;
    private int mLevel;
    private double mDuration;
    private int mDifficulty;

    private static boolean firstTurnInNewGame = true;

    private static final int BEGINNING_NUMBER_OF_LIVES = 4;
    private static final int BEGINNING_TARGET_LEVEL_ONE = 2;
    private static final int LIFE_LOSS_THRESHOLD = 80;

    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int ONE_HUNDRED = 100;
    private static final int PLUS_TWO_LIVES = 2;
    private static final int PLUS_ONE_LIFE = 1;
    private static final int MINUS_ONE_LIFE = -1;
    private static final int PLUS_ONE_LIVE_THRESHOLD = 98;

    private static final int RANDOM_TARGET_THRESHOLD = 3;

    private static final double MILLIS_IN_SECONDS = 1000.0;
    private static final int DEC_TO_WHOLE_PERCENTAGE = 100;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        initGameStats();
    }

    public static Context getAppContext() {
        return ApplicationState.context;
    }

    private void initGameStats() {
        mLevel = ONE;
        mRunningScoreTotal = ZERO;
        mLives = BEGINNING_NUMBER_OF_LIVES;
        mBaseTarget = BEGINNING_TARGET_LEVEL_ONE;
        mTurnAccuracy = ZERO;
        mTurnPoints = ZERO;
        mTurn = ONE;
    }

    public void resetGameStatsForNewGame() {
        mLevel = ONE;
        mRunningScoreTotal = ZERO;
        mLives = BEGINNING_NUMBER_OF_LIVES;
        mBaseTarget = BEGINNING_TARGET_LEVEL_ONE;
        mTurnAccuracy = ZERO;
        mTurnPoints = ZERO;
        mTurn = ONE;
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

    public double getBaseTarget() {
        return mBaseTarget;
    }

    public void setBaseTarget(double target) {
        this.mBaseTarget = target;
    }

    public void setTurnTarget(double mTurnTarget) {
        this.mTurnTarget = mTurnTarget;
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

    public void setmDifficulty(int mDifficulty) {
        this.mDifficulty = mDifficulty;
    }

    public boolean isFirstTurnInNewGame() {
        return firstTurnInNewGame;
    }

    public void setFirstTurnInNewGame(boolean firstTurnInNewGame) {
        ApplicationState.firstTurnInNewGame = firstTurnInNewGame;
    }

    public double randomizeTarget(double baseTarget) {
        Random random = new Random();
        return random.nextInt(RANDOM_TARGET_THRESHOLD) + baseTarget;
    }

    public void updateRunningScoreTotal(int newScore) {
        mRunningScoreTotal += newScore;
    }

    public String obtainGameDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat gameDate = new SimpleDateFormat("dd-MMM-yyyy");
        return gameDate.format(c.getTime());
    }

//    Methods for calculating the game values stored in this class

    public double roundElapsedCount(long elapsedCount, double counterCeiling) {
        if (elapsedCount / MILLIS_IN_SECONDS >= counterCeiling) {
            return counterCeiling;
        } else {
            return elapsedCount / MILLIS_IN_SECONDS;
        }
    }

    public int calcAccuracy(double target, double elapsedCount) {
        double error = Math.abs(target - elapsedCount);
        double accuracy = ((target - error) / target) * DEC_TO_WHOLE_PERCENTAGE;
        int accuracyInt = (int) accuracy;
        if (accuracyInt < ZERO) {
            accuracyInt = ZERO;
        }
        return accuracyInt;
    }

    public int calcWeightedAccuracy(double target, double elapsedCount) {
        if (target > BEGINNING_TARGET_LEVEL_ONE) {
            double notCalculated = target - BEGINNING_TARGET_LEVEL_ONE;
            target -= notCalculated;
            elapsedCount -= notCalculated;
        }
        double error = Math.abs(target - elapsedCount);
        double accuracy = ((target - error) / target) * DEC_TO_WHOLE_PERCENTAGE;
        int accuracyInt = (int) accuracy;
        if (accuracyInt < ZERO) {
            accuracyInt = ZERO;
        }
        return accuracyInt;
    }


    public int numOfLivesGainedOrLost(int accuracy, boolean lifeLossPossible) {
        int livesGained = ZERO;
        if (accuracy == ONE_HUNDRED) {
            livesGained = PLUS_TWO_LIVES;
        }
        else if (accuracy > PLUS_ONE_LIVE_THRESHOLD) {
            livesGained = PLUS_ONE_LIFE;
        }
        else if (accuracy <= LIFE_LOSS_THRESHOLD && lifeLossPossible) {
            livesGained = MINUS_ONE_LIFE;
        }
        mLives += livesGained;
        return livesGained;
    }

    public int calcScore(int accuracy) {
        int score = ZERO;
        int scoreToCalc = accuracy - LIFE_LOSS_THRESHOLD;
        if (scoreToCalc > ZERO) {
            score = scoreToCalc;
        }
        return score;
    }
}
