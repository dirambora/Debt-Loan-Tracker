package com.evanwaldron.debtloantracker.storage.tasks;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.os.Handler;

import com.evanwaldron.debtloantracker.storage.DbBatchTask;
import com.evanwaldron.debtloantracker.storage.operations.delete.DeleteAllChangesOperation;
import com.evanwaldron.debtloantracker.storage.operations.delete.DeleteAllItemsOperation;
import com.evanwaldron.debtloantracker.storage.operations.delete.DeletePersonOperation;

import java.util.ArrayList;

/**
 * Created by Evan on 9/26/14 1:26 PM.
 */
public class DeletePersonTask extends DbBatchTask<Integer> {
    public DeletePersonTask(ContentResolver resolver, Handler onFinish) {
        super(resolver, onFinish);
    }

    @Override
    protected ArrayList<ContentProviderOperation> prepareOperations(Integer... params) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>(3 * params.length);

        for(int personId : params){
            operations.add(DeleteAllChangesOperation.newOperationPersonId(personId));
            operations.add(DeleteAllItemsOperation.newOperation(personId));
            operations.add(DeletePersonOperation.newOperation(personId));
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
