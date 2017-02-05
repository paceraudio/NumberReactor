package com.pacerdevelopment.numberreactor.app.counter.vp_counter;

/**
 * Created by jeffwconaway on 1/29/17.
 */

interface CounterContract {

    interface View {

        void viewStats();

        void onRoundedCount(double roundedCount);
    }

    interface Presenter {

        void onViewGameStats();

        void obtainRoundedCount(long elapsedCount, double counterCeiling);
    }

}
