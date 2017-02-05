package com.pacerdevelopment.numberreactor.app.model;

/**
 * Created by jeffwconaway on 2/5/17.
 */

public interface ModelContract {

    void initGameStats();

    boolean checkPrefsForDbNotNull();


    double roundElapsedCount(long elapsedCount, double counterCeiling);
}
