package com.evanwaldron.debtloantracker.storage.operations;

import android.content.ContentProviderOperation;

import com.evanwaldron.debtloantracker.storage.DebtStorage;

/**
 * Created by Evan on 9/20/2014.
 */
public final class AddPersonOperation {
    public static ContentProviderOperation newOperation(String name){
        return ContentProviderOperation.newInsert(DebtStorage.People.CONTENT_URI)
                .withValue(DebtStorage.People.NAME, name)
                .build();
    }
}