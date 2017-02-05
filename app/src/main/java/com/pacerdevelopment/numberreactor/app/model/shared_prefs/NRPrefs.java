package com.pacerdevelopment.numberreactor.app.model.shared_prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.pacerdevelopment.numberreactor.app.R;

/**
 * Created by jeffwconaway on 2/5/17.
 */

public class NRPrefs {

    private Context context;
    protected SharedPreferences prefs;
    protected PackageInfo versionInfo;
    protected String appNamePlusVersion;
    protected String dbNotNullPrefsKey;

    public NRPrefs(Context context) {
        this.context = context.getApplicationContext();
    }

    private void initSharedPrefsElements() {
        versionInfo = getPackageInfo();
        appNamePlusVersion = context.getString(R.string.app_name) + versionInfo.versionName;
        dbNotNullPrefsKey = context.getString(R.string.prefs_db_not_null_key);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    protected boolean checkSharedPrefsForPreviousInstall() {
        return prefs.getBoolean(appNamePlusVersion, false);
    }

    protected void setSharedPrefsGameInstalled(boolean isInstalled) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(appNamePlusVersion, isInstalled);
        editor.commit();
    }

    public boolean checkSharedPrefsForDbNotNull() {
        return prefs.getBoolean(dbNotNullPrefsKey, false);
    }

    protected void setSharedPrefsDbNotNull() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(dbNotNullPrefsKey, true);
        editor.commit();
    }

    protected PackageInfo getPackageInfo() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo;
    }
}
