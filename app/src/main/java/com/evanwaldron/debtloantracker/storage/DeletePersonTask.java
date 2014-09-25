package com.evanwaldron.debtloantracker.storage;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import com.evanwaldron.debtloantracker.storage.operations.delete.DeleteAllChangesOperation;
import com.evanwaldron.debtloantracker.storage.operations.delete.DeleteAllItemsOperation;
import com.evanwaldron.debtloantracker.storage.operations.delete.DeletePersonOperation;

import java.util.ArrayList;

/**
 * Created by Evan on 9/25/14.
 */
public class DeletePersonTask extends AsyncTask<Integer, Void, ContentProviderResult[]> {

    private final ContentResolver resolver;
    private final Handler onFinish;

    public DeletePersonTask(final ContentResolver resolver, final Handler onFinish){
        this.resolver = resolver;
        this.onFinish = onFinish;
    }

    @Override
    protected ContentProviderResult[] doInBackground(Integer... params) {
        if(params.length == 0){
            return null;
        }
        int personId = params[0];

        ArrayList<ContentProviderOperation> operations = new ArrayList<>(3);

        ContentProviderOperation deleteChanges = DeleteAllChangesOperation.newOperation(personId);
        ContentProviderOperation deleteItems = DeleteAllItemsOperation.newOperation(personId);
        ContentProviderOperation deletePerson = DeletePersonOperation.newOperation(personId);

        operations.add(deleteChanges);
        operations.add(deleteItems);
        operations.add(deletePerson);

        ContentProviderResult[] results;

        try{
            results = resolver.applyBatch(DebtContentProvider.AUTH, operations);
        } catch (RemoteException | OperationApplicationException e) {
            return null;
        }

        return results;
    }

    public static final int MESSAGE_COMPLETED = 1;

    @Override
    protected void onPostExecute(ContentProviderResult[] results){
        if(results == null){
            sendMessage("Failure");
        }
        String message = "Success";
        for(ContentProviderResult result : results){
            if(result.count <= 0){
                message = "Failure";
                break;
            }
        }
        sendMessage(message);
    }

    private void sendMessage(String message){
        Message msg = onFinish.obtainMessage(MESSAGE_COMPLETED, message);
        onFinish.dispatchMessage(msg);
    }
}
