package com.evanwaldron.debtloantracker.storage.operations;

import android.content.ContentProviderOperation;
import android.content.ContentValues;

import com.evanwaldron.debtloantracker.storage.DebtStorage;

/**
 * Created by Evan on 9/20/2014.
 */
public final class AddItemOperation {

    public static class Params{
        public int personId = -1;
        public double amount = 0.0;
        public String description = null;
        public int personIdBackRef = -1;

        public ContentValues toContentValues(){
            ContentValues vals = new ContentValues();

            vals.put(DebtStorage.Items.PERSON_ID, personId);
            vals.put(DebtStorage.Items.AMOUNT, amount);
            if(description != null){
                vals.put(DebtStorage.Items.DESCRIPTION, description);
            }

            return vals;
        }
    }

    public static ContentProviderOperation newOperation(Params params){
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(DebtStorage.Items.CONTENT_URI);
        builder.withValues(params.toContentValues());
        if(params.personId <= 0){
            builder.withValueBackReference(DebtStorage.Items.PERSON_ID, params.personIdBackRef);
        }
        return builder.build();
    }

}
