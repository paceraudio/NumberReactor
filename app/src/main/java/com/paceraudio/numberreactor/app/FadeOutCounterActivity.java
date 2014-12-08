package com.paceraudio.numberreactor.app;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class FadeOutCounterActivity extends FragmentActivity implements FadeCounterListener, FadeCountDialogFragment.OnFragmentInteractionListener {

    static final String DEBUG_TAG = "jwc";

    private TextView tvFadeCounter;
    private TextView tvFadeTarget;
    private TextView tvFadeAccuracy;

    private long startTime;
    private long target = 10;

    private static final String FADE_COUNT_DIALOG_FRAGMENT = "customFadeCountDialogFragment";

    DialogFragment mDialogFragment;
    FadeOutCounterAsync mFadeOutCounterAsync;
    DBHelper mDbHelper;
    ApplicationState mState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fade_out_counter);
        mState = (ApplicationState) getApplication();
        int level = mState.getLevel();
        mState.setLevel(level + 1);
        mDbHelper = new DBHelper(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        UpdateLevelDbAsync updateLevelDbAsync = new UpdateLevelDbAsync(this);
        updateLevelDbAsync.execute(mState.getLevel());

    }

    @Override
    protected void onResume() {
        super.onResume();

        mState = (ApplicationState) getApplicationContext();
        // TODO dialog shows up after activity is on pause.  Move to onCreate?
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mDialogFragment = new FadeCountDialogFragment();
        mDialogFragment.show(ft, FADE_COUNT_DIALOG_FRAGMENT);

        tvFadeTarget = (TextView) findViewById(R.id.t_v_fade_target);
        tvFadeAccuracy = (TextView) findViewById(R.id.t_v_accuracy_rating);
        tvFadeCounter = (TextView) findViewById(R.id.t_v_fade_counter);

        Button startFadeButton = (Button) findViewById(R.id.b_fade_start);
        startFadeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    startTime = System.currentTimeMillis();
                    mFadeOutCounterAsync = new FadeOutCounterAsync(FadeOutCounterActivity.this, FadeOutCounterActivity.this);
                    mFadeOutCounterAsync.execute(startTime, target);
                }
                return false;
            }
        });
        Button stopFadeButton = (Button) findViewById(R.id.b_fade_stop);
        stopFadeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mFadeOutCounterAsync.isCancelled()) {
                        ;
                    } else {
                        mFadeOutCounterAsync.cancel(true);
                        String target = tvFadeTarget.getText().toString();
                        String userValue = tvFadeCounter.getText().toString();
                        compareUserValueToTarget(userValue, target, tvFadeCounter);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fade_out_counter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFadeCountComplete(Double seconds) {
        tvFadeCounter.setText(String.format("%.2f", seconds));
        tvFadeCounter.setTextColor(0xffff0000);
    }

    @Override
    public void onFragmentInteraction() {
        Log.d(DEBUG_TAG, "onFragmentInteraction running in FadeOutCounterActivity");
        mDialogFragment.dismiss();
    }

    private void compareUserValueToTarget(String userValue, String target, TextView tv) {
        if (userValue.equals(target)) {
            tv.setTextColor(getResources().getColor(R.color.green));
            int chancesLeft = mState.getLivesRemaining();
            mState.setLivesRemaining(chancesLeft + 1);
        } else {
            tv.setTextColor(getResources().getColor(R.color.red));
        }
    }
}
