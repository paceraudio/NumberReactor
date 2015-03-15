package com.paceraudio.numberreactor.app.activities;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.os.Bundle;

import com.paceraudio.numberreactor.app.db.DBHelper;
import com.paceraudio.numberreactor.app.db.QueryAllDbAsync;
import com.paceraudio.numberreactor.app.db.QueryDbListener;
import com.paceraudio.numberreactor.app.util.GameStats;
import com.paceraudio.numberreactor.app.R;

import java.util.ArrayList;


public class ViewStatsActivity extends ListActivity implements QueryDbListener {

    private ViewStatsListAdapter mAdapter;
    private DBHelper mDbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stats);
        mDbHelper = new DBHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        QueryAllDbAsync queryAllDbAsync = new QueryAllDbAsync(this, this);
        queryAllDbAsync.execute();
    }

    @Override
    public ArrayList<GameStats> onAllDbQueried(ArrayList<GameStats> arrayList) {
        mAdapter = new ViewStatsListAdapter(this, arrayList);
        setListAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        return arrayList;
    }
}
