package com.paceraudio.numberreactor.app;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import static com.paceraudio.numberreactor.app.CounterActivity.DEBUG_TAG;

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
        Log.d(DEBUG_TAG, "onPostExecute updated level: " + Integer.toString(mDbHelper.queryLevelFromDb()));
        QueryAllDbAsync queryAllDbAsync = new QueryAllDbAsync(mContext);
        queryAllDbAsync.execute();
    }
}
