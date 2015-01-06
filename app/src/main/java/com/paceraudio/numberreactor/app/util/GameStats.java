package com.paceraudio.numberreactor.app.util;

/**
 * Created by jeffwconaway on 12/3/14.
 */
public class GameStats {

    private int mGameNumber;
    private String mGameDate;
    private int mGameLevelReached;
    private int mGamePointsScored;

    public GameStats( int gameNum, String gameDate, int gameLevel, int gamePoints) {
        this.mGameNumber = gameNum;
        this.mGameDate = gameDate;
        this.mGameLevelReached = gameLevel;
        this.mGamePointsScored = gamePoints;
    }

    public int getmGameNumber() {
        return mGameNumber;
    }

    public void setmGameNumber(int mGameNumber) {
        this.mGameNumber = mGameNumber;
    }

    public String getmGameDate() {
        return mGameDate;
    }

    public void setmGameDate(String mGameDate) {
        this.mGameDate = mGameDate;
    }

    public int getmGameLevelReached() {
        return mGameLevelReached;
    }

    public void setmGameLevelReached(int mGameLevelReached) {
        this.mGameLevelReached = mGameLevelReached;
    }

    public int getmGamePointsScored() {
        return mGamePointsScored;
    }

    public void setmGamePointsScored(int mGamePointsScored) {
        this.mGamePointsScored = mGamePointsScored;
    }


}
