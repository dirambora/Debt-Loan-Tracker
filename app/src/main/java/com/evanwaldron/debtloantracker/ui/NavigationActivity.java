package com.evanwaldron.debtloantracker.ui;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;

import com.evanwaldron.debtloantracker.R;


public class NavigationActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, FragmentManager.OnBackStackChangedListener {

    /**
     * Interface that MUST be implemented by any fragment used in this activity that wishes to make
     * use of the ActionBar.
     */
    public interface ActionBarConfigurer{
        /**
         * Method that will be called by activity when action bar configuration is needed
         * @param actionBar The action bar for this activity
         */
        public void configureActionBar(ActionBar actionBar);
    }

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * Used to configure action bar based on active fragment. For use in {@link #restoreActionBar()}
     */
    private ActionBarConfigurer mCurActionBarConfig = null;

    private int mCurSelectedItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        getFragmentManager().addOnBackStackChangedListener(this);

        if(savedInstanceState != null){
            mCurActionBarConfig = (ActionBarConfigurer) getFragmentManager().findFragmentById(R.id.container);
        }
    }

    /**
     * Replaces the active fragment with Person List Fragment
     */
    private void showPersonListFragment(){
        PersonListFragment fragment = new PersonListFragment();
        mCurActionBarConfig = fragment;
        getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment, PersonListFragment.TAG)
                .commit();
    }

    /**
     * Replaces the active fragment with History Fragment
     */
    private void showHistoryFragment(){
        HistoryFragment fragment = new HistoryFragment();
        mCurActionBarConfig = fragment;
        getFragmentManager().beginTransaction()
                .addToBackStack(null)
                .replace(R.id.container, fragment)
                .commit();
    }

    /**
     * Returns open/closed state of navigation drawer
     * @return true if open, false if closed
     */
    boolean isDrawerOpen(){
        return mNavigationDrawerFragment.isDrawerOpen();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if(position == mCurSelectedItem){
            return;
        }
        mCurSelectedItem = position;
        switch(position){
            case 0:
                showPersonListFragment();
                break;
            case 1:
                showHistoryFragment();
                break;
            case 2:
                SettingsActivity.showSettings(this);
                break;
        }
    }

    /**
     * Configures the action bar based on current active fragment, or defaults if there is no active fragment (should never happen)
     */
    public void restoreActionBar() {
        if(mCurActionBarConfig == null) {
            restoreDefaultActionBar();
        }else{
            mCurActionBarConfig.configureActionBar(getActionBar());
        }
    }

    /**
     * Restores the default action bar for the app
     */
    private void restoreDefaultActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.navigation, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            SettingsActivity.showSettings(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if(mNavigationDrawerFragment.isDrawerOpen()){
            mNavigationDrawerFragment.closeNavigationDrawer();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager manager = getFragmentManager();
        if(manager.getBackStackEntryCount() == 0){
            mCurActionBarConfig = (ActionBarConfigurer) manager.findFragmentById(R.id.container);
            mCurSelectedItem = 0;
        }
    }
}
