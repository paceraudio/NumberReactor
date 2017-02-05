package com.pacerdevelopment.numberreactor.app.model;

import com.pacerdevelopment.numberreactor.app.model.shared_prefs.NRPrefs;

/**
 * Created by jeffwconaway on 2/5/17.
 */

public class Model implements ModelContract{

    private GameState gameState;
    private NRPrefs nrPrefs;
    private GameStats gameStats;

    public Model() {
        gameState = new GameState();
    }

    public GameState getGameState() {
        return gameState;
    }

    public GameStats getGameStats() {
        return gameStats;
    }

    @Override
    public void initGameStats() {
        gameState.initGameStats();
    }

    @Override
    public boolean checkPrefsForDbNotNull() {
        return nrPrefs.checkSharedPrefsForDbNotNull();
    }

    @Override
    public double roundElapsedCount(long elapsedCount, double counterCeiling) {
        return gameState.roundElapsedCount(elapsedCount, counterCeiling);
    }
}
