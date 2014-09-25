package com.evanwaldron.debtloantracker.storage.operations.delete;

import android.content.ContentProviderOperation;

import com.evanwaldron.debtloantracker.storage.Storage;

/**
 * Created by Evan on 9/25/14.
 */
public final class DeleteItemOperation {
    private DeleteItemOperation(){}

    private static final String DELETE_ITEM_SELECTION = Storage.Items.ID + " = ?";

    public static ContentProviderOperation newOperation(int itemId){
        ContentProviderOperation.Builder builder = ContentProviderOperation.newDelete(Storage.Items.CONTENT_URI);

        builder.withSelection(DELETE_ITEM_SELECTION, new String[]{ Integer.toString(itemId) });

        return builder.build();
    }
}
