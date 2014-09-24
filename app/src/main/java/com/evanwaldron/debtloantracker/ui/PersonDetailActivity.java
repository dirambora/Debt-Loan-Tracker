package com.evanwaldron.debtloantracker.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

/**
 * Created by Evan on 9/24/14.
 */
public class PersonDetailActivity extends Activity {

    public static final String ARG_PERSON_NAME = "person_name";
    public static final String ARG_PERSON_ID = "person_id";

    private String mPersonName;
    private int mPersonId;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        mPersonId = intent.getIntExtra(ARG_PERSON_ID, 0);
        mPersonName = intent.getStringExtra(ARG_PERSON_NAME);

        ActionBar bar = getActionBar();
        bar.setTitle(mPersonName);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
