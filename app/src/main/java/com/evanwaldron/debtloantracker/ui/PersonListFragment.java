package com.evanwaldron.debtloantracker.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.evanwaldron.debtloantracker.R;
import com.evanwaldron.debtloantracker.storage.tasks.DeletePersonTask;
import com.evanwaldron.debtloantracker.storage.Storage;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Evan on 9/23/2014.
 */
public class PersonListFragment extends ListFragment
        implements NavigationActivity.ActionBarConfigurer, ActionBar.OnNavigationListener, LoaderManager.LoaderCallbacks<Cursor>{

    /* ------------------- Public constants ---------------------- */
    public static final String TAG = "person_list";
    /* ----------------- End public constants -------------------- */


    /* ------------------- Private contants ---------------------- */
    private static final String TAG_ADD_ITEM_DIALOG = "add_item_dialog";
    private static final String TAG_SORT_BY_DIALOG = "sort_by_dialog";
    private static final String TAG_LONG_CLICK_OPTIONS = "long_click_options";

    private static final String SELECTION_ALL = null;
    private static final String SELECTION_DEBTS_ONLY = Storage.PersonInfo.NET_BALANCE + " < 0";
    private static final String SELECTION_LOANS_ONLY = Storage.PersonInfo.NET_BALANCE + " > 0";

    private static final String[] PERSON_LIST_PROJECTION = { Storage.PersonInfo.ID, Storage.PersonInfo.NAME, Storage.PersonInfo.NET_BALANCE };
    private static final String SELECTION_CLAUSE_HIDE_ZERO = " AND " + Storage.PersonInfo.NET_BALANCE + " != 0.0";
    /* ----------------- End private constants ------------------- */


    /* -------------------- Class variables ---------------------- */
    private CursorAdapter mAdapter;
    private int mCurNavSelection = -1;
    private String mSelection = SELECTION_ALL;
    /* ------------------ End class variables -------------------- */


    /* ------------------ Lifecycle methods ---------------------- */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAdapter = new PersonListAdapter(getActivity());
        setListAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = super.onCreateView(inflater, parent, savedInstanceState);

        ListView list = (ListView) v.findViewById(android.R.id.list);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int personId = (Integer) view.getTag(R.id.person_id);
                String personName = ((TextView)view.getTag(R.id.name)).getText().toString();
                showLongClickOptions(personId, personName);
                return true;
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        if(((NavigationActivity)getActivity()).isDrawerOpen()){
            super.onCreateOptionsMenu(menu, inflater);
        }else{
            inflater.inflate(R.menu.person_list, menu);
        }
    }
    /* --------------- End lifecycle methods -------------------- */

    
    /* ------------- Item selection callbacks ------------------- */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_add_debt:
                showAddItemDialog(AddItemDialog.MODE_DEBT);
                return true;
            case R.id.action_add_loan:
                showAddItemDialog(AddItemDialog.MODE_LOAN);
                return true;
            case R.id.action_sort_by:
                showSortByDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onListItemClick(ListView list, View view, int position, long id){
        int personId = (Integer) view.getTag(R.id.person_id);
        String personName = ((TextView)view.getTag(R.id.name)).getText().toString();

        showPersonDetails(personId, personName);
    }
    /* ----------- End item selection callbacks ----------------- */


    /* ------ ActionBar.OnNavigationListener Callback ----------- */
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
    /* ---- End ActionBar.OnNavigationListener Callback --------- */


    /* ---- NavigationActivity.ActionBarConfigurer Callback ----- */
    @Override
    public void configureActionBar(ActionBar actionBar) {
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setListNavigationCallbacks(ArrayAdapter.createFromResource(actionBar.getThemedContext(), R.array.person_list_nav_items, android.R.layout.simple_spinner_dropdown_item), this);
    }
    /* -- End NavigationActivity.ActionBarConfigurer Callback --- */


    /* ------------ Private class methods ----------------------- */
    private void showAddItemDialog(int mode){
        DialogFragment dialog = AddItemDialog.newInstance(mode);
        showDialogFragment(dialog, TAG_ADD_ITEM_DIALOG);
    }
    private void showSortByDialog(){
        SortByDialogFragment dialog = new SortByDialogFragment();
        dialog.setHandler(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                getLoaderManager().restartLoader(R.id.person_list_loader, null, PersonListFragment.this);
                return true;
            }
        }));

        showDialogFragment(dialog, TAG_SORT_BY_DIALOG);
    }
    private void showLongClickOptions(int personId, String name){
        LongClickDialog dialog = LongClickDialog.newInstance(personId, name);
        showDialogFragment(dialog, TAG_LONG_CLICK_OPTIONS);
    }
    private void showDialogFragment(DialogFragment fragment, String tag){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if(tag != null) {
            Fragment prev = getFragmentManager().findFragmentByTag(tag);
            if (prev != null) {
                transaction.remove(prev);
            }
        }
        transaction.addToBackStack(null);
        fragment.show(transaction, tag);
    }
    private void showPersonDetails(int personId, String personName){
        Intent intent = new Intent(getActivity(), PersonDetailActivity.class);
        intent.putExtra(PersonDetailFragment.ARG_PERSON_NAME, personName);
        intent.putExtra(PersonDetailFragment.ARG_PERSON_ID, personId);
        startActivity(intent);
    }
    private void deletePerson(int personId){
        DeletePersonTask task = new DeletePersonTask(getActivity().getContentResolver(), new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Toast.makeText(getActivity(), (String)msg.obj, Toast.LENGTH_SHORT).show();
                return true;
            }
        }));
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, personId);
    }
    private void startLoader(){
        LoaderManager manager = getLoaderManager();
        if(manager.getLoader(R.id.person_list_loader) == null){
            manager.initLoader(R.id.person_list_loader, null, this);
        }else{
            manager.restartLoader(R.id.person_list_loader, null, this);
        }
    }
    /* -------------- End private class methods ----------------- */


    /* --------- LoaderManager.LoaderCallbacks callback -------- */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortBy = prefs.getString(getString(R.string.pref_key_person_list_sort_by), getString(R.string.pref_person_list_sort_by_default));
        boolean showZeroBalance = prefs.getBoolean(getString(R.string.pref_key_person_list_show_zero), true);
        return new CursorLoader(getActivity(), Storage.PersonInfo.CONTENT_URI, PERSON_LIST_PROJECTION, !showZeroBalance ? mSelection + SELECTION_CLAUSE_HIDE_ZERO : mSelection, null, sortBy);
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
    /* ------- End LoaderManager.LoaderCallbacks callback ------ */


    /* ------------- Private subclasses -------------- */
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
    /* ----------- End private subclasses ------------ */


    /* ------------- Public subclasses --------------- */
    public static final class SortByDialogFragment extends DialogFragment{

        private String[] sortByOptions;
        private Handler onFinish;

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            sortByOptions = getResources().getStringArray(R.array.person_list_sort_by_vals);
        }

        public void setHandler(final Handler handler){
            onFinish = handler;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortBy = prefs.getString(getString(R.string.pref_key_person_list_sort_by), getString(R.string.pref_person_list_sort_by_default));
            final int curPos = getCurPos(sortBy);
            builder.setTitle(getString(R.string.action_sort_by))
                    .setSingleChoiceItems(R.array.person_list_sort_by, curPos, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == curPos) {
                                dismiss();
                                return;
                            }

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(getString(R.string.pref_key_person_list_sort_by), sortByOptions[which]).commit();

                            Message msg = onFinish.obtainMessage();
                            onFinish.dispatchMessage(msg);
                            dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);

            return builder.create();
        }

        private int getCurPos(String sortBy){
            for(int i = 0; i < 4; i++){
                if(sortBy.equals(sortByOptions[i])) {
                    return i;
                }
            }
            return -1;
        }
    }
    public static final class LongClickDialog extends DialogFragment{

        private static final String ARG_PERSON_ID = "person_id";
        private static final String ARG_PERSON_NAME = "person_name";

        private static final int DETAILS = 0;
        private static final int CLEAR_ITEMS = 1;
        private static final int DELETE_PERSON = 2;

        public static LongClickDialog newInstance(int personId, String personName){
            LongClickDialog dialog = new LongClickDialog();

            Bundle args = new Bundle();
            args.putInt(ARG_PERSON_ID, personId);
            args.putString(ARG_PERSON_NAME, personName);
            dialog.setArguments(args);

            return dialog;
        }

        private int mPersonId;
        private String mPersonName;

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            mPersonId = getArguments().getInt(ARG_PERSON_ID);
            mPersonName = getArguments().getString(ARG_PERSON_NAME);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(mPersonName)
                    .setItems(R.array.person_long_click_menu, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PersonListFragment fragment = (PersonListFragment) getFragmentManager().findFragmentByTag(TAG);
                            switch (which){
                                case DELETE_PERSON:
                                    fragment.deletePerson(mPersonId);
                                    break;
                                case DETAILS:
                                    fragment.showPersonDetails(mPersonId, mPersonName);
                                    break;
                            }
                        }
                    });

            return builder.create();
        }

    }
    /* ----------- End public subclasses ------------- */
}
