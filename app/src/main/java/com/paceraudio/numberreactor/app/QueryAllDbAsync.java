package com.paceraudio.numberreactor.app;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeffwconaway on 12/3/14.
 */
public class QueryAllDbAsync extends AsyncTask<Void, Void, ArrayList<GameStats>> {

    Context mContext;
    DBHelper mDbHelper;
    QueryDbListener mListener;

    public QueryAllDbAsync(Context context, QueryDbListener listener) {
        this.mContext = context;
        this.mDbHelper = new DBHelper(context);
        this.mListener = listener;
    }

    @Override
    protected ArrayList<GameStats> doInBackground(Void... voids) {
        return mDbHelper.queryAllFromDb();
    }

    @Override
    protected void onPostExecute(ArrayList<GameStats> list) {
        super.onPostExecute(list);
        mListener.onAllDbQueried(list);
    }
}
