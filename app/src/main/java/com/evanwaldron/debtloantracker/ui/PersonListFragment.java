package com.evanwaldron.debtloantracker.ui;

import android.app.ActionBar;
import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.evanwaldron.debtloantracker.R;
import com.evanwaldron.debtloantracker.storage.DebtStorage;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Evan on 9/23/2014.
 */
public class PersonListFragment extends ListFragment implements NavigationActivity.ActionBarConfigurer, ActionBar.OnNavigationListener{

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void configureActionBar(ActionBar actionBar) {
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setListNavigationCallbacks(ArrayAdapter.createFromResource(actionBar.getThemedContext(), R.array.person_list_nav_items, android.R.layout.simple_spinner_dropdown_item), this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        if(((NavigationActivity)getActivity()).isDrawerOpen()){
            super.onCreateOptionsMenu(menu, inflater);
        }else{
            inflater.inflate(R.menu.person_list, menu);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return false;
    }

    private static final class PersonListAdapter extends CursorAdapter {

        private NumberFormat mCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        private final Context ctx;

        public PersonListAdapter(final Context ctx){
            super(ctx, null, true);
            this.ctx = ctx;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = View.inflate(context, R.layout.person_list_item, null);

            v.setTag(R.id.name, v.findViewById(R.id.name));
            v.setTag(R.id.balance, v.findViewById(R.id.balance));

            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView nameView, balanceView;

            nameView = (TextView) view.getTag(R.id.name);
            balanceView = (TextView) view.getTag(R.id.balance);

            int id = cursor.getInt(cursor.getColumnIndex(DebtStorage.PersonInfo.ID));
            String name = cursor.getString(cursor.getColumnIndex(DebtStorage.PersonInfo.NAME));
            double balance = cursor.getDouble(cursor.getColumnIndex(DebtStorage.PersonInfo.NET_BALANCE));

            view.setTag(R.id.person_id, id);
            nameView.setText(name);
            bindBalance(balanceView, balance);

        }

        public void bindBalance(TextView view, double balance){
            int color = (balance < 0) ? Color.RED : ctx.getResources().getColor(R.color.loan_green);
            view.setTextColor(color);
            view.setText(mCurrencyFormat.format(Math.abs(balance)));
        }
    }
}
