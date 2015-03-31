package com.pacerdevelopment.numberreactor.app.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pacerdevelopment.numberreactor.app.R;


public class OutOfLivesDialogFragment extends android.app.DialogFragment {

    private OutOfLivesListener mListener;

    public static OutOfLivesDialogFragment newInstance() {
        return new OutOfLivesDialogFragment();
    }


    public OutOfLivesDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.NR_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_out_of_lives_dialog, container, true);

        Button okButton = (Button) v.findViewById(R.id.b_yes_out_of_lives_dialog);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onOkClickedOutOfLivesDialog();
                    dismiss();
                }
            }
        });

        Button exitButton = (Button) v.findViewById(R.id.b_exit_out_of_lives_dialog);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onExitClickedOutOfLivesDialog();
                }
            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OutOfLivesListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OutOfLivesListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OutOfLivesListener {

        public void onOkClickedOutOfLivesDialog();

        public void onExitClickedOutOfLivesDialog();
    }

}
