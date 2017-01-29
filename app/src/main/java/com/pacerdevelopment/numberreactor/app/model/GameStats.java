package com.pacerdevelopment.numberreactor.app.model;

/**
 * Created by jeffwconaway on 12/3/14.
 */
public class GameStats {

    private int gameNumber;
    private String gameDate;
    private int gameLevelReached;
    private int gamePointsScored;

    public GameStats( int gameNum, String gameDate, int gameLevel, int gamePoints) {
        this.gameNumber = gameNum;
        this.gameDate = gameDate;
        this.gameLevelReached = gameLevel;
        this.gamePointsScored = gamePoints;
    }

    public int getGameNumber() {
        return gameNumber;
    }

    public String getGameDate() {
        return gameDate;
    }

    public int getGameLevelReached() {
        return gameLevelReached;
    }

    public int getGamePointsScored() {
        return gamePointsScored;
    }

}
