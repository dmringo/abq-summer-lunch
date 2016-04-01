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
import android.view.SubMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MenuItem.OnMenuItemClickListener {

    private boolean parkFiltersOpen = false;
    private boolean commFiltersOpen = false;

    private HashMap<String,String> ctrAliases;
    private HashMap<String,String> parkAliases;

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

        /* Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }*/

        MapsInitializer.initialize(getApplicationContext());
        Map parkData = JsonParser.getParkData(this);
        Map commData = JsonParser.getCommData(this);
        parkAliases = (HashMap) JsonParser.getAliases(parkData);
        ctrAliases = (HashMap) JsonParser.getAliases(commData);

        ArrayList<Map<String, Object>> commList = JsonParser.getCommList(commData);
        for (Map ctr : commList) {
            MarkerOptions mkrOpt = Util.getMarker(ctr, this);
            ctr.put("MarkerOptions", mkrOpt);
        }
        ArrayList<Map<String, Object>> parkList = JsonParser.getParkList(parkData);
        for (Map park : parkList) {
            MarkerOptions mkrOpt = Util.getMarker(park,this);
            park.put("MarkerOptions", mkrOpt);
        }
        Filter.init(commList, parkList);

        ListView lv = (ListView) findViewById(R.id.list_view);
        lv.setAdapter(new MapListAdapter(this,Filter.filtered()));

        drawer.openDrawer(GravityCompat.START);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void expandFilters(MenuItem item)
    {

        NavigationView nav = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = nav.getMenu();

        

        if(item.getTitle() == getString(R.string.park_filter_title)) {
            ((AppCompatImageView)item.getActionView())
            .setImageResource(parkFiltersOpen ?
                    R.drawable.ic_add_24dp :
                    R.drawable.ic_remove_24dp);

            menu.setGroupEnabled(R.id.park_filters, !parkFiltersOpen);
            menu.setGroupVisible(R.id.park_filters, !parkFiltersOpen);
            parkFiltersOpen = !parkFiltersOpen;
        }
        if(item.getTitle() == getString(R.string.comm_filter_title)) {
            ((AppCompatImageView)item.getActionView())
                    .setImageResource(commFiltersOpen ?
                            R.drawable.ic_add_24dp :
                            R.drawable.ic_remove_24dp);

            menu.setGroupEnabled(R.id.comm_filters, !commFiltersOpen);
            menu.setGroupVisible(R.id.comm_filters, !commFiltersOpen);
            commFiltersOpen = !commFiltersOpen;
        }

            
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initNavView(NavigationView nv) {
        Menu menu = nv.getMenu();
        AppCompatImageView iv1 = new AppCompatImageView(this);
        AppCompatImageView iv2 = new AppCompatImageView(this);
        iv1.setImageResource(R.drawable.ic_add_24dp);
        iv2.setImageResource(R.drawable.ic_add_24dp);


        menu.findItem(R.id.park_header).setActionView(iv1);
        menu.findItem(R.id.comm_header).setActionView(iv2);



        for(String p : getResources().getStringArray(R.array.Park_Filter_Options))
        {
            menu.add(R.id.park_filters, Menu.NONE, getResources().getInteger(R.integer.parkFilterOrder),p)
                    .setActionView(new AppCompatCheckBox(this))
                    .setOnMenuItemClickListener(this)
                    .setVisible(false);
        }
        for(String cc : getResources().getStringArray(R.array.CC_Filter_Options))
        {
            menu.add(R.id.comm_filters, Menu.NONE, getResources().getInteger(R.integer.commFilterOrder),cc)
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
            intent.putExtra("ctrAliases",ctrAliases);
            intent.putExtra("parkAliases",parkAliases);
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
