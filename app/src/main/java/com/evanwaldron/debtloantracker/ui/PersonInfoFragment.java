package com.evanwaldron.debtloantracker.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.evanwaldron.debtloantracker.R;
import com.evanwaldron.debtloantracker.storage.Storage;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Evan on 9/22/2014.
 */
public class PersonInfoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String ARG_PERSON_ID = "person_id";
    private NumberFormat mCurrencyFormat;

    private TextView mName, mBalance, mNumUnpayed;

    public static PersonInfoFragment newInstance(int personId){
        PersonInfoFragment fragment = new PersonInfoFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_PERSON_ID, personId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_person_info, null);

        mName = (TextView) v.findViewById(R.id.name);
        mBalance = (TextView) v.findViewById(R.id.balance);
        mNumUnpayed = (TextView) v.findViewById(R.id.num_unpayed);

        getLoaderManager().initLoader(R.id.person_info_loader, null, this);

        return v;
    }

    private static final String[] PERSON_INFO_PROJECTION = { Storage.PersonInfo.ID, Storage.PersonInfo.NAME, Storage.PersonInfo.NET_BALANCE, Storage.PersonInfo.NUM_UNPAYED };
    private static final String PERSON_INFO_SELECTION = Storage.PersonInfo.ID + " = ?";

    private void setCursor(Cursor data){
        if(data == null){
            return;
        }

        String name = data.getString(data.getColumnIndex(Storage.PersonInfo.NAME));
        double balance = data.getDouble(data.getColumnIndex(Storage.PersonInfo.NET_BALANCE));
        int numUnpayed = data.getInt(data.getColumnIndex(Storage.PersonInfo.NUM_UNPAYED));

        setName(name);
        setBalance(balance);
        setNumUnpayed(numUnpayed);
    }

    private void setName(String name){
        mName.setText(name);
    }
    private void setNumUnpayed(int numUnpayed){
        mNumUnpayed.setText(getString(R.string.num_unpayed) + " " + numUnpayed);
    }

    private void setBalance(double balance){
        int color;
        String prefix;
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if(balance < 0){
            prefix = getString(R.string.you_owe);
            color = Color.RED;
            balance *= -1;
        }else{
            prefix = getString(R.string.owes_you);
            color = getResources().getColor(R.color.loan_green);
        }
        builder.append(prefix + " " + mCurrencyFormat.format(balance));
        builder.setSpan(new ForegroundColorSpan(color), prefix.length() + 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBalance.setText(builder);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        int personId = getArguments().getInt(ARG_PERSON_ID);
        return new CursorLoader(getActivity(), Storage.PersonInfo.CONTENT_URI, PERSON_INFO_PROJECTION, PERSON_INFO_SELECTION, new String[]{ Integer.toString(personId)}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount() == 0){
            return;
        }
        data.moveToFirst();
        setCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        setCursor(null);
    }
}
