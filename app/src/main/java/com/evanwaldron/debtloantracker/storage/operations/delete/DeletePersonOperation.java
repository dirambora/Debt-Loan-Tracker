package com.evanwaldron.debtloantracker.storage.operations.delete;

import android.content.ContentProviderOperation;

import com.evanwaldron.debtloantracker.storage.Storage;

/**
 * Created by Evan on 9/25/14.
 */
public final class DeletePersonOperation {
    private DeletePersonOperation(){}

    private static final String DELETE_PERSON_SELECTION = Storage.People.ID + " = ?";

    public static ContentProviderOperation newOperation(int personId){
        ContentProviderOperation.Builder builder = ContentProviderOperation.newDelete(Storage.People.CONTENT_URI);

        builder.withSelection(DELETE_PERSON_SELECTION, new String[]{ Integer.toString(personId) });
        builder.withExpectedCount(1);

        return builder.build();
    }
}
