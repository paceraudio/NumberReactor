package com.paceraudio.numberreactor.app.db;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by jeffwconaway on 12/2/14.
 */
public class UpdateScoreDbAsync extends AsyncTask<Integer, Void, Void>{

    DBHelper mDbHelper;
    Context mContext;
    UpdateDbListener mListener;

    public UpdateScoreDbAsync(Context context, UpdateDbListener listener) {
        this.mContext = context;
        this.mDbHelper = new DBHelper(context);
        this.mListener = listener;
    }

    // pass in the updated score from counter activity as the first param
    @Override
    protected Void doInBackground(Integer ...params) {
        int score = params[0];
        mDbHelper.updateScoreDB(score);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        mListener.onDbScoreUpdatedEndOfTurn();
    }


}
