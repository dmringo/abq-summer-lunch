package appcontest.playabq;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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
import java.util.Arrays;
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
    private Map<String,String> aliases;
    private List<String> fieldsToIgnore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Intent intent = getIntent();
        locData = (HashMap<String,Object>)intent.getSerializableExtra("data");
        String[] possFeatures;
        if (Util.isCommCenter(locData)) {
            isCtr = true;
            aliases = Util.aliases;
        }
        else {
            isPark = true;
            aliases = (Map) intent.getSerializableExtra("parkAliases");
            fieldsToIgnore = Arrays.asList(getResources().getStringArray(R.array.park_ignore));
        }


        features = new ArrayList<String>();
        for (String key : locData.keySet()) {
            System.out.println(key+" "+locData.get(key)+" "+Filter.resemblesTruth(locData, key));
            if (Filter.resemblesTruth(locData, key)){
                if (!fieldsToIgnore.contains(key)) {
                    features.add('\u2022'+" "+aliases.get(key));

                }
            }
        }

        String website;
        if (isCtr) {
            features.add(0, (String) locData.get("REGULARHOURS"));
            website = (String) locData.get("WEBSITE");
        }
        else {
            website = getString(R.string.abq_park_website);
        }

        locName = Util.getName(locData);
        TextView textView = (TextView) findViewById(R.id.location_name);
        textView.setText(locName);

        String moreInfo = "<a href='"+website+"'>Click for More Information</a>";
        TextView infoView =(TextView)findViewById(R.id.more_info);
        infoView.setClickable(true);
        infoView.setMovementMethod(LinkMovementMethod.getInstance());
        infoView.setText(Html.fromHtml(moreInfo));

        mapView = (MapView) findViewById(R.id.location_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


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

        listView = (ListView) findViewById(R.id.location_list);
        listView.setAdapter(itemsAdapter);

    }

    private void addToMap() {
        MarkerOptions mkrOpt = Util.getMarker(locData, this);
        Marker m = mMap.addMarker(mkrOpt);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(m.getPosition()));
    }

}
