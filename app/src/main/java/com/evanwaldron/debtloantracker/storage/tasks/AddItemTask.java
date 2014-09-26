package com.evanwaldron.debtloantracker.storage.tasks;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Handler;

import com.evanwaldron.debtloantracker.storage.DbBatchTask;
import com.evanwaldron.debtloantracker.storage.Storage;
import com.evanwaldron.debtloantracker.storage.operations.add.AddChangeOperation;
import com.evanwaldron.debtloantracker.storage.operations.add.AddItemOperation;
import com.evanwaldron.debtloantracker.storage.operations.add.AddPersonOperation;

import java.util.ArrayList;

/**
 * Created by Evan on 9/26/14 1:00 PM.
 */
public class AddItemTask extends DbBatchTask<AddItemTask.TaskInfo>{

    public static final class TaskInfo{
        public int personId;
        public double amount;
        public String description, name;
    }

    public AddItemTask(ContentResolver resolver, Handler onFinish) {
        super(resolver, onFinish);
    }

    @Override
    protected ArrayList<ContentProviderOperation> prepareOperations(TaskInfo... params) {
        // Create ArrayList to hold ContentProviderOperations
        ArrayList<ContentProviderOperation> operations = new ArrayList<>(3 * params.length);

        for(TaskInfo info : params){
            // Set AddItemOperation parameters
            AddItemOperation.Params addItemParams = new AddItemOperation.Params();
            addItemParams.amount = info.amount;
            addItemParams.description = info.description;

            // Get person ID or create AddPersonOperation accordingly
            if(info.personId <= 0 && (addItemParams.personId = getPersonId(info.name)) <= 0){
                operations.add(AddPersonOperation.newOperation(info.name));
                addItemParams.personIdBackRef = operations.size() - 1;
            }

            // Create AddItemOperation
            operations.add(AddItemOperation.newOperation(addItemParams));

            // Configure and create AddChangeOperation
            AddChangeOperation.Params addChangeParams = new AddChangeOperation.Params();
            addChangeParams.changeAmount = addItemParams.amount;
            addChangeParams.personId = addItemParams.personId;
            addChangeParams.personIdBackRef = addItemParams.personIdBackRef;
            addChangeParams.itemIdBackRef = operations.size() - 1;

            operations.add(AddChangeOperation.newOperation(addChangeParams));
        }
        return operations;
    }

    @Override
    protected boolean resultsAreValid(ContentProviderResult[] results) {
        for(ContentProviderResult result : results){
            if(result.uri == null){
                return false;
            }
        }
        return true;
    }


    private static final String[] GET_PERSON_ID_PROJECTION = { Storage.People.ID };
    private static final String GET_PERSON_ID_SELECTION = Storage.People.NAME + " = ?";

    private int getPersonId(String name){

        Cursor cursor = resolver.query(Storage.People.CONTENT_URI, GET_PERSON_ID_PROJECTION, GET_PERSON_ID_SELECTION, new String[]{ name }, null);
        if(cursor.getCount() == 0){
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }
}
