package appcontest.playabq;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.clustering.ClusterItem;
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
    private HashMap<String,String> ctrAliases;
    private HashMap<String,String> parkAliases;


    // polygonMap has park polygons as keys, their names as values
    // for use in click listener
    //private Map<Polygon, String> polygonMap;
   // private Map<Marker, String> markerMap = new HashMap<>();

    private ClusterManager<ClusterIndicator> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        parkAliases = (HashMap) intent.getExtras().get("parkAliases");
        ctrAliases = (HashMap) intent.getExtras().get("ctrAliases");
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
        /*mMap.setOnMapClickListener(GoogleMapAdapter.DEBUG_IMPL);
        mMap.setOnPolygonClickListener(GoogleMapAdapter.DEBUG_IMPL);
        mMap.setOnMarkerClickListener(GoogleMapAdapter.DEBUG_IMPL);*/
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        GoogleMapAdapter adapter = new GoogleMapAdapter();
        //adapter.setAllListeners(mMap);

        /*
        for (Polygon poly : polygonMap.keySet()) {
            System.out.println(polygonMap.get(poly));
            System.out.println(poly.getPoints());
        }*/

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
            /*
            String name = (String) park.get("PARKNAME");
            Map coords = (Map) park.get("geometry");
            // get arraylist of arraylists representing contiguous areas
            for (Object key : coords.keySet()) {
                List polys = (List) coords.get(key);
                for (Object poly : polys) {
                    //poly is arraylist of 2-element arraylists
                    // representing closed polygon
                    ArrayList coordList = (ArrayList) poly;
                    List<LatLng> vtxList = new ArrayList<LatLng>();
                    for (Object coord : coordList) {
                        @SuppressWarnings("unchecked") ArrayList<Double> doubleList = (ArrayList) coord;
                        double lat = doubleList.get(1);
                        double lon = doubleList.get(0);
                        LatLng latlon = new LatLng(lat, lon);
                        vtxList.add(latlon);
                    }
                    PolygonOptions polyOpt = new PolygonOptions()
                            .addAll(vtxList)
                            .strokeColor(Color.RED)
                            .fillColor(Color.BLUE)
                            .clickable(true);
                    */
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
            Marker m = mMap.addMarker(mkrOpt);
            double lat = Util.getLat(ctr);
            double lon = Util.getLon(ctr);
            String name = Util.getName(ctr);
            ClusterIndicator clusterIndicator = new ClusterIndicator(lat,lon,Util.getMarker(ctr, this));
            mClusterManager.addItem(clusterIndicator);
        }
    }

    private void markAllParks() {
        for (Map park : parkList) {
            MarkerOptions mkrOpt = (MarkerOptions) park.get("MarkerOptions");
            Marker m = mMap.addMarker(mkrOpt);
            double lat = Util.getLat(park);
            double lon = Util.getLon(park);
            String name = Util.getName(park);
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
            intent.putExtra("ctrAliases",ctrAliases);
            intent.putExtra("parkAliases",parkAliases);
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
            if (title.equals("My Location")) {
                mContents = getLayoutInflater().inflate(R.layout.my_location_info_layout, null);
                TextView titleUi = ((TextView) mContents.findViewById(R.id.my_title_window));
                titleUi.setText(title);
            }
            else {
                mContents = getLayoutInflater().inflate(R.layout.info_window_layout, null);
                TextView titleUi = ((TextView) mContents.findViewById(R.id.title_window));
                titleUi.setText(title);
                int info = R.drawable.ic_comm;
                ((ImageView) mContents.findViewById(R.id.info)).setImageResource(info);
                ((ImageView) mContents.findViewById(R.id.info)).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
            /*int badge;
            Map mrkData = getMarkerData(marker);
            if (Util.isCommCenter(mrkData)) {
                badge = R.mipmap.ic_com_center;
            }
            else {
                badge = R.mipmap.ic_park;
            }
            ((ImageView) mContents.findViewById(R.id.badge)).setImageResource(badge);
            //((ImageView) mContents.findViewById(R.id.badge)).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            */

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


