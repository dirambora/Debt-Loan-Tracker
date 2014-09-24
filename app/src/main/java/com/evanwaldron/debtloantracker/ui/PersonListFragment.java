package com.evanwaldron.debtloantracker.ui;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.evanwaldron.debtloantracker.R;
import com.evanwaldron.debtloantracker.storage.Storage;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Evan on 9/23/2014.
 */
public class PersonListFragment extends ListFragment
        implements NavigationActivity.ActionBarConfigurer, ActionBar.OnNavigationListener, LoaderManager.LoaderCallbacks<Cursor>{

    private CursorAdapter mAdapter;
    private int mCurNavSelection = -1;
    private String mSelection = SELECTION_ALL;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAdapter = new PersonListAdapter(getActivity());
        setListAdapter(mAdapter);
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

    private static final String TAG_ADD_ITEM_DIALOG = "add_item_dialog";

    private void showAddItemDialog(int mode){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(TAG_ADD_ITEM_DIALOG);
        if(prev != null){
            transaction.remove(prev);
        }

        DialogFragment dialog = AddItemDialog.newInstance(mode);
        dialog.show(transaction, TAG_ADD_ITEM_DIALOG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_add_debt:
                showAddItemDialog(AddItemDialog.MODE_DEBT);
                return true;
            case R.id.action_add_loan:
                showAddItemDialog(AddItemDialog.MODE_LOAN);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static final String SELECTION_ALL = null;
    private static final String SELECTION_DEBTS_ONLY = Storage.PersonInfo.NET_BALANCE + " < 0";
    private static final String SELECTION_LOANS_ONLY = Storage.PersonInfo.NET_BALANCE + " > 0";

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if(itemPosition == mCurNavSelection){ return true; }

        mCurNavSelection = itemPosition;
        switch(itemPosition){
            case 0:
                mSelection = SELECTION_ALL;
                break;
            case 1:
                mSelection = SELECTION_DEBTS_ONLY;
                break;
            case 2:
                mSelection = SELECTION_LOANS_ONLY;
                break;
        }

        startLoader();

        return true;
    }

    private void startLoader(){
        LoaderManager manager = getLoaderManager();
        if(manager.getLoader(R.id.person_list_loader) == null){
            manager.initLoader(R.id.person_list_loader, null, this);
        }else{
            manager.restartLoader(R.id.person_list_loader, null, this);
        }
    }

    private static final String[] PERSON_LIST_PROJECTION = { Storage.PersonInfo.ID, Storage.PersonInfo.NAME, Storage.PersonInfo.NET_BALANCE };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = prefs.getString(getString(R.string.pref_key_person_list_sort_by), getString(R.string.pref_person_list_sort_by_default));
        return new CursorLoader(getActivity(), Storage.PersonInfo.CONTENT_URI, PERSON_LIST_PROJECTION, mSelection, null, sortBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if(data.getCount() == 0){
            setEmptyText("No entries");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
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

            int id = cursor.getInt(cursor.getColumnIndex(Storage.PersonInfo.ID));
            String name = cursor.getString(cursor.getColumnIndex(Storage.PersonInfo.NAME));
            double balance = cursor.getDouble(cursor.getColumnIndex(Storage.PersonInfo.NET_BALANCE));

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
