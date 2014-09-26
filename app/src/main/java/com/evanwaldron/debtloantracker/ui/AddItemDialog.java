package com.evanwaldron.debtloantracker.ui;

import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.evanwaldron.debtloantracker.R;
import com.evanwaldron.debtloantracker.storage.tasks.AddItemTask;

/**
 * Created by Evan on 9/9/14.
 */
public class AddItemDialog extends DialogFragment{

    private static final String ARG_PERSON_NAME = "person_name";
    private static final String ARG_MODE = "mode";

    public static final int MODE_DEBT = 0;
    public static final int MODE_LOAN = 1;

    public static AddItemDialog newInstance(int mode){
        return newInstance(mode, null);
    }

    public static AddItemDialog newInstance(int mode, String personName){
        AddItemDialog dialog = new AddItemDialog();

        Bundle args = new Bundle();
        args.putInt(ARG_MODE, mode);
        if(personName != null) {
            args.putString(ARG_PERSON_NAME, personName);
        }
        dialog.setArguments(args);

        return dialog;
    }

    private boolean mFixedName;
    private String mPersonName = null, mTitle;
    private int mMode;
    private TextView mNameInput, mAmountInput,  mDescriptionInput;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mMode = args.getInt(ARG_MODE);
        mFixedName = args.containsKey(ARG_PERSON_NAME);
        if(mFixedName){
            mPersonName = args.getString(ARG_PERSON_NAME);
        }

        mTitle = (mMode == MODE_DEBT ? getString(R.string.action_add_debt) : getString(R.string.action_add_loan));
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        getDialog().setTitle(mTitle);

        View v = inflater.inflate(R.layout.dialog_add_item, container);

        Button ok = (Button) v.findViewById(R.id.ok);
        Button cancel = (Button) v.findViewById(R.id.cancel);
        mNameInput = (TextView) v.findViewById(R.id.name);
        mAmountInput = (TextView) v.findViewById(R.id.amount);
        mDescriptionInput = (TextView) v.findViewById(R.id.description);

        if(mFixedName){
            mNameInput.setText(mPersonName);
            mNameInput.setFocusable(false);
            mNameInput.setFocusableInTouchMode(false);
        }


        ok.setOnClickListener(new OkClickListener());
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }

    private class OkClickListener implements View.OnClickListener{

        private String readNameInput(){
            return mNameInput.getText().toString();
        }
        private String readDescriptionInput(){
            return mDescriptionInput.getText().toString();
        }

        private double readAmountInput(){
            String text = mAmountInput.getText().toString();
            try{
                double amount = Double.parseDouble(text);
                return (mMode == MODE_DEBT) ? amount * -1 : amount;
            }catch(NumberFormatException e){
                return 0.0;
            }
        }

        @Override
        public void onClick(View v){
            String name = readNameInput();
            String description = readDescriptionInput();
            double amount = readAmountInput();

            if(TextUtils.isEmpty(name) || amount == 0.0){
                Toast.makeText(getActivity(), "Invalid Input", Toast.LENGTH_SHORT).show();
                return;
            }

            AddItemTask.TaskInfo info = new AddItemTask.TaskInfo();
            info.name = name;
            info.amount = amount;
            info.description = description;

            AddItemTask addItemTask = new AddItemTask(getActivity().getContentResolver(), new OnAddItemHandler(getActivity()));
            addItemTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, info);

            dismiss();
        }
    }

    private static class OnAddItemHandler extends Handler{
        private final Context ctx;
        public OnAddItemHandler(final Context ctx){
            this.ctx = ctx;
        }
        @Override
        public void handleMessage(Message msg){
            String message = (String)msg.obj;
            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
        }
    }
}
