package com.evanwaldron.debtloantracker.storage.operations;

import android.content.ContentProviderOperation;

import com.evanwaldron.debtloantracker.storage.Storage;

/**
 * Created by Evan on 9/25/14.
 */
public final class DeleteAllItemsOperation {

    private DeleteAllItemsOperation(){}

    private static final String DELETE_ALL_ITEMS_SELECTION = Storage.Items.PERSON_ID + " = ?";

    public static ContentProviderOperation newOperation(int personId){
        ContentProviderOperation.Builder builder = ContentProviderOperation.newDelete(Storage.Items.CONTENT_URI);

        builder.withSelection(DELETE_ALL_ITEMS_SELECTION, new String[]{ Integer.toString(personId) });

        return builder.build();
    }
}
