package appcontest.playabq;

import android.graphics.Color;

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
            String[] capsString = ((String) m.get("PARKNAME")).split(" ");
            String newName = "";
            for (String str : capsString) {
                str.toLowerCase();
                String newStr = str.substring(0, 1).toUpperCase() + str.substring(1);
                newName.concat(newStr+" ");
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

    /**
     * @param name      community center name
     * @param geometry  map with lat("y") & lon("x")
     * @return          MarkerOptions for community center
     */
    public static MarkerOptions getCenterMkrOpt(String name, Map<String, Object> geometry) {
        double lat = (double) geometry.get("y");
        double lon = (double) geometry.get("x");
        return new MarkerOptions().position(new LatLng(lat, lon)).title(name);
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
                    .fillColor(Color.parseColor("#859bff"))
                    .clickable(true);
            polyList.add(polyOpt);
        }

        return polyList;
    }

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
}
