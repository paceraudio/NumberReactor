package com.pacerdevelopment.numberreactor.app.util;

import android.content.Context;
import android.graphics.Typeface;
import java.util.Hashtable;

/**
 * Created by jeffwconaway on 4/6/15.
 */
public class CustomTypeface {

    public static final Hashtable<String, Typeface> cachedTypeface = new Hashtable<>();

    private static CustomTypeface typefaceInstance = new CustomTypeface();

    private CustomTypeface(){}

    public static CustomTypeface getInstance() {
        return typefaceInstance;
    }

    public static Typeface get(Context context, String assetPath) {
        synchronized (cachedTypeface) {
            if (!cachedTypeface.containsKey(assetPath)) {
                try {
                    Typeface tf = Typeface.createFromAsset(context.getAssets(), assetPath);
                    cachedTypeface.put(assetPath, tf);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return cachedTypeface.get(assetPath);
    }
}
