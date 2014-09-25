package com.evanwaldron.debtloantracker.storage;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import com.evanwaldron.debtloantracker.storage.operations.update.PayItemOperation;

import java.util.ArrayList;

/**
 * Created by Evan on 9/25/2014.
 */
public class PayItemTask extends AsyncTask<Integer, Void, ContentProviderResult[]> {

    private final ContentResolver resolver;
    private final Handler onFinish;

    public PayItemTask(final ContentResolver resolver, final Handler onFinish){
        this.resolver = resolver;
        this.onFinish = onFinish;
    }

    @Override
    protected ContentProviderResult[] doInBackground(Integer... params) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>(params.length);
        for(int itemId : params){
            operations.add(PayItemOperation.newOperation(itemId));
        }

        ContentProviderResult[] results;
        try{
            results = resolver.applyBatch(DebtContentProvider.AUTH, operations);
        }catch(OperationApplicationException | RemoteException e){
            return null;
        }

        return results;
    }

    @Override
    protected void onPostExecute(ContentProviderResult[] result){
        if(result == null){
            sendMessage("Failure");
        }else {
            sendMessage("Success");
        }
    }

    private void sendMessage(String message){
        Message msg = onFinish.obtainMessage();
        msg.obj = message;
        onFinish.dispatchMessage(msg);
    }
}
