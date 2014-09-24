package com.evanwaldron.debtloantracker.storage;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import com.evanwaldron.debtloantracker.storage.operations.AddChangeOperation;
import com.evanwaldron.debtloantracker.storage.operations.AddItemOperation;
import com.evanwaldron.debtloantracker.storage.operations.AddPersonOperation;

import java.util.ArrayList;

/**
 * Created by Evan on 9/20/2014.
 */
public final class AddItemTask extends AsyncTask<AddItemTask.TaskInfo, Void, ContentProviderResult[]> {

    private final ContentResolver resolver;
    private final Handler onFinish;

    public AddItemTask(final ContentResolver resolver, final Handler onFinish){
        this.resolver = resolver;
        this.onFinish = onFinish;
    }

    @Override
    protected ContentProviderResult[] doInBackground(TaskInfo... params) {
        if(params.length == 0){ return null; }

        ContentProviderResult[] results;

        TaskInfo info = params[0];

        AddItemOperation.Params addItemParams = new AddItemOperation.Params();
        addItemParams.amount = info.amount;
        addItemParams.description = info.description;

        ArrayList<ContentProviderOperation> operations = new ArrayList<>(3);
        if(info.personId <= 0 && (addItemParams.personId = getPersonId(info.name)) == -1){
            ContentProviderOperation addPerson = AddPersonOperation.newOperation(info.name);
            operations.add(addPerson);
            addItemParams.personIdBackRef = 0;
        }

        ContentProviderOperation addItem = AddItemOperation.newOperation(addItemParams);
        operations.add(addItem);

        AddChangeOperation.Params addChangeParams = new AddChangeOperation.Params();
        addChangeParams.changeAmount = info.amount;
        addChangeParams.personIdBackRef = 0;
        addChangeParams.itemIdBackRef = 1;

        ContentProviderOperation addChange = AddChangeOperation.newOperation(addChangeParams);
        operations.add(addChange);

        try{
            results = resolver.applyBatch(DebtContentProvider.AUTH, operations);
        }catch (OperationApplicationException e){
            e.printStackTrace();
            return null;
        }catch(RemoteException re){
            re.printStackTrace();
            return null;
        }

        return results;
    }

    @Override
    protected void onPostExecute(ContentProviderResult[] results){
        Message msg = onFinish.obtainMessage();
        if(results != null){
            msg.obj = "Success";
        }else{
            msg.obj = "Failure";
        }
        onFinish.dispatchMessage(msg);
    }

    public static final class TaskInfo{
        public int personId;
        public double amount;
        public String description, name;
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
