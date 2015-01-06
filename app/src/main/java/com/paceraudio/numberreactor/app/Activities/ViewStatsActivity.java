package com.paceraudio.numberreactor.app.Activities;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.os.Bundle;

import com.paceraudio.numberreactor.app.DB.DBHelper;
import com.paceraudio.numberreactor.app.DB.QueryAllDbAsync;
import com.paceraudio.numberreactor.app.DB.QueryDbListener;
import com.paceraudio.numberreactor.app.Utilities.GameStats;
import com.paceraudio.numberreactor.app.R;

import java.util.ArrayList;


public class ViewStatsActivity extends ListActivity implements QueryDbListener/*,
        ClearAllGameDataDialogFragment.OnFragmentInteractionListener,
        ClearAllGameDataDbAsync.ClearDbListener*/ {
private static final String CLEAR_ALL_GAME_DATA_DF = "clearAllGameDataDialogFragment";

    private ViewStatsListAdapter mAdapter;
    private DBHelper mDbHelper;
    private DialogFragment mDialogFragment;


    //    ArrayList<GameStats> mStatsArrayList;

    //    TODO trying to see if the the QueryDb Async Listener will fire and pass the ArrayList
    // from the DB here.  Good chance there will be 10 null pointer errors

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stats);
        /*ActionBar actionBar = getActionBar();
        actionBar.hide();*/
        mDbHelper = new DBHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        QueryAllDbAsync queryAllDbAsync = new QueryAllDbAsync(this, this);
        queryAllDbAsync.execute();
    }
    // TODO figure out if you want to be able to clear the database of games data.
    /*@Override
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
        if (id == R.id.action_clear_data) {
            android.app.FragmentManager fm = getFragmentManager();
            android.app.FragmentTransaction ft = fm.beginTransaction();
            mDialogFragment = ClearAllGameDataDialogFragment.newInstance();
            mDialogFragment.show(ft, CLEAR_ALL_GAME_DATA_DF);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public ArrayList<GameStats> onAllDbQueried(ArrayList<GameStats> arrayList) {
        mAdapter = new ViewStatsListAdapter(this, arrayList);
        setListAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        return arrayList;
    }

    /*@Override
    public void onYesClicked() {
        mDialogFragment.dismiss();
        ClearAllGameDataDbAsync clearAllGameDataDbAsync = new ClearAllGameDataDbAsync(this,
                mDbHelper, this);
        clearAllGameDataDbAsync.execute();
    }

    @Override
    public void onNoClicked() {
        mDialogFragment.dismiss();
    }

    @Override
    public void onDBCleared() {

    }*/
}
