package com.evanwaldron.debtloantracker.storage;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import java.util.ArrayList;

/**
 * Created by Evan on 9/26/14 12:50 PM.
 */
public abstract class DbBatchTask<T> extends AsyncTask<T, Void, ContentProviderResult[]> {

    private static final int MESSAGE_COMPLETED = 1;

    protected final ContentResolver resolver;
    private final Handler onFinish;

    public DbBatchTask(final ContentResolver resolver, final Handler onFinish){
        this.resolver = resolver;
        this.onFinish = onFinish;
    }

    protected abstract ArrayList<ContentProviderOperation> prepareOperations(T... params);
    protected abstract boolean resultsAreValid(ContentProviderResult[] results);

    @SafeVarargs
    @Override
    protected final ContentProviderResult[] doInBackground(T... params){
        if(params == null || params.length == 0)
            return null;

        ArrayList<ContentProviderOperation> operations = prepareOperations(params);

        ContentProviderResult[] results;

        try{
            results = resolver.applyBatch(DebtContentProvider.AUTH, operations);
        }catch(OperationApplicationException | RemoteException e){
            return null;
        }
        return results;
    }

    @Override
    protected void onPostExecute(ContentProviderResult[] results){
        if(results == null || !resultsAreValid(results)){
            sendMessage("Failure");
        }else{
            sendMessage("Success");
        }
    }

    private void sendMessage(String message){
        Message msg = onFinish.obtainMessage(MESSAGE_COMPLETED, message);
        onFinish.sendMessage(msg);
    }


}
