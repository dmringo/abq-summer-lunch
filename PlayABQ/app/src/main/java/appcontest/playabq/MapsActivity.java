package appcontest.playabq;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Map parkData;
    private List<Map> parkList;
    private Map<String,String> aliases;


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

        for (Map park : parkList)  {
            System.out.println(park.get("PARKNAME"));
            Map coords = (Map) park.get("geometry");
            for (Object key : coords.keySet()) {
                List coordlist = (List) coords.get(key);
                for (Object item : coordlist) {
                    System.out.println(item.toString()); // returns array of points for poly
                }
            }
        }
        for (String name : aliases.keySet()) {
            System.out.println(aliases.get(name));
        }

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




        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

}
