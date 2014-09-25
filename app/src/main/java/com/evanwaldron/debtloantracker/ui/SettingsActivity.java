package com.evanwaldron.debtloantracker.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.evanwaldron.debtloantracker.R;

/**
 * Created by Evan on 9/23/14.
 */
public class SettingsActivity extends Activity{

    public static final String ARG_PARENT_NAME = "parent_name";

    private String mParentName;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mParentName = getIntent().getStringExtra(ARG_PARENT_NAME);

        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment{
        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    private void goBack(){
        Class parent = null;
        try {
            parent = Class.forName(mParentName);
        }catch(ClassNotFoundException e){
            return;
        }

        Intent back = new Intent(this, parent);
        back.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(back);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home){
            goBack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void showSettings(Context from){
        String className = from.getClass().getName();
        Intent intent = new Intent(from, SettingsActivity.class);
        intent.putExtra(ARG_PARENT_NAME, className);
        from.startActivity(intent);
    }

}
