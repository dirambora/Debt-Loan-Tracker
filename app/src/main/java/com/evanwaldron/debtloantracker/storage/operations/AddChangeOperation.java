package com.evanwaldron.debtloantracker.storage.operations;

import android.content.ContentProviderOperation;
import android.content.ContentValues;

import com.evanwaldron.debtloantracker.storage.Storage;

/**
 * Created by Evan on 9/22/2014.
 */
public final class AddChangeOperation {

    public static final class Params{
        public int personId = -1;
        public int itemId = -1;
        public double changeAmount = 0.0;
        public int personIdBackRef = -1;
        public int itemIdBackRef = -1;

        public ContentValues toContentValues(){
            ContentValues values = new ContentValues();

            values.put(Storage.Changes.PERSON_ID, personId);
            values.put(Storage.Changes.ITEM_ID, itemId);
            values.put(Storage.Changes.AMOUNT, changeAmount);

            return values;
        }
    }

    public static ContentProviderOperation newOperation(Params params){
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(Storage.Changes.CONTENT_URI);

        builder.withValues(params.toContentValues());
        if(params.itemId == -1){
            builder.withValueBackReference(Storage.Changes.ITEM_ID, params.itemIdBackRef);
        }
        if(params.personId == -1){
            builder.withValueBackReference(Storage.Changes.ITEM_ID, params.personIdBackRef);
        }

        return builder.build();
    }

}
