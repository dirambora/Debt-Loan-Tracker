package com.evanwaldron.debtloantracker.prefs;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

import com.evanwaldron.debtloantracker.R;

import java.util.Arrays;

/**
 * Created by Evan on 9/26/14 3:29 PM.
 */
public class NumberPickerPreference extends DialogPreference {

    private static final int[] PICKER_VALUES = { 25, 50, 75, 100, 150, 200, 250, 500, -1 };

    private static final int DEFAULT_VALUE = 100;

    private final String SUMMARY_SUFFIX;

    private NumberPicker mNumberPicker;
    private int mCurrentValue, mCurrentIndex;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.dialog_pref_num_selector);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        SUMMARY_SUFFIX = context.getString(R.string.pref_summary_history_show_num);

        setDialogIcon(null);
    }

    @Override
    protected Parcelable onSaveInstanceState(){
        final Parcelable superState = super.onSaveInstanceState();
        if(isPersistent()){
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = mCurrentIndex;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state){
        if(state == null || !state.getClass().equals(SavedState.class)){
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mNumberPicker.setValue(myState.value);
        updateSummary();
    }

    @Override
    protected View onCreateDialogView(){
        View v = super.onCreateDialogView();
        mNumberPicker = (NumberPicker) v.findViewById(R.id.num_picker);
        mNumberPicker.setDisplayedValues(getContext().getResources().getStringArray(R.array.num_selector_vals));
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(PICKER_VALUES.length - 1);
        mNumberPicker.setValue(mCurrentIndex);
        return v;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult){
        if(positiveResult){
            mCurrentIndex = mNumberPicker.getValue();
            mCurrentValue = PICKER_VALUES[mCurrentIndex];
            persistInt(mCurrentValue);
            updateSummary();
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue){
        if(restorePersistedValue){
            mCurrentValue= this.getPersistedInt(DEFAULT_VALUE);
        }else{
            mCurrentValue = (Integer) defaultValue;
            persistInt(mCurrentValue);
        }
        mCurrentIndex = Arrays.binarySearch(PICKER_VALUES, mCurrentValue);
        updateSummary();
    }

    private static final String SUMMARY_NO_LIMIT = "No Limit";

    private void updateSummary(){
        String summary = (mCurrentValue == -1) ? SUMMARY_NO_LIMIT : mCurrentValue + SUMMARY_SUFFIX;
        setSummary(summary.toString());
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index){
        return a.getInteger(index, DEFAULT_VALUE);
    }


    private static class SavedState extends BaseSavedState{

        int value;

        public SavedState(Parcel source) {
            super(source);
            value = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags){
            super.writeToParcel(dest, flags);

            dest.writeInt(value);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
