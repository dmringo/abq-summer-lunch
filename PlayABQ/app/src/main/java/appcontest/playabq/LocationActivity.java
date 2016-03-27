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
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;


public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng location;
    private String[] features;
    private MapView mapView;
    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        //Intent intent = getIntent();
        //HashMap<Object,Object> locData = (HashMap<Object, Object>)intent.getSerializableExtra("data");


        String name = "test name";
        location =  new LatLng(35.110148212230001,-106.69786569233656);
        features = new String[]{"HALFBASKETBALLCOURTS", "SOCCERFIELDS","LITSOFTBALLFIELDS",
                                    "something else", "something else",
                                    "something else", "something else",
                                    "something else", "something lse",
                                    "something else", "something else",
                                    "something else", "something else"};

        TextView textView = (TextView) findViewById(R.id.location_name);
        textView.setText(name);

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
        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title("TEST"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));

        //final View mapView = getSupportFragmentManager().findFragmentById(R.id.location_map).getView();
    }

    private void setUpList(String[] features) {
        ArrayAdapter itemsAdapter;
        itemsAdapter = new ArrayAdapter(this, android.R.layout.test_list_item, features);

        //ArrayAdapter<String> itemsAdapter =
        //        new ArrayAdapter<String>(this, R.layout.activity_location, R.id.loc_list_item, features);

        listView = (ListView) findViewById(R.id.location_list);
        listView.setAdapter(itemsAdapter);

    }

}
