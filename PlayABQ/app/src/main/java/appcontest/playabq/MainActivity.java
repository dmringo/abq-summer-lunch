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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MenuItem.OnMenuItemClickListener {

    private boolean parkFiltersOpen = false;
    private boolean commFiltersOpen = false;


    @Override
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

        Map parkData = JsonParser.getParkData(this);
        Map commData = JsonParser.getCommData(this);

        Filter.init(JsonParser.getCommList(commData), JsonParser.getParkList(parkData));

        ListView lv = (ListView) findViewById(R.id.list_view);
        lv.setAdapter(new MapListAdapter(this,Filter.filtered()));

        drawer.openDrawer(GravityCompat.START);
    }

    public void expandFilters(MenuItem item)
    {

        NavigationView nav = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = nav.getMenu();

        

        if(item.getTitle() == getString(R.string.park_filter_title)) {

            menu.setGroupEnabled(R.id.park_filters, !parkFiltersOpen);
            menu.setGroupVisible(R.id.park_filters, !parkFiltersOpen);
            parkFiltersOpen = !parkFiltersOpen;
        }
        if(item.getTitle() == getString(R.string.comm_filter_title)) {

            menu.setGroupEnabled(R.id.comm_filters, !commFiltersOpen);
            menu.setGroupVisible(R.id.comm_filters, !commFiltersOpen);
            commFiltersOpen = !commFiltersOpen;
        }

            
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initNavView(NavigationView nv) {
        Menu menu = nv.getMenu();
        MenuItem item;
        int i = 0;
        for(String p : getResources().getStringArray(R.array.Park_Filter_Options))
        {
            menu.add(R.id.park_filters, Menu.NONE, R.integer.parkFilterOrder,p)
                    .setActionView(new AppCompatCheckBox(this))
                    .setOnMenuItemClickListener(this)
                    .setVisible(false);
        }
        for(String cc : getResources().getStringArray(R.array.CC_Filter_Options))
        {
            menu.add(R.id.comm_filters, Menu.NONE, R.integer.commFilterOrder,cc)
                    .setActionView(new AppCompatCheckBox(this))
                    .setOnMenuItemClickListener(this)
                    .setVisible(false);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
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
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.go_to_maps) {
            Log.i("Main", "go_to_maps");
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }
//        else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    protected void onStart() {
                super.onStart();
    }

    protected void onStop() {
        //mGoogleApiClient.disconnect();
        super.onStop();
    }

    protected void onDestroy() {
        //stopLocationUpdates();
        super.onDestroy();
    }

    /**
     * Called when a menu item has been invoked.  This is the first code
     * that is executed; if it returns true, no other callbacks will be
     * executed.
     *
     * @param item The menu item that was invoked.
     * @return Return true to consume this click and prevent others from
     * executing.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        item.getActionView().performClick();
        return false;
    }
}
