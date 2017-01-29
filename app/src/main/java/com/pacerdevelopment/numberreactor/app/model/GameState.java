package com.pacerdevelopment.numberreactor.app.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by jeffwconaway on 1/29/17.
 */

public class GameState {

    private int turn;
    private double baseTarget;
    private double turnTarget;

    private int turnAccuracy;

    private int weightedAccuracy;
    private int lives;
    private int turnPoints;
    private int runningScoreTotal;
    private int level;
    private double duration;
    private int difficulty;

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

    public void initGameStats() {
        level = ONE;
        runningScoreTotal = ZERO;
        lives = BEGINNING_NUMBER_OF_LIVES;
        baseTarget = BEGINNING_TARGET_LEVEL_ONE;
        turnAccuracy = ZERO;
        turnPoints = ZERO;
        turn = ONE;
    }

    public void resetGameStatsForNewGame() {
        level = ONE;
        runningScoreTotal = ZERO;
        lives = BEGINNING_NUMBER_OF_LIVES;
        baseTarget = BEGINNING_TARGET_LEVEL_ONE;
        turnAccuracy = ZERO;
        turnPoints = ZERO;
        turn = ONE;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int l) {
        level = l;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public double getBaseTarget() {
        return baseTarget;
    }

    public void setBaseTarget(double target) {
        this.baseTarget = target;
    }

    public void setTurnTarget(double mTurnTarget) {
        this.turnTarget = mTurnTarget;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public int getTurnAccuracy() {
        return turnAccuracy;
    }

    public void setTurnAccuracy(int turnAccuracy) {
        this.turnAccuracy = turnAccuracy;
    }

    public int getWeightedAccuracy() {
        return weightedAccuracy;
    }

    public void setWeightedAccuracy(int weightedAccuracy) {
        this.weightedAccuracy = weightedAccuracy;
    }

    public int getRunningScoreTotal() {
        return runningScoreTotal;
    }

    public int getTurnPoints() {
        return turnPoints;
    }

    public void setTurnPoints(int turnPoints) {
        this.turnPoints = turnPoints;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isFirstTurnInNewGame() {
        return firstTurnInNewGame;
    }

    public void setFirstTurnInNewGame(boolean firstTurnInNewGame) {
        firstTurnInNewGame = firstTurnInNewGame;
    }

    public double randomizeTarget(double baseTarget) {
        Random random = new Random();
        return random.nextInt(RANDOM_TARGET_THRESHOLD) + baseTarget;
    }

    public void updateRunningScoreTotal(int newScore) {
        runningScoreTotal += newScore;
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
        lives += livesGained;
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
