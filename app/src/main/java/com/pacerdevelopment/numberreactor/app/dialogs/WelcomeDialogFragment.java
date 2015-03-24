package com.pacerdevelopment.numberreactor.app.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pacerdevelopment.numberreactor.app.R;

/**
 * Created by jeffwconaway on 3/22/15.
 */
public class WelcomeDialogFragment extends DialogFragment{

    public static WelcomeDialogFragment newInstance() {
        return new WelcomeDialogFragment();
    }

    public WelcomeDialogFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.NR_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View v = inflater.inflate(R.layout.welcome_dialog, null);
        Button okButton = (Button) v.findViewById(R.id.b_ok_welcome_dialog_frag);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return v;

    }
}
