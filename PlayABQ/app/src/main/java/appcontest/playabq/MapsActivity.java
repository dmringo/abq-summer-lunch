package appcontest.playabq;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private GoogleMap mMap;
    // park/comm variables:
    // data = raw map read from Json
    // list = list of maps, 1 per park/ctr
    // aliases maps Json field names to their aliases
    private List<Map> parkList;
    private List<Map> commList;



    // polygonMap has park polygons as keys, their names as values
    // for use in click listener
    private Map<Polygon, String> polygonMap;
    private Map<Marker, String> markerMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        Intent intent = getIntent();
        parkList = (List<Map>) intent.getSerializableExtra(String.valueOf(R.string.PARKLIST));
        commList = (List<Map>) intent.getSerializableExtra(String.valueOf(R.string.COMMLIST));


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*for (Map park : parkList)  {
            System.out.println(park.get("PARKNAME"));
            Map coords = (Map) park.get("geometry");
            for (Object key : coords.keySet()) {
                List coordlist = (List) coords.get(key);
                for (Object item : coordlist) {
                    System.out.println(item.toString()); // returns array of points for poly
                }
            }
        }*/

        /**
         try {
         TestJackson.testJackson(getAssets().open(TestJackson.TEST_FILE));
         } catch (IOException e) {
         e.printStackTrace();
         }
         **/
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
        mMap.setOnMapClickListener(GoogleMapAdapter.DEBUG_IMPL);
        mMap.setOnPolygonClickListener(GoogleMapAdapter.DEBUG_IMPL);
        mMap.setOnMarkerClickListener(GoogleMapAdapter.DEBUG_IMPL);

        GoogleMapAdapter adapter = new GoogleMapAdapter();
        adapter.setAllListeners(mMap);

        /*
        for (Polygon poly : polygonMap.keySet()) {
            System.out.println(polygonMap.get(poly));
            System.out.println(poly.getPoints());
        }*/


        /* roughly I-25 and I-40 */
        final LatLng center = new LatLng(35.10998051198137, -106.61751497536898);
        /* zoom level determined by trial and error */
        final float zoom = 10.6f;
        CameraUpdate move = CameraUpdateFactory.newLatLngZoom(center, zoom);

        /* let the map register the move before we try to add polygons and markers,
         * rendering may be threaded and occur before we get the map centered on ABQ */
        mMap.moveCamera(move);

        //getPolys();
        markAllCenters();

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


    private void markAllCenters() {
        for (Map ctr : commList) {
            Map geometry = (Map) ctr.get("geometry");
            //double lat = (double) geometry.get("y");
            //double lon = (double) geometry.get("x");
            String name = (String) ctr.get("CENTERNAME");
            MarkerOptions mkrOpt = Util.getCenterMkrOpt(name, geometry);
            Marker m = mMap.addMarker(mkrOpt);
            markerMap.put(m, name);
        }
    }



}


