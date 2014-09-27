package com.evanwaldron.debtloantracker.storage.tasks;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Handler;

import com.evanwaldron.debtloantracker.storage.DbBatchTask;
import com.evanwaldron.debtloantracker.storage.Storage;
import com.evanwaldron.debtloantracker.storage.operations.add.AddChangeOperation;
import com.evanwaldron.debtloantracker.storage.operations.update.PayAllOperation;
import com.evanwaldron.debtloantracker.storage.operations.update.SetPaidOperation;

import java.util.ArrayList;

/**
 * Created by Evan on 9/26/14 10:41 PM.
 */
public class ClearItemsTask extends DbBatchTask<Integer> {

    public ClearItemsTask(ContentResolver resolver, Handler onFinish) {
        super(resolver, onFinish);
    }

    private static final String[] PROJECTION = {Storage.Items.ID, Storage.Items.PERSON_ID, Storage.Items.AMOUNT};
    private static final String SELECTION = Storage.Items.PERSON_ID + " = ? AND " + Storage.Items.PAYED + " = 0";

    @Override
    protected ArrayList<ContentProviderOperation> prepareOperations(Integer... params) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>(2 * params.length);
        for(int personId : params){
            operations.add(PayAllOperation.newOperation(personId));
            Cursor items = resolver.query(Storage.Items.CONTENT_URI, PROJECTION, SELECTION, new String[]{ Integer.toString(personId) }, null);
            if(items.getCount() == 0){ break; }
            items.moveToFirst();
            for(int i = 0; i < items.getCount(); i++){
                AddChangeOperation.Params addChangeParams = new AddChangeOperation.Params();
                addChangeParams.itemId = items.getInt(0);
                addChangeParams.personId = items.getInt(1);
                addChangeParams.changeAmount = -1 * items.getDouble(2);
                operations.add(AddChangeOperation.newOperation(addChangeParams));
                items.moveToNext();
            }
        }
        return operations;
    }

    @Override
    protected boolean resultsAreValid(ContentProviderResult[] results) {
        for(ContentProviderResult result : results){
            if(result.uri == null && result.count <= 0){
                return false;
            }
        }
        return true;
    }
}
