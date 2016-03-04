import com.google.android.gms.maps.model.LatLng;

/**
 * Created by jessica on 2/28/16.
 */
public class CommunityCenter {

    private String name;
    private LatLng coordinates;

    public CommunityCenter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }
}
