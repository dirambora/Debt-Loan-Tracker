package com.evanwaldron.debtloantracker.storage;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Evan on 8/18/2014.
 */
public class DebtContentProvider extends ContentProvider {

    private static final String LOG_TAG = DebtContentProvider.class.getSimpleName();

    static final String AUTH = "com.evanwaldron.debtloantracker.content";

    private final static int ITEMS = 1;
    private final static int PEOPLE = 2;
    private final static int CHANGES = 3;
    private final static int PERSON_INFO = 4;
    private final static int HISTORY = 5;

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    private final static UriMatcher uriMatcher;

    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTH, Storage.Items.CONTENT_URI.getLastPathSegment() , ITEMS);
        uriMatcher.addURI(AUTH, Storage.People.CONTENT_URI.getLastPathSegment(), PEOPLE);
        uriMatcher.addURI(AUTH, Storage.Changes.CONTENT_URI.getLastPathSegment(), CHANGES);
        uriMatcher.addURI(AUTH, Storage.PersonInfo.CONTENT_URI.getLastPathSegment(), PERSON_INFO);
        uriMatcher.addURI(AUTH, Storage.History.CONTENT_URI.getLastPathSegment(), HISTORY);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations){
        ContentProviderResult[] result = new ContentProviderResult[operations.size()];

        db.beginTransaction();

        try {
            for (int i = 0; i < operations.size(); i++) {
                ContentProviderOperation operation = operations.get(i);
                result[i] = operation.apply(this, result, i);
            }
            db.setTransactionSuccessful();
        }catch (Exception e){
            Log.e(LOG_TAG, "Error performing database operation: " + e.getMessage());
            return null;
        }finally {
            db.endTransaction();
        }

        return result;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        db = dbHelper.getReadableDatabase();

        Cursor result = null;

        switch(uriMatcher.match(uri)){
            case ITEMS:
                result = db.query(Storage.Items.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PEOPLE:
                result = db.query(Storage.People.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PERSON_INFO:
                result = db.query(Storage.PersonInfo.VIEW_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case HISTORY: {
                String limit = uri.getQueryParameter("limit");
                result = db.query(Storage.History.VIEW_NAME, projection, selection, selectionArgs, null, null, sortOrder, limit);
                break;
            }
            default:
                break;
        }

        if(result != null){
            result.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return result;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) throws SQLiteException{
        db = dbHelper.getWritableDatabase();

        long result = -1;
        Uri[] notificationUris = null;

        switch(uriMatcher.match(uri)){
            case ITEMS:
                result = db.insert(Storage.Items.TABLE_NAME, null, values);
                notificationUris = NOTIFICATION_URIS_ITEMS;
                break;
            case PEOPLE:
                result = db.insert(Storage.People.TABLE_NAME, null, values);
                notificationUris = NOTIFICATION_URIS_PEOPLE;
                break;
            case CHANGES:
                result = db.insert(Storage.Changes.TABLE_NAME, null, values);
                notificationUris = NOTIFICATION_URIS_CHANGES;
                break;
            case PERSON_INFO:
                throw new SQLiteException("PersonInfo URI corresponds to an SQL view, not a table. It can only be queried, not inserted into");
        }

        if(result <= 0){
            Log.e(LOG_TAG, "An error occurred during insertion on " + uri.toString());
            return null;
        }

        if(notificationUris != null){
            ContentResolver resolver = getContext().getContentResolver();
            for(Uri notify : notificationUris){
                resolver.notifyChange(notify, null);
            }
        }

        return Uri.withAppendedPath(uri, Long.toString(result));
    }

    private static final Uri[] NOTIFICATION_URIS_PEOPLE = { Storage.People.CONTENT_URI, Storage.PersonInfo.CONTENT_URI };
    private static final Uri[] NOTIFICATION_URIS_ITEMS = { Storage.Items.CONTENT_URI, Storage.PersonInfo.CONTENT_URI };
    private static final Uri[] NOTIFICATION_URIS_CHANGES = { Storage.Changes.CONTENT_URI };

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int result = 0;

        Uri[] notificationUris = null;

        db = dbHelper.getWritableDatabase();

        switch(uriMatcher.match(uri)){
            case PEOPLE:
                result = db.delete(Storage.People.TABLE_NAME, selection, selectionArgs);
                notificationUris = NOTIFICATION_URIS_PEOPLE;
                break;
            case ITEMS:
                result = db.delete(Storage.Items.TABLE_NAME, selection, selectionArgs);
                notificationUris = NOTIFICATION_URIS_ITEMS;
                break;
            case CHANGES:
                result = db.delete(Storage.Changes.TABLE_NAME, selection, selectionArgs);
                notificationUris = NOTIFICATION_URIS_CHANGES;
                break;
            case PERSON_INFO:
                throw new SQLiteException("PersonInfo URI corresponds to an SQL view, not a table. It can only be queried, not deleted from");
        }

        if(result <= 0){
            Log.e(LOG_TAG, "An error occurred during deletion from " + uri.toString());
            return result;
        }

        if(notificationUris != null){
            ContentResolver resolver = getContext().getContentResolver();
            for(Uri notify : notificationUris){
                resolver.notifyChange(notify, null);
            }
        }


        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int result = 0;

        Uri[] notificationUris = null;

        switch(uriMatcher.match(uri)){
            case ITEMS:
                result = db.update(Storage.Items.TABLE_NAME, values, selection, selectionArgs);
                notificationUris = NOTIFICATION_URIS_ITEMS;
                break;
        }

        if(notificationUris != null){
            ContentResolver resolver = getContext().getContentResolver();
            for(Uri notify : notificationUris){
                resolver.notifyChange(notify, null);
            }
        }

        return result;
    }

}