package com.pacerdevelopment.numberreactor.app.application;

/**
 * Created by jeffwconaway on 9/26/14.
 */

import android.app.Application;
import android.content.Context;

import com.pacerdevelopment.numberreactor.app.model.GameState;

public class NrApp extends Application{

    private static Context context;

    public static GameState gameState;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        gameState = new GameState();
        gameState.initGameStats();
    }

    public static Context getAppContext() {
        return context;
    }

    public static GameState getGameState() {
        return gameState;
    }
}
