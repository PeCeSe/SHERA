package no.gruppe2.shera.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import no.gruppe2.shera.R;

// Fragment for interaction and presentation of a navigation drawer.
public class NavigationDrawerFragment extends Fragment {

    // Shared preference that remembers the position of the selected item
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    /*
    * According to Googles design principles the drawer should be visible on launch when the user
    * logs in for the first time.
    * When the user manually removes it, this shared preference tracks this action.
    */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    // A pointer to the current callback instance.
    private NavigationDrawerCallbacks callbacks;

    // A helper that ties the action bar to the navigation drawer.
    private ActionBarDrawerToggle drawerToggle;

    DrawerLayout drawerLayout;
    private ListView drawerListView;
    private View fragmentContainerView;

    private int currentSelectedPosition = 6;
    private boolean fromSavedInstanceState, userLearnedDrawer;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        * The shared preference indicating whether or not the user has shown awareness of the
        * drawer is read in.
        */
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        userLearnedDrawer = sharedPref.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            fromSavedInstanceState = true;
        }

        setRetainInstance(true);

        // Either the default item (0) is selected, or the last selected item.
        selectItem(currentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Choose whether or not this fragment/activity should influence actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        drawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        /*
        * Select which strings should be displayed, the layout/design of the strings and the way
        *the strings are displayed within each clickable row in the navigation drawer.
        */
        drawerListView.setAdapter(new ArrayAdapter<>(
                getActionBar().getThemedContext(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                new String[]{
                        getString(R.string.blank),
                        getString(R.string.create_event_string),
                        getString(R.string.title_activity_events),
                        getString(R.string.chats),
                        getString(R.string.logout),
                }));
        drawerListView.setItemChecked(currentSelectedPosition, true);
        return drawerListView;
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
    }

    /*
    * This method is called to set up the navigation drawer interactions.
    * The fragmentId is the android:id og this fragment and
    * the drawerLayout contains this fragment's UI.
    */
    public void setUp(int fragmentId, final DrawerLayout drawerLayout) {
        drawerLayout.setScrimColor(getResources().getColor(R.color.navigation_drawer_background));
        fragmentContainerView = getActivity().findViewById(fragmentId);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        /*
        * The ActionBarDrawerToggle connects the navigation drawer and
        * the action bar app icon properly.
        */
        drawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }
                getActivity().supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(R.string.main_menu);
                if (!isAdded()) {
                    return;
                }

                if (!userLearnedDrawer) {
                    /*
                    * If the user has manually opened the drawer this preference is stored to
                    * prevent the navigation drawer from auto-showing in the future.
                    */
                    userLearnedDrawer = true;
                    SharedPreferences sharedPref = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sharedPref.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
                getActivity().supportInvalidateOptionsMenu();
            }
        };
        /*
        * If the user has not shown awareness or has not "learned" about the drawer,
        * open it to show them.
        */
        if (!userLearnedDrawer && !fromSavedInstanceState) {
            drawerLayout.openDrawer(fragmentContainerView);
        }

        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });
        drawerLayout.setDrawerListener(drawerToggle);
    }

    private void selectItem(int position) {
        currentSelectedPosition = position;
        if (drawerListView != null) {
            drawerListView.setItemChecked(position, true);
        }
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(fragmentContainerView);
        }
        if (callbacks != null) {
            callbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, currentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        /*
        * If the drawer is open, the global app actions are shown in the action bar.
        * The method showGlobalContextActionBar further down controls the top-left area of the
        * action bar.
        */
        if (drawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.menu_map, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * Updates the action bar to show the global app context,
    * instead of just what's in the current screen.
    */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    // Callback interface which all activities using the NavigationDrawerFragment must implement.
    public static interface NavigationDrawerCallbacks {
        // This is called when an item is selected in the navigation drawer.
        void onNavigationDrawerItemSelected(int position);
    }
}
