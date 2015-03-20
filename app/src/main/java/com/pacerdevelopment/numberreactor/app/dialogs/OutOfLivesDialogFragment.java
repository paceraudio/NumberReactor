package com.pacerdevelopment.numberreactor.app.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pacerdevelopment.numberreactor.app.R;


/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.pacerdevelopment.numberreactor.app.dialogs.OutOfLivesDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link com.pacerdevelopment.numberreactor.app.dialogs.OutOfLivesDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OutOfLivesDialogFragment extends DialogFragment {


        private OnFragmentInteractionListener mListener;


        public static OutOfLivesDialogFragment newInstance() {
            OutOfLivesDialogFragment fragment = new OutOfLivesDialogFragment();
            return fragment;
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
            View v = inflater.inflate(R.layout.fragment_out_of_lives_dialog, null);

            Button okButton = (Button) v.findViewById(R.id.b_yes_out_of_lives_dialog);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onOkClicked();
                    }

                }
            });

            Button exitButton = (Button) v.findViewById(R.id.b_exit_out_of_lives_dialog);
            exitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onExitClicked();
                    }
                }
            });

            return v;
        }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {

        public void onOkClicked();

        public void onExitClicked();
    }

}
