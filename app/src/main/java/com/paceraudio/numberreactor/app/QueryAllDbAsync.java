package com.paceraudio.numberreactor.app;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by jeffwconaway on 12/3/14.
 */
public class QueryAllDbAsync extends AsyncTask<Void, Void, Void> {

    Context mContext;
    DBHelper mDbHelper;

    public QueryAllDbAsync(Context context) {
        this.mContext = context;
        mDbHelper = new DBHelper(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        mDbHelper.queryAllFromDb();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
