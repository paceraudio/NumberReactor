package com.pacerdevelopment.numberreactor.app.vp_stats;

import android.app.ActionBar;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.MenuItem;
import com.pacerdevelopment.numberreactor.app.R;
import com.pacerdevelopment.numberreactor.app.db.QueryAllDbAsync;
import com.pacerdevelopment.numberreactor.app.db.QueryDbListener;
import com.pacerdevelopment.numberreactor.app.util.GameStats;

import java.util.ArrayList;


public class ViewStatsActivity extends ListActivity implements QueryDbListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stats);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

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

    public ArrayList<GameStats> onAllDbQueried(ArrayList<GameStats> arrayList) {
        ViewStatsListAdapter adapter = new ViewStatsListAdapter(this, arrayList);
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
        return arrayList;
    }
}
