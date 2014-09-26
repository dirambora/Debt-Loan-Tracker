package com.evanwaldron.debtloantracker.storage.operations.delete;

import android.content.ContentProviderOperation;

import com.evanwaldron.debtloantracker.storage.Storage;

/**
 * Created by Evan on 9/25/14.
 */
public final class DeleteAllChangesOperation {
    private DeleteAllChangesOperation(){}

    private static final String DELETE_ALL_CHANGES_PERSON_SELECTION = Storage.Changes.PERSON_ID + " = ?";
    private static final String DELETE_ALL_CHANGES_ITEM_SELECTION = Storage.Changes.ITEM_ID + " = ?";

    public static ContentProviderOperation newOperationPersonId(int personId){
        ContentProviderOperation.Builder builder = ContentProviderOperation.newDelete(Storage.Changes.CONTENT_URI);

        builder.withSelection(DELETE_ALL_CHANGES_PERSON_SELECTION, new String[]{ Integer.toString(personId) });

        return builder.build();
    }

    public static ContentProviderOperation newOperationItemId(int itemId){
        return ContentProviderOperation.newDelete(Storage.Changes.CONTENT_URI)
                .withSelection(DELETE_ALL_CHANGES_ITEM_SELECTION, new String[]{ Integer.toString(itemId) })
                .build();
    }
}
