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
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Pair;
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
import com.evanwaldron.debtloantracker.storage.tasks.SetPaidTask;
import com.evanwaldron.debtloantracker.storage.Storage;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Evan on 9/23/14.
 */
public class ItemListFragment extends ListFragment implements ActionBar.OnNavigationListener, LoaderManager.LoaderCallbacks<Cursor>{

    public static final String FRAGMENT_TAG = "item_list";

    private static final String LOG_TAG = ItemListFragment.class.getSimpleName();

    private static final String ARG_PERSON_ID = "person_id";
    private static final String ARG_PERSON_NAME = "person_name";

    private int mPersonId, mCurNavSelection = 0;
    private String mPersonName, mSelection;
    private CursorAdapter mAdapter;


    public static ItemListFragment newInstance(int personId, String personName){
        ItemListFragment fragment = new ItemListFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_PERSON_ID, personId);
        args.putString(ARG_PERSON_NAME, personName);
        fragment.setArguments(args);

        return fragment;
    }

    private static final String TAG_ITEM_OPTIONS_DIALOG = "item_options_dialog";

    private void showItemOptionsDialog(int id, boolean payed){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(TAG_ITEM_OPTIONS_DIALOG);
        if(prev != null){
            transaction.remove(prev);
        }

        DialogFragment dialog = ItemOptionsDialogFragment.newInstance(id, payed);
        dialog.show(transaction, TAG_ITEM_OPTIONS_DIALOG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = super.onCreateView(inflater, parent, savedInstanceState);

        ListView list = (ListView) v.findViewById(android.R.id.list);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int itemId;
                boolean payed;

                itemId = (Integer) view.getTag(R.id.item_id);
                payed = (Boolean) view.getTag(R.id.item_paid);

                showItemOptionsDialog(itemId, payed);
                return true;
            }
        });

        startLoader();
        return v;
    }

    private void setPaid(int itemId, boolean paid){
        SetPaidTask setPaid = new SetPaidTask(getActivity().getContentResolver(), new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Toast.makeText(getActivity(), (String)msg.obj, Toast.LENGTH_SHORT).show();
                return true;
            }
        }));
        setPaid.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Pair<>(itemId, paid));
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        mPersonId = args.getInt(ARG_PERSON_ID);
        mPersonName = args.getString(ARG_PERSON_NAME);

        mAdapter = new ItemListAdapter(getActivity());
        setListAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.item_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        menu.findItem(R.id.action_show_paid).setChecked(prefs.getBoolean(getString(R.string.pref_key_item_list_show_paid), false));
    }

    private void setShowPaid(boolean showPaid){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putBoolean(getString(R.string.pref_key_item_list_show_paid), showPaid);
        editor.commit();
    }

    private static final String TAG_SORT_BY_DIALOG = "sort_by_dialog";

    private void showSortByDialog(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(TAG_SORT_BY_DIALOG);
        if(prev != null){
            transaction.remove(prev);
        }

        SortByDialogFragment dialog = new SortByDialogFragment();
        dialog.setHandler(new Handler(new Handler.Callback(){
            @Override
            public boolean handleMessage(Message msg){
                startLoader();
                return true;
            }
        }));
        dialog.show(transaction, TAG_SORT_BY_DIALOG);
    }

    private static final String TAG_ADD_ITEM_DIALOG = "add_item_dialog";

    private void showAddItemDialog(int mode){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(TAG_ADD_ITEM_DIALOG);
        if(prev != null){
            transaction.remove(prev);
        }

        DialogFragment dialog = AddItemDialog.newInstance(mode, mPersonName);
        dialog.show(transaction, TAG_ADD_ITEM_DIALOG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){
            case R.id.action_show_paid:
                setShowPaid(!item.isChecked());
                startLoader();
                return true;
            case R.id.action_sort_by:
                showSortByDialog();
                return true;
            case R.id.action_add_debt:
                showAddItemDialog(AddItemDialog.MODE_DEBT);
                return true;
            case R.id.action_add_loan:
                showAddItemDialog(AddItemDialog.MODE_LOAN);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void restoreActionBar(ActionBar actionBar){
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(ArrayAdapter.createFromResource(actionBar.getThemedContext(), R.array.person_list_nav_items, android.R.layout.simple_spinner_dropdown_item), this);
    }

    private void startLoader(){
        LoaderManager manager = getLoaderManager();
        if(manager.getLoader(R.id.item_list_loader) == null){
            manager.initLoader(R.id.item_list_loader, null, this);
        }else{
            manager.restartLoader(R.id.item_list_loader, null, this);
        }
    }

    private static final String SELECTION_ALL = null;
    private static final String SELECTION_DEBTS_ONLY = Storage.Items.AMOUNT + " < 0";
    private static final String SELECTION_LOANS_ONLY = Storage.Items.AMOUNT + " > 0";

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

    private static final String[] ITEM_LIST_PROJECTION = { Storage.Items.ID, Storage.Items.AMOUNT, Storage.Items.DESCRIPTION, Storage.Items.DATE_CREATED, Storage.Items.PAYED };
    private static final String ITEM_LIST_SELECTION = Storage.Items.PERSON_ID + " = ?";
    private static final String HIDE_PAID_CLAUSE = " AND " + Storage.Items.PAYED + " = 0";

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        StringBuilder selection = new StringBuilder(ITEM_LIST_SELECTION);
        if(mSelection != null){
            selection.append(" AND " + mSelection);
        }
        if(!prefs.getBoolean(getString(R.string.pref_key_item_list_show_paid), false)){
            selection.append(HIDE_PAID_CLAUSE);
        }
        String sortBy = prefs.getString(getString(R.string.pref_key_item_list_sort_by), getString(R.string.pref_item_list_sort_by_default));
        return new CursorLoader(getActivity(), Storage.Items.CONTENT_URI, ITEM_LIST_PROJECTION, selection.toString(), new String[]{ Integer.toString(mPersonId) }, sortBy);
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

    private static final class ItemListAdapter extends CursorAdapter{

        private static final DateFormat sDateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private static final NumberFormat sCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        private final Context ctx;

        public ItemListAdapter(Context ctx){
            super(ctx, null, true);
            this.ctx = ctx;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = View.inflate(context, R.layout.item_list_item, null);

            v.setTag(R.id.date, v.findViewById(R.id.date));
            v.setTag(R.id.description, v.findViewById(R.id.description));
            v.setTag(R.id.amount, v.findViewById(R.id.amount));

            return v;
        }

        private CharSequence getDateString(String dateIn){
            Date date;

            try{
                date = sDateParser.parse(dateIn);
            } catch (ParseException e) {
                return null;
            }

            return android.text.format.DateFormat.format("M/d/yy", date);
        }

        private void bindAmount(TextView view, double amount){
            int color = (amount < 0) ? Color.RED : ctx.getResources().getColor(R.color.loan_green);
            view.setTextColor(color);
            view.setText(sCurrencyFormat.format(Math.abs(amount)));
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView dateView, descriptionView, amountView;

            dateView = (TextView) view.getTag(R.id.date);
            descriptionView = (TextView) view.getTag(R.id.description);
            amountView = (TextView) view.getTag(R.id.amount);

            view.setTag(R.id.item_id, cursor.getInt(cursor.getColumnIndex(Storage.Items.ID)));
            view.setTag(R.id.item_paid, (cursor.getInt(cursor.getColumnIndex(Storage.Items.PAYED)) == 1));

            dateView.setText(getDateString(cursor.getString(cursor.getColumnIndex(Storage.Items.DATE_CREATED))));
            descriptionView.setText(cursor.getString(cursor.getColumnIndex(Storage.Items.DESCRIPTION)));
            bindAmount(amountView, cursor.getDouble(cursor.getColumnIndex(Storage.Items.AMOUNT)));
        }
    }

    public static final class SortByDialogFragment extends DialogFragment {

        private String[] sortByOptions;
        private Handler onFinish;

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            sortByOptions = getResources().getStringArray(R.array.item_list_sort_by_vals);
        }

        public void setHandler(final Handler handler){
            onFinish = handler;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortBy = prefs.getString(getString(R.string.pref_key_item_list_sort_by), getString(R.string.pref_item_list_sort_by_default));
            final int curPos = getCurPos(sortBy);
            builder.setTitle(getString(R.string.action_sort_by))
                    .setSingleChoiceItems(R.array.item_list_sort_by, curPos, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == curPos) {
                                dismiss();
                                return;
                            }

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(getString(R.string.pref_key_item_list_sort_by), sortByOptions[which]).commit();

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

    public static final class ItemOptionsDialogFragment extends DialogFragment{

        private static final String ARG_ITEM_ID = "item_id";
        private static final String ARG_ITEM_PAYED = "item_paid";

        public static ItemOptionsDialogFragment newInstance(int itemId, boolean payed){
            ItemOptionsDialogFragment dialogFragment = new ItemOptionsDialogFragment();

            Bundle args = new Bundle();
            args.putInt(ARG_ITEM_ID, itemId);
            args.putBoolean(ARG_ITEM_PAYED, payed);
            dialogFragment.setArguments(args);

            return dialogFragment;
        }

        private int mItemId;
        private boolean mPayed;

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            Bundle args = getArguments();
            mItemId = args.getInt(ARG_ITEM_ID);
            mPayed = args.getBoolean(ARG_ITEM_PAYED);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.options)
                    .setItems(mPayed ? R.array.item_long_click_menu_paid : R.array.item_long_click_menu_unpaid, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch(which){
                                case 0:
                                    ItemListFragment fragment = (ItemListFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
                                    fragment.setPaid(mItemId, !mPayed);
                                    break;
                            }
                        }
                    });

            return builder.create();
        }

    }
}
