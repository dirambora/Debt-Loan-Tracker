package com.evanwaldron.debtloantracker.storage.operations.update;

import android.content.ContentProviderOperation;

import com.evanwaldron.debtloantracker.storage.Storage;

/**
 * Created by Evan on 9/26/14 10:44 PM.
 */
public class PayAllOperation {

    private static final String SELECTION = Storage.Items.PERSON_ID + " = ?";

    public static ContentProviderOperation newOperation(int personId){
        return ContentProviderOperation.newUpdate(Storage.Items.CONTENT_URI)
                .withSelection(SELECTION, new String[]{ Integer.toString(personId) })
                .withValue(Storage.Items.PAYED, 1)
                .build();
    }
}
