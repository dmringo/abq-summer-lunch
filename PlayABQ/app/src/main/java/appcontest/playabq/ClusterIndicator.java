package appcontest.playabq;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptor;

/**
 * Created by Stephen on 3/30/2016.
 */
public class ClusterIndicator implements com.google.maps.android.clustering.ClusterItem{
    private final LatLng clusterPosition;
    private final MarkerOptions mrkrOptions;
    public ClusterIndicator(double lat, double lng, MarkerOptions markerOptions) {
        clusterPosition = new LatLng(lat,lng);
        mrkrOptions=markerOptions;
    }
    @Override
    public LatLng getPosition() {
        return clusterPosition;
    }
    public String getTitle () {return mrkrOptions.getTitle();}
    public MarkerOptions getMarkerOptions () {return mrkrOptions;}
    public BitmapDescriptor getIcon () {return mrkrOptions.getIcon();}
}