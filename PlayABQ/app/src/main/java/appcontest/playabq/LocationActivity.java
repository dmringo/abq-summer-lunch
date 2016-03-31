package appcontest.playabq;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private HashMap<String,Object> locData;
    private String locName;
    private boolean isPark;
    private boolean isCtr;
    private LatLng location;
    private ArrayList<String> features;
    private GoogleMap mMap;
    private MapView mapView;
    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Intent intent = getIntent();
        locData = (HashMap<String,Object>)intent.getSerializableExtra("data");
        if (Util.isCommCenter(locData)) {
            isCtr = true;
        }
        else {
            isPark = true;
        }


        /*
        features = new String[]{"HALFBASKETBALLCOURTS", "SOCCERFIELDS","LITSOFTBALLFIELDS",
                                    "something else", "something else",
                                    "something else", "something else",
                                    "something else", "something lse",
                                    "something else", "something else",
                                    "something else", "something else"};*/
        features = new ArrayList<String>();
        for (String key : locData.keySet()) {
            if (Filter.resemblesTruth(locData, key)) {
                features.add(key);
            }
        }

        locName = Util.getName(locData);
        TextView textView = (TextView) findViewById(R.id.location_name);
        textView.setText(locName);

        mapView = (MapView) findViewById(R.id.location_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /**SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location_map);
        mapFragment.getMapAsync(this);**/

        setUpList(features);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addToMap();
    }

    private void setUpList(ArrayList<String> features) {
        ArrayAdapter itemsAdapter;
        itemsAdapter = new ArrayAdapter(this, android.R.layout.test_list_item, features);

        //ArrayAdapter<String> itemsAdapter =
        //        new ArrayAdapter<String>(this, R.layout.activity_location, R.id.loc_list_item, features);

        listView = (ListView) findViewById(R.id.location_list);
        listView.setAdapter(itemsAdapter);

    }

    private void addToMap() {
        MarkerOptions mkrOpt = Util.getParkMkrOpt(locData, getResources());
        Marker m = mMap.addMarker(mkrOpt);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(m.getPosition()));
        /*if (isCtr) {
            MarkerOptions mkrOpt = Util.getCenterMkrOpt(locData);
            Marker m = mMap.addMarker(mkrOpt);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(m.getPosition()));
        }*/
        /*if (isPark) {
            List<PolygonOptions> polyList = Util.getParkPolyOpt(locData);
            for (PolygonOptions polyOpt : polyList) {
                Polygon newPoly = mMap.addPolygon(polyOpt);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Util.getParkCenter(locData)));
        }*/
    }

}
