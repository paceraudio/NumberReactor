package com.paceraudio.numberreactor.app;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;


public class ViewStatsActivity extends ListActivity implements QueryDbListener{

    ViewStatsListAdapter mAdapter;
    DBHelper mDbHelper;
//    ArrayList<GameStats> mStatsArrayList;


//    TODO trying to see if the the QueryDb Async Listener will fire and pass the ArrayList from the DB here.  Good chance there will be 10 null pointer errors

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stats);
       // mDbHelper = new DBHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        QueryAllDbAsync queryAllDbAsync = new QueryAllDbAsync(this, this);
        queryAllDbAsync.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public ArrayList<GameStats> onAllDbQueried(ArrayList<GameStats> arrayList) {
        mAdapter = new ViewStatsListAdapter(this, arrayList);
        setListAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        return arrayList;
    }

    @Override
    public int onLatestGameDbQueried(int gameNumber) {
        return 0;
    }
}
