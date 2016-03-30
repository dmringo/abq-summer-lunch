package appcontest.playabq;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Map;

/**
 * Created by david on 3/26/16.
 */
public class Util {
    private static String ccBaseColor = "#859bff";
    private static String parkBaseColor = "#9ad48f";


    public static Location getUserLocation() {
        return userLocation;
    }

    private static Location userLocation = null;
    public static boolean isCommCenter(Map m)
    {
        return m.containsKey("CENTERNAME");
    }

    public static boolean isPark(Map m)
    {
        return m.containsKey("PARKNAME");
    }

    public static String getName(Map m) {
        if(isCommCenter(m)) return (String) m.get("CENTERNAME");
        else {
            String[] capsString = ((String) m.get("PARKNAME")).split("[ ]+");
            String newName = "";
            for (String str : capsString) {
                String lowerCase = str.toLowerCase()+" ";
                String newStr = lowerCase.substring(0, 1).toUpperCase() + lowerCase.substring(1);
                newName = newName.concat(newStr);
            }
            return newName.trim();
        }
    }

    public static double getLat(Map<String,Object> m) {
        return ((Map<String,Double>)m.get("geometry")).get("y");
    }

    public static double getLon(Map<String, Object> m)
    {
        return ((Map<String,Double>)m.get("geometry")).get("x");
    }

    public static double metersToMiles(double meters)
    {
        return meters * 0.000621371192;
    }


    public static MarkerOptions getCenterMkrOpt(Map<String, Object> center) {
        String name = getName(center);
        double lat = getLat(center);
        double lon = getLon(center);
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(ccBaseColor), hsv);
        BitmapDescriptor color = BitmapDescriptorFactory.defaultMarker(hsv[0]);
        return new MarkerOptions().position(new LatLng(lat, lon)).title(name).icon(color);
    }

    /**
     * Get list of polygon options for a given park's geometry
     * @param park  map representing a park
     * @return      list of polygon options for that park's polygons
     */
    public static List<PolygonOptions> getParkPolyOpt(Map park) {
        List<PolygonOptions>  polyList = new ArrayList<PolygonOptions>();
        Map coords = (Map) park.get("geometry");
        // get arraylist of arraylists representing contiguous areas

        List polys = (List) coords.get("rings");
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
                    .fillColor(R.color.parkBaseColor)
                    .strokeColor(R.color.parkBaseColor)
                    .clickable(true);
            polyList.add(polyOpt);
        }

        return polyList;
    }

    @Deprecated
    public static LatLng getParkCenter(Map<String, Object> park)
    {
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        Map geometry = (Map) park.get("geometry");

        List<List<List<Double>>> rings = (List) geometry.get("rings");
        for(List<List<Double>> path : rings) {
            for (List<Double> coords : path)
                b.include(new LatLng(coords.get(1), coords.get(0)));
        }
        LatLng center = b.build().getCenter();
        return center;
    }

    public static MarkerOptions getParkMkrOpt(Map<String, Object> park) {
        String name = getName(park);
        double lat = getLat(park);
        double lon = getLon(park);
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(parkBaseColor), hsv);
        BitmapDescriptor color = BitmapDescriptorFactory.defaultMarker(hsv[0]);
        return new MarkerOptions().position(new LatLng(lat, lon)).title(name).icon(color);
    }

    /**
     * Called from Main Activity
     * @param loc location of user
     */
    public static void setUserLocation(Location loc)
    {
        userLocation=loc;
    }

    /**
     *
     * @param area the area you want to find the distance to from the user
     * @return the distance from the user to the area in meters
     */
    public static double getDistanceFromUser(Map area){
        if (userLocation == null) {
            return Double.POSITIVE_INFINITY;
        }
        Map geometry = (Map) area.get("geometry");
        Location areaLoc = new Location("Area Loc");
        areaLoc.setLatitude((double) geometry.get("y"));
        areaLoc.setLongitude((double) geometry.get("x"));
        return userLocation.distanceTo(areaLoc);
    }
}
