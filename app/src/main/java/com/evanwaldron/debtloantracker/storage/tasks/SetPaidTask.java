package com.evanwaldron.debtloantracker.storage.tasks;

import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.os.Handler;
import android.util.Pair;

import com.evanwaldron.debtloantracker.storage.DbBatchTask;
import com.evanwaldron.debtloantracker.storage.operations.update.SetPaidOperation;

import java.util.ArrayList;

/**
 * Created by Evan on 9/26/14 1:29 PM.
 */
public class SetPaidTask extends DbBatchTask<Pair<Integer, Boolean>> {
    public SetPaidTask(ContentResolver resolver, Handler onFinish) {
        super(resolver, onFinish);
    }

    @Override
    protected ArrayList<ContentProviderOperation> prepareOperations(Pair<Integer, Boolean>... params) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>(params.length);

        for(Pair<Integer, Boolean> info : params){
            operations.add(SetPaidOperation.newOperation(info));
        }

        return operations;
    }

    @Override
    protected boolean resultsAreValid(ContentProviderResult[] results) {
        return true;
    }
}
