package com.pacerdevelopment.numberreactor.app.model.db;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by jeffwconaway on 12/3/14.
 */
public class UpdateLevelDbAsync extends AsyncTask<Integer, Void, Void> {

    Context mContext;
    DBHelper mDbHelper;

    public UpdateLevelDbAsync(Context context) {
        this.mContext = context;
        mDbHelper = new DBHelper(context);
    }
    @Override
    protected Void doInBackground(Integer... integers) {
        int level = integers[0];
        mDbHelper.updateLevelReached(level);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
    }
}
