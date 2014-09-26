package com.evanwaldron.debtloantracker.storage.operations.update;

import android.content.ContentProviderOperation;
import android.util.Pair;

import com.evanwaldron.debtloantracker.storage.Storage;

/**
 * Created by Evan on 9/25/2014.
 */
public class SetPaidOperation {

    private static final String SET_PAID_SELECTION = Storage.Items.ID + " = ?";

    public static ContentProviderOperation newOperation(Pair<Integer, Boolean> pair){
        return ContentProviderOperation.newUpdate(Storage.Items.CONTENT_URI)
                .withSelection(SET_PAID_SELECTION, new String[]{ Integer.toString(pair.first) })
                .withValue(Storage.Items.PAYED, pair.second ? 1 : 0)
                .withExpectedCount(1)
                .build();
    }
}
