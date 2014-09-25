package com.evanwaldron.debtloantracker.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.evanwaldron.debtloantracker.R;

/**
 * Created by Evan on 9/24/14.
 */
public class PersonDetailActivity extends Activity {


    private PersonDetailFragment mPersonDetailFragment;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        int personId = intent.getIntExtra(PersonDetailFragment.ARG_PERSON_ID, 0);
        String personName = intent.getStringExtra(PersonDetailFragment.ARG_PERSON_NAME);

        ActionBar bar = getActionBar();
        bar.setTitle(personName);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        mPersonDetailFragment = PersonDetailFragment.newInstance(personId, personName);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, mPersonDetailFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.navigation, menu);
        if(mPersonDetailFragment != null){
            mPersonDetailFragment.restoreActionBar(getActionBar());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_settings:
                SettingsActivity.showSettings(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
