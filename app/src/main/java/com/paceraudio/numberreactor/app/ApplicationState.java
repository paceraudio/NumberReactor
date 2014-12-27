package com.paceraudio.numberreactor.app;

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

    private int turn;
    private double lastTarget;
    private double target;
    private int overallAccuracy;
    private int turnAccuracy;
    private int lives;
    private int turnPoints;
    private int runningScoreTotal;
    private int level;
    private double accelerator;

    private List<Integer> scoreList;
    private List<Integer> accuracyList;

    private static final int BEGINNING_NUMBER_OF_LIVES = 4;
    private static final int LIFE_LOSS_THRESHOLD = 90;

    private static final String DEBUG_TAG = "jwc";

    @Override
    public void onCreate() {
        level = 1;
        runningScoreTotal = 0;
        scoreList = new ArrayList<Integer>();
        accuracyList = new ArrayList<Integer>();
        lives = BEGINNING_NUMBER_OF_LIVES;
//        To be set by the Activities when they begin.  We need ApplicationState to keep track of it
//        in order to consolidate the code for updating the displayed game values in each Activity
        target = 0;
        turnAccuracy = 0;
        turnPoints = 0;
        accelerator = 0;
        turn = 0;
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

    public double getTarget() {
        return target;
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public double getAccelerator() {
        return accelerator;
    }

    public void setAccelerator(double accelerator) {
        this.accelerator = accelerator;
    }

    public int getTurnAccuracy() {
        return turnAccuracy;
    }

    public void setTurnAccuracy(int turnAccuracy) {
        this.turnAccuracy = turnAccuracy;
    }

    public int getOverallAccuracy() {return overallAccuracy; }

    public void setOverallAccuracy(int accuracy) {
        accuracyList.add(accuracy);
        int listSize = accuracyList.size();
        int accuracySum = 0;
        for (int i = 0; i < listSize; i++) {
            int accuracyTurnRating = accuracyList.get(i);
            accuracySum += accuracyTurnRating;
        }
        double overallAccuracyDouble = accuracySum / listSize;
        overallAccuracy = (int) Math.round(overallAccuracyDouble);
    }
    public int getRunningScoreTotal() {
        return runningScoreTotal;
    }

    public double getLastTarget() {
        return lastTarget;
    }

    public void setLastTarget(double lastTarget) {
        this.lastTarget = lastTarget;
    }

    public int getTurnPoints() {
        return turnPoints;
    }

    public void setTurnPoints(int turnPoints) {
        this.turnPoints = turnPoints;
    }

    public void setRunningScoreTotal(int newScore) {
        scoreList.add(newScore);
        runningScoreTotal += newScore;
//        runningScoreTotal = 0;
//        for(int i = 0; i < scoreList.size(); i++) {
//            runningScoreTotal += scoreList.get(i);
//        }
    }

    public void resetScoreForNewGame() {
        scoreList.clear();
        runningScoreTotal = 0;
    }

    public void resetLivesForNewGame() {
        lives = BEGINNING_NUMBER_OF_LIVES;
    }

    public String setGameDate() {
        //TODO see if this is the best place for this?
        Calendar c = Calendar.getInstance();
        SimpleDateFormat gameDate = new SimpleDateFormat("dd-MMM-yyyy");
        return gameDate.format(c.getTime());
    }

    public List<Integer> getScoreList() {
        return scoreList;
    }

    public void setScoreList(List<Integer> list) {
        list = scoreList;
    }


//    Methods for calculating the game values stored in this class
    public double roundElapAccelCount(double accelCount) {
        if (accelCount > 99.99) {
            return 99.99;
        }
        return ((int) (accelCount * 100)) /100d;
    }
    public int calcAccuracy(double target, double elapAccelCount) {
//        double counterToHundredths = ((int) (counter * 100)) / 100d;
        double error = Math.abs(target - elapAccelCount);
        double accuracy = ((target - error) / target) * 100;
        int accuracyInt = (int) Math.round(accuracy);
        if (accuracyInt < 0) {
            accuracyInt = 0;
        }
        Log.d(DEBUG_TAG, "calcAccuracy()return accuracy: " + accuracy);
        return accuracyInt;
    }

    public void checkAccuracyAgainstLives() {
        if (turnAccuracy < LIFE_LOSS_THRESHOLD) {
            lives -= 1;
            Log.d(DEBUG_TAG, "should lose life: " + Boolean.toString(turnAccuracy <
                    LIFE_LOSS_THRESHOLD) + " lives remaining: " + lives);
        }
        else if (turnAccuracy == 100) {
            lives += 1;
        }

        Log.d(DEBUG_TAG, "checkAccuracyAgainstLives lives remaining: " + lives);
    }

    public boolean isLifeLost() {
        if (turnAccuracy < LIFE_LOSS_THRESHOLD) {
            lives -= 1;
            return true;
        }
        return false;
    }

   public boolean isLifeGained() {
       if (turnAccuracy == 100) {
           lives += 1;
           return true;
       }
       return false;
   }

    public int calcAccuracyResult(double accuracy) {
        return (int) Math.round(accuracy);
    }

    public int calcScore(int accuracy) {
        int score = 0;
//        int margin = 80;
        int scoreToCalc = accuracy - LIFE_LOSS_THRESHOLD;
        if (scoreToCalc > 0) {
            score = scoreToCalc;
        }
        return score;
    }
}
