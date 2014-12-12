package com.paceraudio.numberreactor.app;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.util.Hashtable;

/**
 * Created by jeffwconaway on 12/12/14.
 */
public class MyTypefaces {

    private static final String TAG = "Typefaces";

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    private static MyTypefaces ourInstance = new MyTypefaces();

    public static  MyTypefaces getInstance() {
                return ourInstance;
    }

    public static Typeface get(Context context, String assetPath) {
        synchronized (cache) {
            if (!cache.containsKey(assetPath)) {
                try {
                    Typeface typeface  = Typeface.createFromAsset(context.getAssets(), assetPath);
                    cache.put(assetPath, typeface);
                } catch (Exception e) {
                    Log.e(TAG, "Couldn't get typeface " + assetPath + " " + e.getMessage());
                }
            }
        }

        return cache.get(assetPath);
    }

    private MyTypefaces() {
    }
}
