package appcontest.playabq;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    private GoogleMap mMap;
    private Map parkData;
    private List<Map> parkList;
    private Map<String,String> aliases;
    private Map<Polygon, String> polygonMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        parkData = JsonParser.getParkData(this);
        parkList = JsonParser.getParkList(parkData);
        aliases = JsonParser.getParkAliases(parkData);

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

        /***
        try {
            TestJackson.testJackson(getAssets().open(TestJackson.TEST_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ***/
    }


    /**
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
        polygonMap = getPolys(mMap);
        /*
        for (Polygon poly : polygonMap.keySet()) {
            System.out.println(polygonMap.get(poly));
            System.out.println(poly.getPoints());
        }*/
        mMap.setOnMapLoadedCallback(this);
    }

    // this method isn't working
    @Override
    public void onMapLoaded() {
        LatLngBounds abqBounds = new LatLngBounds(
                new LatLng(34.946766, -106.471163), new LatLng(35.218054, -106.881796));
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(abqBounds, 0));
    }

    private Map getPolys(GoogleMap gmap) {
        HashMap<Polygon,String> polygonMap = new HashMap<>();
        for (Map park : parkList)  {
            String name = (String) park.get("PARKNAME");
            Map coords = (Map) park.get("geometry");
            // get arraylist of arraylists representing contiguous areas
            for (Object key : coords.keySet()) {
                List polys = (List) coords.get(key);
                for (Object poly : polys) {
                    //poly is arraylist of 2-element arraylists
                    // representing closed polygon
                    ArrayList coordList = (ArrayList) poly;
                    List <LatLng> vtxList = new ArrayList<LatLng>();
                    for (Object coord : coordList) {
                        ArrayList<Double> doubleList = (ArrayList) coord;
                        double lat = doubleList.get(1);
                        double lon = doubleList.get(0);
                        LatLng latlon = new LatLng(lat, lon);
                        vtxList.add(latlon);
                    }
                    PolygonOptions polyOpt = new PolygonOptions()
                            .addAll(vtxList)
                            .strokeColor(Color.RED)
                            .fillColor(Color.BLUE);
                    Polygon newPoly = gmap.addPolygon(polyOpt);
                    polygonMap.put(newPoly, name);
                }
               }
            }
            return polygonMap;
        }

    }


