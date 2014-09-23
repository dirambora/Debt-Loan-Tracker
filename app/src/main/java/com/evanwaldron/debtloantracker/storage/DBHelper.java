package com.evanwaldron.debtloantracker.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Evan on 8/18/2014.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = DBHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "debts.db";
    private static final int DATABASE_VERSION = 1;

    private static final String ITEM_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + DebtStorage.Items.TABLE_NAME + " (" +
            DebtStorage.Items.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DebtStorage.Items.PERSON_ID + " TEXT, " +
            DebtStorage.Items.AMOUNT + " DECIMAL(10,2), " +
            DebtStorage.Items.DESCRIPTION + " TEXT, " +
            DebtStorage.Items.PAYED + " BOOLEAN DEFAULT 0, " +
            DebtStorage.Items.DATE_CREATED + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            DebtStorage.Items.DATE_PAYED + " TIMESTAMP DEFAULT NULL)";

    private static final String PERSON_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + DebtStorage.People.TABLE_NAME + "(" +
            DebtStorage.People.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DebtStorage.People.NAME + " TEXT, " +
            DebtStorage.People.CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    private static final String CHANGES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + DebtStorage.Changes.TABLE_NAME + " (" +
            DebtStorage.Changes.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            DebtStorage.Changes.PERSON_ID + " INTEGER, " +
            DebtStorage.Changes.ITEM_ID + " INTEGER, " +
            DebtStorage.Changes.AMOUNT + " DECIMAL(10,2), " +
            DebtStorage.Changes.DATE + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    private static final String PERSON_INFO_VIEW_CREATE = "CREATE VIEW IF NOT EXISTS " + DebtStorage.PersonInfo.VIEW_NAME + " AS SELECT " +
            DebtStorage.People.withTablePrefix(DebtStorage.People.ID) + ", " +
            DebtStorage.People.withTablePrefix(DebtStorage.People.NAME) + ", " +
            DebtStorage.People.withTablePrefix(DebtStorage.People.CREATED_AT) + ", " +
            "SUM(CASE WHEN " + DebtStorage.Items.withTablePrefix(DebtStorage.Items.PAYED) + "=0 THEN " + DebtStorage.Items.withTablePrefix(DebtStorage.Items.AMOUNT) + " ELSE 0 END) AS " + DebtStorage.PersonInfo.NET_BALANCE + ", " +
            "COUNT(CASE WHEN " + DebtStorage.Items.withTablePrefix(DebtStorage.Items.PAYED) + "=0 THEN 1 END) AS " + DebtStorage.PersonInfo.NUM_UNPAYED + ", " +
            "COUNT(CASE WHEN " + DebtStorage.Items.withTablePrefix(DebtStorage.Items.PAYED) + "=1 THEN 1 END) AS " + DebtStorage.PersonInfo.NUM_PAYED + " " +
            "FROM " + DebtStorage.People.TABLE_NAME + " LEFT JOIN " + DebtStorage.Items.TABLE_NAME + " ON " + DebtStorage.People.withTablePrefix(DebtStorage.People.ID) + " = " + DebtStorage.Items.withTablePrefix(DebtStorage.Items.PERSON_ID) + " GROUP BY " + DebtStorage.People.NAME;

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
        db.execSQL("DROP TABLE IF EXISTS " + DebtStorage.Items.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DebtStorage.People.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DebtStorage.Changes.TABLE_NAME);
        this.onCreate(db);
    }
}
