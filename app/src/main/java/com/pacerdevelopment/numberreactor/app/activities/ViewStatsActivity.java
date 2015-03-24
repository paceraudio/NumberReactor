package com.pacerdevelopment.numberreactor.app.activities;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.pacerdevelopment.numberreactor.app.R;
import com.pacerdevelopment.numberreactor.app.db.DBHelper;
import com.pacerdevelopment.numberreactor.app.db.QueryAllDbAsync;
import com.pacerdevelopment.numberreactor.app.db.QueryDbListener;
import com.pacerdevelopment.numberreactor.app.util.GameStats;

import java.util.ArrayList;

import static com.pacerdevelopment.numberreactor.app.activities.CounterActivity
        .FROM_COUNTER_ACTIVITY_FLAG;
import static com.pacerdevelopment.numberreactor.app.activities.FadeOutCounterActivity
        .FROM_FADE_COUNTER_ACTIVITY_FLAG;


public class ViewStatsActivity extends ListActivity implements QueryDbListener {

    private ViewStatsListAdapter mAdapter;
    private DBHelper mDbHelper;
    private Intent mIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stats);
        mDbHelper = new DBHelper(this);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mIntent = getIntent();
    }

   /* @Nullable
    @Override
    public Intent getParentActivityIntent() {
        Intent intent = new Intent();
        if (mIntent.getFlags() == FROM_COUNTER_ACTIVITY_FLAG) {
            intent = new Intent(this, CounterActivity.class);
        } else if (mIntent.getFlags() == FROM_FADE_COUNTER_ACTIVITY_FLAG){
            intent = new Intent(this, FadeOutCounterActivity.class);
        }
        return intent;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return(super.onOptionsItemSelected(item));
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
