package com.pacerdevelopment.numberreactor.app.counter.vp_counter;

import com.pacerdevelopment.numberreactor.app.model.ModelContract;

/**
 * Created by jeffwconaway on 2/5/17.
 */

public class CounterPresenter implements CounterContract.Presenter {

    CounterContract.View view;
    ModelContract model;

    public CounterPresenter(CounterContract.View view, ModelContract modelContract) {
        this.view = view;
        this.model = modelContract;
    }

    @Override
    public void onViewGameStats() {
        if (model.checkPrefsForDbNotNull()) {
            view.viewStats();
        }
    }

    @Override
    public void obtainRoundedCount(long elapsedCount, double counterCeiling) {
        double roundedCount = model.roundElapsedCount(elapsedCount, counterCeiling);
        view.onRoundedCount(roundedCount);
    }
}
