package com.pacerdevelopment.numberreactor.app.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.pacerdevelopment.numberreactor.app.R;

import static com.pacerdevelopment.numberreactor.app.activities.CounterActivity.FROM_COUNTER_ACTIVITY_FLAG;
import static com.pacerdevelopment.numberreactor.app.activities.FadeOutCounterActivity.FROM_FADE_COUNTER_ACTIVITY_FLAG;

public class SettingsActivity extends PreferenceActivity {

   /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);
        ActionBar actionBar = getActionBar();
        actionBar.hide();
    }*/

    Intent mIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIntent = getIntent();
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
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

    public static class SettingsFragment extends  PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }

        /*@Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.menu_settings_frag, menu);
        }*/
    }
}
