package appcontest.playabq;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapsInitializer;

import java.util.HashMap;

public class SplashScreenActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient = null;
    final int FINE_LOCATION_ACCESS_REQUEST=0;
    private LocationListener locationListener;
    private boolean hasLoadedGoogleAPI = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MapsInitializer.initialize(getApplicationContext());

        ImageView image = (ImageView) findViewById(R.id.imageView2);
        if (image != null) {
            image.setImageResource(R.drawable.ic_logo);
        }

        /* Initialize static data sources */
        Util.init(this);
        Filter.init(Util.commList, Util.parkList);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        View playButton = findViewById(R.id.button1);
        if (playButton != null) {

            playButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            Button view = (Button) v;
                            view.setBackgroundResource(R.drawable.button_start_pressed);
                            v.invalidate();
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                            getStarted();
                        case MotionEvent.ACTION_CANCEL: {
                            Button view = (Button) v;
                            view.setBackgroundResource(R.drawable.button_start);
                            view.invalidate();
                            break;
                        }
                    }
                    return true;
                }
            });
        }
    }

    /** Called when the user clicks the Send button */
    public void getStarted() {
        // Do something in response to button
        if(hasLoadedGoogleAPI) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "One moment, waiting for location information. Please try again.",
                    Toast.LENGTH_LONG).show();
        }
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
        hasLoadedGoogleAPI=true;
    }

    protected void onStart() {
        mGoogleApiClient.connect();
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

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
