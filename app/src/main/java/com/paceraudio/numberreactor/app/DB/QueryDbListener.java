package com.paceraudio.numberreactor.app.DB;

import com.paceraudio.numberreactor.app.Utility.GameStats;

import java.util.ArrayList;

/**
 * Created by jeffwconaway on 12/11/14.
 */
public interface QueryDbListener {

    public ArrayList<GameStats> onAllDbQueried(ArrayList<GameStats> arrayList);

}
