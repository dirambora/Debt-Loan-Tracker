package com.evanwaldron.debtloantracker.storage.tasks;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.os.Handler;

import com.evanwaldron.debtloantracker.storage.DbBatchTask;
import com.evanwaldron.debtloantracker.storage.operations.delete.DeleteAllChangesOperation;
import com.evanwaldron.debtloantracker.storage.operations.delete.DeleteItemOperation;

import java.util.ArrayList;

/**
 * Created by Evan on 9/26/14 1:21 PM.
 */
public class DeleteItemTask extends DbBatchTask<Integer> {
    public DeleteItemTask(ContentResolver resolver, Handler onFinish) {
        super(resolver, onFinish);
    }

    @Override
    protected ArrayList<ContentProviderOperation> prepareOperations(Integer... params) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>(2 * params.length);
        for(int itemId : params){
            operations.add(DeleteItemOperation.newOperation(itemId));
            operations.add(DeleteAllChangesOperation.newOperationItemId(itemId));
        }
        return operations;
    }

    @Override
    protected boolean resultsAreValid(ContentProviderResult[] results) {
        for(ContentProviderResult result : results){
            if(result.count <= 0){
                return false;
            }
        }
        return true;
    }
}
