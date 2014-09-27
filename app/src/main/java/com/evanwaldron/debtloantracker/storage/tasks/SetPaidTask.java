package com.evanwaldron.debtloantracker.storage.tasks;

import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Handler;
import android.util.Pair;

import com.evanwaldron.debtloantracker.storage.DbBatchTask;
import com.evanwaldron.debtloantracker.storage.Storage;
import com.evanwaldron.debtloantracker.storage.operations.add.AddChangeOperation;
import com.evanwaldron.debtloantracker.storage.operations.delete.DeleteAllChangesOperation;
import com.evanwaldron.debtloantracker.storage.operations.update.SetPaidOperation;

import java.util.ArrayList;

/**
 * Created by Evan on 9/26/14 1:29 PM.
 */
public class SetPaidTask extends DbBatchTask<Pair<Integer, Boolean>> {
    public SetPaidTask(ContentResolver resolver, Handler onFinish) {
        super(resolver, onFinish);
    }

    private static final String[] ITEM_PROJECTION = {Storage.Items.PERSON_ID, Storage.Items.AMOUNT};
    private static final String ITEM_SELECTION = Storage.Items.ID + " = ?";

    @Override
    protected ArrayList<ContentProviderOperation> prepareOperations(Pair<Integer, Boolean>... params) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>(2 * params.length);

        for(Pair<Integer, Boolean> info : params){
            operations.add(SetPaidOperation.newOperation(info));
            if(info.second){
                Cursor item = resolver.query(Storage.Items.CONTENT_URI, ITEM_PROJECTION, ITEM_SELECTION, new String[]{ Integer.toString(info.first) }, null);
                item.moveToFirst();
                int personId = item.getInt(0);
                double amount = item.getDouble(1);
                AddChangeOperation.Params changeParams = new AddChangeOperation.Params();
                changeParams.itemId = info.first;
                changeParams.personId = personId;
                changeParams.changeAmount = -1 * amount;
                ContentProviderOperation addChange = AddChangeOperation.newOperation(changeParams);
                operations.add(addChange);
            }else{
                ContentProviderOperation deleteChanges = DeleteAllChangesOperation.newOperationItemId(info.first);
                operations.add(deleteChanges);
            }
        }

        return operations;
    }

    @Override
    protected boolean resultsAreValid(ContentProviderResult[] results) {
        return true;
    }
}
