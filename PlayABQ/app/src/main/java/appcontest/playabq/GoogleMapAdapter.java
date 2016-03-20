package appcontest.playabq;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;

/**
 * Created by david on 3/20/16.
 *
 * Implements many of the Google Map event listeners with simple stubs.  Extend this class
 * and override the appropriate methods to respond to map events.  See Google's docs online
 * for details of when/how each should be used:
 * https://developers.google.com/android/reference/com/google/android/gms/maps/package-summary
 *
 * An in-progress debugging implementation is provided as a public static field.
 * (Prints simple info to Debug Log)
 */
public class GoogleMapAdapter implements
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnPolygonClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnCameraChangeListener,
        GoogleMap.OnGroundOverlayClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnInfoWindowCloseListener,
        GoogleMap.OnInfoWindowLongClickListener
{

    public static final GoogleMapAdapter DEBUG_IMPL = new GoogleMapAdapter(){
        @Override
        public void onMapClick(LatLng latLng) {
            Log.d("MAP-DBG", "Click at: " + latLng.toString());
        }

        @Override
        public void onPolygonClick(Polygon polygon) {
            Log.d("MAP-DBG", "Polygon click, ID: " + polygon.getId());
        }
    };

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onPolygonClick(Polygon polygon) {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onGroundOverlayClick(GroundOverlay groundOverlay) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onInfoWindowClose(Marker marker) {

    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {

    }
}
