package appcontest.playabq;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = getClass().getSimpleName();
    GoogleApiClient mGoogleApiClient = null;
    final int FINE_LOCATION_ACCESS_REQUEST=0;
    private LocationListener locationListener;
    private boolean parkFiltersOpen = false;
    private boolean commFiltersOpen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Util.setUserLocation(Util.getDefaultLocation());
        Util.isTrackingUser = false;

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
        initNavView(navigationView);

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        Map parkData = JsonParser.getParkData(this);
        Map commData = JsonParser.getCommData(this);

        Filter.init(JsonParser.getCommList(commData), JsonParser.getParkList(parkData));




        /* David's fuckery begins here.  Remove this comment before release! */

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

    private void initNavView(NavigationView nv) {

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("LOCATION", "onConnected called");
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Util.setUserLocation(Util.getDefaultLocation());
            Util.isTrackingUser = false;

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "This allows the app to display parks and community " +
                        "centers nearest to you.",
                        Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_ACCESS_REQUEST);
            Log.i("LOCATION", "requested permission");

        } else {
            Log.i("LOCATION", "Don't need to request permission");
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_ACCESS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("LOCATION", "Permission Granted");
                    startLocationUpdates();
                } else {
                    Log.i("LOCATION", "Permission Denied");
                    Toast.makeText(this, "Without location permissions this app can not prioritize" +
                                    " areas nearest to you.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        /*TODO: REMOVE THESE TESTES */
       // ArrayList<String> features = new ArrayList<>();
       // features.add("PLAYAREAS");
       // Filter.intersectGetLocationsWith(features);
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    protected void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }

    /**
     * Location updates if the user allows the permissions for location services
     */
    protected void startLocationUpdates() {
        Log.i("LOCATION", "Starting location updates");
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck==PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, LocationRequest.create(), locationListener=new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.i("LOC-CHANGE", "IT HAPPENED!");
                            if (location!=null) {
                                Util.setUserLocation(location);
                                Util.isTrackingUser = true;
                            }
                            else {
                                Util.setUserLocation(Util.getDefaultLocation());
                                Util.isTrackingUser = false;
                            }
                        }
                    });
        } else {
            Util.setUserLocation(Util.getDefaultLocation());
            Util.isTrackingUser = false;
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, locationListener);
    }


}
