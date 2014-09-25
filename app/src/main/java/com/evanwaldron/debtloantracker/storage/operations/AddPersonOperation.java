package com.evanwaldron.debtloantracker.storage.operations;

import android.content.ContentProviderOperation;

import com.evanwaldron.debtloantracker.storage.Storage;

/**
 * Created by Evan on 9/20/2014.
 */
public final class AddPersonOperation {

    private AddPersonOperation(){}

    public static ContentProviderOperation newOperation(String name){
        return ContentProviderOperation.newInsert(Storage.People.CONTENT_URI)
                .withValue(Storage.People.NAME, name)
                .build();
    }
}
