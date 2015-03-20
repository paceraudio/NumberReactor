package com.pacerdevelopment.numberreactor.app.db;

import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by jeffwconaway on 3/14/15.
 */
public class InsertNewGameRowInDbAsync extends AsyncTask<Void, Void, Void>{

    Context mContext;
    InsertNewGameRowListener mListener;
    DBHelper mDbHelper;

    public InsertNewGameRowInDbAsync(Context context, InsertNewGameRowListener listener) {
        this.mContext = context;
        this.mDbHelper = new DBHelper(context);
        this.mListener = listener;
    }
    @Override
    protected Void doInBackground(Void... params) {
        mDbHelper.insertNewGameRowInDb();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mListener.onNewRowInsertedInDb();
    }
}
