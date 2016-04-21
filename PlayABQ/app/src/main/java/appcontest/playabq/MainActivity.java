package appcontest.playabq;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ToggleButton;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        MenuItem.OnMenuItemClickListener
{

    private final String CLASSTAG = getClass().getSimpleName();
    private boolean parkFiltersOpen = false;
    private boolean commFiltersOpen = false;
    private MapListAdapter adapter;
    private ListView listView;
    private Filter.FilterOpts filterOpts = Filter.FilterOpts.newDefault();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            initNavView(navigationView);
        }


        listView = (ListView) findViewById(R.id.list_view);

        adapter = new MapListAdapter(this);
        listView.setAdapter(adapter);
        listView.refreshDrawableState();
        drawer.openDrawer(GravityCompat.START);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initNavView(NavigationView nv) {
        Menu menu = nv.getMenu();

        String[] parkOpts = getResources().getStringArray(R.array.Park_Filter_Options);
        Arrays.sort(parkOpts);
        for(String p : parkOpts)
        {
            AppCompatCheckBox box = (AppCompatCheckBox) getLayoutInflater()
                    .inflate(R.layout.filter_checkbox, null);
            box.setTag(R.id.filter_drawer, R.id.park_filters_grp);
            box.setTag(R.id.feature_checkbox, p);

            menu.add(R.id.park_filters_grp, Menu.NONE, getResources().getInteger(R.integer.parkFilterOrder),p)
                    .setActionView(box)
                    .setOnMenuItemClickListener(this)
                    .setVisible(false);
        }

        String[] commOpts = getResources().getStringArray(R.array.CC_Filter_Options);
        Arrays.sort(commOpts);
        for(String cc : commOpts)
        {
            AppCompatCheckBox box =(AppCompatCheckBox) getLayoutInflater()
                    .inflate(R.layout.filter_checkbox, null);
            box.setTag(R.id.filter_drawer, R.id.comm_filters_grp);
            box.setTag(R.id.feature_checkbox, cc);

            menu.add(R.id.comm_filters_grp, Menu.NONE, getResources().getInteger(R.integer.commFilterOrder),cc)
                    .setActionView(box)
                    .setOnMenuItemClickListener(this)
                    .setVisible(false);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.go_to_maps) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }
        String key = Util.keyByAlias((String) item.getTitle());
        if (key != null) {
            Log.i("OptItem", key);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }


    /**
     * Called when one of the filter group headers is clicked.  Expands all the filtering
     * options for Parks or Comm Centers
     *
     * @param v
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void metaItemClick(MenuItem v)
    {
        boolean menuState = false;
        int id = 0;
        switch (v.getItemId())
        {
            case R.id.park_filter_header:
                id = R.id.park_filters_grp;
                menuState = parkFiltersOpen = !parkFiltersOpen;
                break;
            case R.id.comm_filter_header:
                id = R.id.comm_filters_grp;
                menuState = commFiltersOpen = !commFiltersOpen;
                break;

            /* select show parks or community centers -
               perform click on the checkbox and let it handle the logic */
            case R.id.show_parks_item:
            case R.id.show_comms_item:
                Log.i(CLASSTAG,"show group metaItemClick");
                v.getActionView().performClick();
                return;
            default:
                /*something bad is happening? */
                Log.e(CLASSTAG, "bad menu item id in metaItemClick: " + v.getItemId());
        }

        hideShowMenuGroup(id, menuState);
        AppCompatImageView iv = (AppCompatImageView) v.getActionView();
        iv.setImageResource(menuState ?
                R.drawable.ic_remove_24dp : R.drawable.ic_add_24dp);
    }


    /**
     * This is only called on feature-specific filter items, and simply delegates to the
     * corresponding checkbox's onClick handler (filterBoxClick)
     *
     * @param item The menu item that was invoked.
     * @return Return true to consume this click and prevent others from
     * executing.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onMenuItemClick(MenuItem item) {

        /* calls filterBoxClick below*/
        item.getActionView().performClick();
        return true;
    }

    /**
     * Called when Parks or Community Centers are selected by checkbox in the top level
     * filter options of the Nav Drawer
     *
     * @param v View (really an AppCompatCheckbox) that was clicked
     */
    public void groupToggle(View v)
    {
        Log.i(CLASSTAG, "groupToggle");
        AppCompatCheckBox cb = (AppCompatCheckBox) v;
        switch(cb.getId())
        {
            case R.id.comm_checkbox:
                hideShowMenuGroup(R.id.comm_filter_header_grp, cb.isChecked());
                filterOpts.getGroupByTag(R.id.comm_filters_grp).toggle();
                if(commFiltersOpen) hideShowMenuGroup(R.id.comm_filters_grp,cb.isChecked());
                break;

            case R.id.park_checkbox:
                hideShowMenuGroup(R.id.park_filter_header_grp, cb.isChecked());
                filterOpts.getGroupByTag(R.id.park_filters_grp).toggle();
                if(parkFiltersOpen) hideShowMenuGroup(R.id.park_filters_grp,cb.isChecked());
                break;

            default:
                Log.e(CLASSTAG, "bad group toggle id? - " + cb.getId());
                return;
        }
        Filter.filter(filterOpts);
        adapter.onChanged();

    }

    public void filterModeToggle(View v)
    {
        ToggleButton button = (ToggleButton) v;
        filterOpts.toggleMode();
        Filter.filter(filterOpts);
        adapter.onChanged();
    }


    public void filterBoxClick(View v){

        AppCompatCheckBox box = (AppCompatCheckBox) v;

        /* identify which group the checkbox is part of*/
        int groupTag = (int) box.getTag(R.id.filter_drawer);

        String reqTag = (String) box.getTag(R.id.feature_checkbox);

        Log.i(CLASSTAG, String.format("groupTag = %s, reqTag = %s", groupTag, reqTag));

        Filter.FilterGroup group = filterOpts.getGroupByTag(groupTag);
        {
            Log.i(CLASSTAG, reqTag);
            filterOpts.toggleReq(group.tag, Util.keyByAlias(reqTag));
        }

        Filter.filter(filterOpts);
        adapter.onChanged();

    }


    /**
     * Helper to expand/contract menus dynamically
     *
     * @param groupTag menu group ID
     * @param open whether the menu should be open or closed
     */
    private void hideShowMenuGroup(int groupTag, boolean open) {
        NavigationView nav = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = nav.getMenu();
        Log.i(CLASSTAG,
                String.format("hideShowMenuGroup(groupTag = %d, menuState = %s, menusize = %d",
                        groupTag, open, menu.size()));

        menu.setGroupEnabled(groupTag, open);
        menu.setGroupVisible(groupTag, open);

    }

    public void goToMap(MenuItem item) {
        startActivity(new Intent(this,MapsActivity.class));
    }
}
