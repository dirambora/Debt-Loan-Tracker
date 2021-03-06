package com.evanwaldron.debtloantracker.storage.operations.add;

import android.content.ContentProviderOperation;
import android.content.ContentValues;

import com.evanwaldron.debtloantracker.storage.Storage;

/**
 * Created by Evan on 9/20/2014.
 */
public final class AddItemOperation {

    private AddItemOperation(){}

    public static class Params{
        public int personId = -1;
        public double amount = 0.0;
        public String description = null;
        public int personIdBackRef = -1;

        public ContentValues toContentValues(){
            ContentValues vals = new ContentValues();

            vals.put(Storage.Items.PERSON_ID, personId);
            vals.put(Storage.Items.AMOUNT, amount);
            if(description != null){
                vals.put(Storage.Items.DESCRIPTION, description);
            }

            return vals;
        }
    }

    public static ContentProviderOperation newOperation(Params params){
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(Storage.Items.CONTENT_URI);
        builder.withValues(params.toContentValues());
        if(params.personId <= 0){
            builder.withValueBackReference(Storage.Items.PERSON_ID, params.personIdBackRef);
        }
        return builder.build();
    }

}
