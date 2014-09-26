package com.evanwaldron.debtloantracker.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Evan on 8/18/2014 12:37 PM 12:37 PM.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = DBHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "debts.db";
    private static final int DATABASE_VERSION = 1;

    private static final String ITEM_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + Storage.Items.TABLE_NAME + " (" +
            Storage.Items.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Storage.Items.PERSON_ID + " TEXT, " +
            Storage.Items.AMOUNT + " DECIMAL(10,2), " +
            Storage.Items.DESCRIPTION + " TEXT, " +
            Storage.Items.PAYED + " BOOLEAN DEFAULT 0, " +
            Storage.Items.DATE_CREATED + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            Storage.Items.DATE_PAYED + " TIMESTAMP DEFAULT NULL)";

    private static final String PERSON_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + Storage.People.TABLE_NAME + "(" +
            Storage.People.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Storage.People.NAME + " TEXT, " +
            Storage.People.CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    private static final String CHANGES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + Storage.Changes.TABLE_NAME + " (" +
            Storage.Changes.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Storage.Changes.PERSON_ID + " INTEGER, " +
            Storage.Changes.ITEM_ID + " INTEGER, " +
            Storage.Changes.AMOUNT + " DECIMAL(10,2), " +
            Storage.Changes.DATE + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    private static final String PERSON_INFO_VIEW_CREATE = "CREATE VIEW IF NOT EXISTS " + Storage.PersonInfo.VIEW_NAME + " AS SELECT " +
            Storage.People.withTablePrefix(Storage.People.ID) + ", " +
            Storage.People.withTablePrefix(Storage.People.NAME) + ", " +
            Storage.People.withTablePrefix(Storage.People.CREATED_AT) + ", " +
            "SUM(CASE WHEN " + Storage.Items.withTablePrefix(Storage.Items.PAYED) + "=0 THEN " + Storage.Items.withTablePrefix(Storage.Items.AMOUNT) + " ELSE 0 END) AS " + Storage.PersonInfo.NET_BALANCE + ", " +
            "COUNT(CASE WHEN " + Storage.Items.withTablePrefix(Storage.Items.PAYED) + "=0 THEN 1 END) AS " + Storage.PersonInfo.NUM_UNPAYED + ", " +
            "COUNT(CASE WHEN " + Storage.Items.withTablePrefix(Storage.Items.PAYED) + "=1 THEN 1 END) AS " + Storage.PersonInfo.NUM_PAYED + " " +
            "FROM " + Storage.People.TABLE_NAME + " LEFT JOIN " + Storage.Items.TABLE_NAME + " ON " + Storage.People.withTablePrefix(Storage.People.ID) + " = " + Storage.Items.withTablePrefix(Storage.Items.PERSON_ID) + " GROUP BY " + Storage.People.NAME;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ITEM_TABLE_CREATE);
        Log.i(LOG_TAG, "Debt table created");
        db.execSQL(PERSON_TABLE_CREATE);
        Log.i(LOG_TAG, "Debtor table created");
        db.execSQL(CHANGES_TABLE_CREATE);
        Log.i(LOG_TAG, "Changes table created");
        db.execSQL(PERSON_INFO_VIEW_CREATE);
        Log.i(LOG_TAG, "Person info view created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Storage.Items.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Storage.People.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Storage.Changes.TABLE_NAME);
        this.onCreate(db);
    }
}
