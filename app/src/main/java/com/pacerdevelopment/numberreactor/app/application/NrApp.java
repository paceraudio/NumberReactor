package com.pacerdevelopment.numberreactor.app.application;

/**
 * Created by jeffwconaway on 9/26/14.
 */

import android.app.Application;
import android.content.Context;

import com.pacerdevelopment.numberreactor.app.model.Model;
import com.pacerdevelopment.numberreactor.app.model.ModelContract;

public class NrApp extends Application {

    private static Context context;

    public static ModelContract model;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        model = new Model();
        model.initGameStats();
    }

    public static Context getAppContext() {
        return context;
    }

    public static ModelContract getModel() {
        return model;
    }
}
