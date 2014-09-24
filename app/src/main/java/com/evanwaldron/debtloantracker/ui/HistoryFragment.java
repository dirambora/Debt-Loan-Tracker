package com.evanwaldron.debtloantracker.ui;

import android.app.ActionBar;
import android.app.ListFragment;

import com.evanwaldron.debtloantracker.R;

/**
 * Created by Evan on 9/23/2014.
 */
public class HistoryFragment extends ListFragment implements NavigationActivity.ActionBarConfigurer{


    @Override
    public void configureActionBar(ActionBar actionBar) {
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.title_section_history);
    }
}
