package com.paceraudio.numberreactor.app;

/**
 * Created by jeffwconaway on 9/26/14.
 */

import android.app.Application;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ApplicationState extends Application{

    private String formattedDate;
    private int level;
    private int runningScoreTotal;
    private List<Integer> scoreList;
    private int gameNumber;
    private double lastTarget;



    private final int NUM_OF_LIVES_PER_LEVEL = 4;
    private int livesRemaining;


    @Override
    public void onCreate() {
        level = 1;
        runningScoreTotal = 0;
        scoreList = new ArrayList<Integer>();
        livesRemaining = NUM_OF_LIVES_PER_LEVEL;
        gameNumber = 0;

    }

    public int getNumOfLivesPerLevel() {
        return NUM_OF_LIVES_PER_LEVEL;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int l) {
        level = l;
    }

    public List<Integer> getScoreList() {
        return scoreList;
    }

    public void setScoreList(List<Integer> list) {
        list = scoreList;
    }


    public int getLivesRemaining() {
        return livesRemaining;
    }

    public void setLivesRemaining(int lives) {
        livesRemaining = lives;
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

    public void setRunningScoreTotal(int newScore) {
        scoreList.add(newScore);
        runningScoreTotal = 0;
        for(int i = 0; i < scoreList.size(); i++) {
            runningScoreTotal += scoreList.get(i);
        }
    }

    public String setGameDate() {
        //TODO see if this is the best place for this?
        Calendar c = Calendar.getInstance();
        SimpleDateFormat gameDate = new SimpleDateFormat("dd-MMM-yyyy");
        formattedDate = gameDate.format(c.getTime());
        return formattedDate;
    }
}
