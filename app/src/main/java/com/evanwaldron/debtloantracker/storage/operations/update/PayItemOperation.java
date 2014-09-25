package com.evanwaldron.debtloantracker.storage.operations.update;

import android.content.ContentProviderOperation;

import com.evanwaldron.debtloantracker.storage.Storage;

/**
 * Created by Evan on 9/25/2014.
 */
public class PayItemOperation {

    private static final String PAY_ITEM_SELECTION = Storage.Items.ID + " = ?";

    public static ContentProviderOperation newOperation(int itemId){
        return ContentProviderOperation.newDelete(Storage.Items.CONTENT_URI)
                .withSelection(PAY_ITEM_SELECTION, new String[]{ Integer.toString(itemId) })
                .withValue(Storage.Items.PAYED, 1)
                .withExpectedCount(1)
                .build();
    }
}
