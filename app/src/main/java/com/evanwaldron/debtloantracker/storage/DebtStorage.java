package com.evanwaldron.debtloantracker.storage;

import android.net.Uri;

/**
 * Created by Evan on 8/18/2014.
 */
public final class DebtStorage {
    public static final String AUTH = "com.evanwaldron.debtloantracker.content";

    public final static class Items{

        public static final String TABLE_NAME = "debts";

        public static final String ID = "_id";
        public static final String PERSON_ID = "person_id";
        public static final String AMOUNT = "amount";
        public static final String DATE_CREATED = "created_at";
        public static final String DATE_PAYED = "payed_at";
        public static final String PAYED = "payed";
        public static final String DESCRIPTION = "description";

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTH + "/items");

        public static final String withTablePrefix(String columnName){
            return TABLE_NAME + "." + columnName;
        }
    }

    public static final class People{

        public static final String TABLE_NAME = "people";

        public static final String ID = "_id";
        public static final String NAME = "name";
        public static final String CREATED_AT = "created_at";

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTH + "/people");

        public static final String withTablePrefix(String columnName){
            return TABLE_NAME + "." + columnName;
        }

    }

    public static final class Changes{

        public static final String TABLE_NAME = "changes";

        public static final String ID = "_id";
        public static final String PERSON_ID = "person_id";
        public static final String ITEM_ID = "item_id";
        public static final String AMOUNT = "amount";
        public static final String DATE = "date";

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTH + "/changes");
    }

    public static final class PersonInfo{

        public static final String VIEW_NAME = "person_info";

        public static final String ID = People.ID;
        public static final String NAME = People.NAME;
        public static final String CREATED_AT = People.CREATED_AT;
        public static final String NET_BALANCE = "net_balance";
        public static final String NUM_UNPAYED = "num_unpayed";
        public static final String NUM_PAYED = "num_payed";

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTH + "/debtor_info");

    }


}
