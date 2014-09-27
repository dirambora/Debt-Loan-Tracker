package com.evanwaldron.debtloantracker.ui;

import android.app.ActionBar;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.evanwaldron.debtloantracker.R;
import com.evanwaldron.debtloantracker.storage.Storage;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Evan on 9/23/2014.
 */
public class HistoryFragment extends ListFragment implements NavigationActivity.ActionBarConfigurer, LoaderManager.LoaderCallbacks<Cursor>{

    private int mItemLimit;
    private CursorAdapter mAdapter;

    /* ----------------- Lifecycle callbacks -------------------- */

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mItemLimit = prefs.getInt(getString(R.string.pref_key_history_show_items_num), -1);

        mAdapter = new HistoryAdapter(getActivity());
        setListAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View v = super.onCreateView(inflater, parent, savedInstanceState);

        startLoader();

        return v;
    }

    /* --------------- End Lifecycle callbacks ------------------ */

    /* ----------------- ActionBarConfigurer implementation ------------ */

    @Override
    public void configureActionBar(ActionBar actionBar) {
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.title_section_history);
    }

    /* -------------- End ActionBarConfigurer implementation ----------- */

    /* ---------------------- LoaderCallbacks methods ----------------------- */

    private static final String[] HISTORY_PROJECTION = {Storage.History.ID, Storage.History.CHANGE_AMOUNT, Storage.History.DATE, Storage.History.NAME, Storage.History.DESCRIPTION, Storage.History.ITEM_AMOUNT};
    private static final String HISTORY_SORT_ORDER = Storage.History.DATE + " DESC";

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri queryUri = Storage.History.CONTENT_URI;
        if(mItemLimit >= 0){
            queryUri.buildUpon().appendQueryParameter("limit", Integer.toString(mItemLimit));
        }
        return new CursorLoader(getActivity(), queryUri, HISTORY_PROJECTION, null, null, HISTORY_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if(data.getCount() == 0){
            setEmptyText("No History");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    /* -------------------- End LoaderCallbacks methods --------------------- */

    /* ------------------ Local private methods ----------------------------- */

    private void startLoader(){
        LoaderManager manager = getLoaderManager();
        if(manager.getLoader(R.id.item_list_loader) == null){
            manager.initLoader(R.id.item_list_loader, null, this);
        }else{
            manager.restartLoader(R.id.item_list_loader, null, this);
        }
    }

    /* --------------- End local private methods ---------------------------- */

    /* -------------------- Private Subclasses ------------------------------ */

    private static final class HistoryAdapter extends CursorAdapter{

        private static final DateFormat sDateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        private final Context ctx;

        public HistoryAdapter(Context context) {
            super(context, null, true);
            this.ctx = context;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = View.inflate(context, R.layout.history_item, null);

            v.setTag(R.id.description, v.findViewById(R.id.description));
            v.setTag(R.id.date, v.findViewById(R.id.date));

            return v;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView descriptionView, dateView;

            descriptionView = (TextView) view.getTag(R.id.description);
            dateView = (TextView) view.getTag(R.id.date);

            String name = cursor.getString(cursor.getColumnIndex(Storage.History.NAME));
            String itemDescription = cursor.getString(cursor.getColumnIndex(Storage.History.DESCRIPTION));
            String dateString = cursor.getString(cursor.getColumnIndex(Storage.History.DATE));
            double changeAmount = cursor.getDouble(cursor.getColumnIndex(Storage.History.CHANGE_AMOUNT));
            double itemAmount = cursor.getDouble(cursor.getColumnIndex(Storage.History.ITEM_AMOUNT));

            Date date = parseDate(dateString);
            Spanned description = new HistoryDescription(ctx, name, itemDescription, changeAmount, itemAmount);
            descriptionView.setText(description);
            dateView.setText(android.text.format.DateFormat.format("MMM d \nh:mm a ", date));
        }

        private Date parseDate(String input){
            try{
                return sDateParser.parse(input);
            }catch (ParseException e) {
                return null;
            }
        }

        private static class HistoryDescription implements Spanned{

            private static final NumberFormat sCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());

            private SpannableStringBuilder spannable;

            private static final void appendNamesPortion(Context ctx, SpannableStringBuilder builder, String name, double changeAmount, double itemAmount){
                String firstName, secondName, middleText;
                if(changeAmount < 0){
                    firstName = name;
                    secondName = ctx.getString(R.string.you_lower_case);
                }else{
                    firstName = ctx.getString(R.string.you_upper_case);
                    secondName = name;
                }
                middleText = (changeAmount * itemAmount < 0) ? ctx.getString(R.string.history_description_middle_paid) : ctx.getString(R.string.history_description_middle_loaned);

                int spanStart = builder.length();
                builder.append(firstName).setSpan(new StyleSpan(Typeface.BOLD), spanStart, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(middleText);
                spanStart = builder.length();
                builder.append(secondName).setSpan(new StyleSpan(Typeface.BOLD), spanStart, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            private static final void appendChangeAmount(Context ctx, SpannableStringBuilder builder, double changeAmount){
                int color = (changeAmount < 0) ? Color.RED : ctx.getResources().getColor(R.color.loan_green);
                int spanStart = builder.length();
                builder.append(sCurrencyFormat.format(Math.abs(changeAmount)));
                builder.setSpan(new ForegroundColorSpan(color), spanStart, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            private static final void appendItemDescription(SpannableStringBuilder builder, String itemDescription){
                builder.append("for ");
                int startSpan = builder.length();
                builder.append(itemDescription).setSpan(new StyleSpan(Typeface.BOLD), startSpan, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            public HistoryDescription(Context ctx, String name, String itemDescription, double changeAmount, double itemAmount){
                spannable = new SpannableStringBuilder();
                appendNamesPortion(ctx, spannable, name, changeAmount, itemAmount);
                spannable.append(' ');
                appendChangeAmount(ctx, spannable, changeAmount);
                spannable.append(' ');
                appendItemDescription(spannable, itemDescription);
            }

            @Override
            public String toString(){
                return spannable.toString();
            }
            @Override
            public <T> T[] getSpans(int start, int end, Class<T> type) {
                return spannable.getSpans(start, end, type);
            }
            @Override
            public int getSpanStart(Object tag) {
                return spannable.getSpanStart(tag);
            }
            @Override
            public int getSpanEnd(Object tag) {
                return spannable.getSpanEnd(tag);
            }
            @Override
            public int getSpanFlags(Object tag) {
                return spannable.getSpanFlags(tag);
            }
            @Override
            public int nextSpanTransition(int start, int limit, Class type) {
                return spannable.nextSpanTransition(start, limit, type);
            }
            @Override
            public int length() {
                return spannable.length();
            }
            @Override
            public char charAt(int index) {
                return spannable.charAt(index);
            }
            @Override
            public CharSequence subSequence(int start, int end) {
                return spannable.subSequence(start, end);
            }
        }
    }

    /* ----------------- End Private Subclasses ----------------------------- */
}
