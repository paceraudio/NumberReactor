package com.pacerdevelopment.numberreactor.app.db;

import com.pacerdevelopment.numberreactor.app.util.GameStats;

import java.util.ArrayList;

/**
 * Created by jeffwconaway on 12/11/14.
 */
public interface QueryDbListener {

    public ArrayList<GameStats> onAllDbQueried(ArrayList<GameStats> arrayList);

}
