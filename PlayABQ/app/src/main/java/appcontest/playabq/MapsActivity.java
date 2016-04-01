package appcontest.playabq;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleMap.OnMapLoadedCallback,GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    // park/comm variables:
    // data = raw map read from Json
    // list = list of maps, 1 per park/ctr
    // aliases maps Json field names to their aliases
    private ArrayList<Map<String, Object>> parkList;
    private ArrayList<Map<String, Object>> commList;


    private ClusterManager<ClusterIndicator> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        parkList = Filter.selectParks(Filter.filtered());
        commList = Filter.selectCommCenters(Filter.filtered());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /*
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        GoogleMapAdapter adapter = new GoogleMapAdapter();


        //settup clustering
        mClusterManager = new ClusterManager<ClusterIndicator>(this, mMap);
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mClusterManager.setRenderer(new MyClusterRenderer(this,mMap,mClusterManager));

        // if user isn't tracked this will defaultly be the center of ABQ
        LatLng center = new LatLng(Util.getUserLocation().getLatitude(),Util.getUserLocation().getLongitude());

        //found by trial + error
        float zoom = 10.6f;

        //zoom closer to user if user is tracked
        if(Util.isTrackingUser)
        {
            zoom=12;
        }
        /* zoom level determined by trial and error */
        CameraUpdate move = CameraUpdateFactory.newLatLngZoom(center, zoom);

        /* let the map register the move before we try to add polygons and markers,
         * rendering may be threaded and occur before we get the map centered on ABQ */
        mMap.moveCamera(move);

        markAllCenters();
        markAllParks();
        markUserLocation();

        /* onMapLoaded does nothing at the moment, but we'll probably forget to add this if we start
         * using it again, so let's leave this here for now */
        mMap.setOnMapLoadedCallback(this);

    }


    /**
     * Unused at the moment.  May be useful later.  Called once the Map is loaded and rendering.
     */
    @Override
    public void onMapLoaded() {
        /*
        Old abq rectangular bounds saved in case we need them...
        LatLngBounds abqBounds =
                new LatLngBounds(new LatLng(34.946766, -106.471163), new LatLng(35.218054, -106.881796));
        */
    }


    // puts polygons on GoogleMap, also adds them to polygon-parkname map
    private void getPolys() {

        HashMap<Polygon, String> polygonMap = new HashMap<>();
        for (Map park : parkList) {

            String name = (String) park.get("PARKNAME");

            List<PolygonOptions> polyList = Util.getParkPolyOpt(park);
            for (PolygonOptions polyOpt : polyList) {
                Polygon newPoly = mMap.addPolygon(polyOpt);
                polygonMap.put(newPoly, name);
            }
        }
    }

    // puts markers for community centers on map
    private void markAllCenters() {
        for (Map ctr : commList) {
            MarkerOptions mkrOpt = (MarkerOptions) ctr.get("MarkerOptions");

            double lat = Util.getLat(ctr);
            double lon = Util.getLon(ctr);

            ClusterIndicator clusterIndicator = new ClusterIndicator(lat,lon,Util.getMarker(ctr, this));
            mClusterManager.addItem(clusterIndicator);
        }
    }

    private void markAllParks() {
        for (Map park : parkList) {
            MarkerOptions mkrOpt = (MarkerOptions) park.get("MarkerOptions");

            double lat = Util.getLat(park);
            double lon = Util.getLon(park);

            ClusterIndicator clusterIndicator = new ClusterIndicator(lat,lon,Util.getMarker(park,this));
            mClusterManager.addItem(clusterIndicator);
        }
    }

    private void markUserLocation() {
        if(Util.isTrackingUser) {
            MarkerOptions mkrOpt = Util.getUserMkrOpt();
            Marker m = mMap.addMarker(mkrOpt);
            m.showInfoWindow();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (!marker.getTitle().equals("My Location")) {
            Intent intent = new Intent(this, LocationActivity.class);
            HashMap locData = (HashMap) getMarkerData(marker);
            intent.putExtra("data", locData);

            startActivity(intent);
        }
    }

    private Map<String, Object> getMarkerData(Marker m) {
        String name = m.getTitle();
        Map <String, Object> locData = null;
        for (Map map : parkList) {
            if (Util.getName(map).equals(name)) {
                locData = map;
            }
        }
        if (locData == null) {
            for (Map map : commList) {
                if (Util.getName(map).equals(name)) {
                    locData = map;
                }
            }
        }
        return locData;
    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        //@Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        //@Override
        public View getInfoContents(Marker marker) {
            View mContents;
            String title = marker.getTitle();
            if (title==null){
                mContents = null;
            }
            else if (title.equals("My Location")) {
                mContents = getLayoutInflater().inflate(R.layout.my_location_info_layout, null);
                TextView titleUi = ((TextView) mContents.findViewById(R.id.my_title_window));
                titleUi.setText(title);
            }
            else {
                mContents = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                TextView titleUi = ((TextView) mContents.findViewById(R.id.title_window));
                titleUi.setText(title);
                int info = R.drawable.ic_info;

                ((ImageView) mContents.findViewById(R.id.info)).setImageResource(info);
                ((ImageView) mContents.findViewById(R.id.info)).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
            return mContents;
        }
    }

    class MyClusterRenderer extends DefaultClusterRenderer<ClusterIndicator> {

        public MyClusterRenderer(Context context, GoogleMap map,
                               ClusterManager<ClusterIndicator> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(ClusterIndicator item, MarkerOptions markerOptions) {
            markerOptions.icon(item.getIcon());
            markerOptions.title(item.getTitle());
            super.onBeforeClusterItemRendered(item, markerOptions);
        }
    }
}


