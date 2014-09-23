package com.evanwaldron.debtloantracker.ui;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.evanwaldron.debtloantracker.R;

/**
 * Created by Evan on 9/23/14.
 */
public class SettingsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

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

}
