package com.evanwaldron.debtloantracker.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.evanwaldron.debtloantracker.R;

/**
 * Created by Evan on 9/27/14 11:27 AM.
 */
public class ConfirmDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public static final String FRAGMENT_TAG = "confirm_dialog";

    public static ConfirmDialogFragment newInstance(String message){
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();

        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    private String mMessage;
    private Handler onConfirm;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mMessage = getArguments().getString(ARG_MESSAGE);
    }

    private static final int MESSAGE_COMPLETED = 1;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.are_you_sure)
                .setMessage(mMessage)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Message msg = onConfirm.obtainMessage(MESSAGE_COMPLETED);
                        onConfirm.dispatchMessage(msg);
                    }
                })
                .create();
    }

    public void setHandler(final Handler onConfirm){
        this.onConfirm = onConfirm;
    }
}
