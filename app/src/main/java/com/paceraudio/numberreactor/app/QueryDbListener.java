package com.paceraudio.numberreactor.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeffwconaway on 12/11/14.
 */
public interface QueryDbListener {

    public ArrayList<GameStats> onAllDbQueried(ArrayList<GameStats> arrayList);

    public int onLatestGameDbQueried(int gameNumber);
}
