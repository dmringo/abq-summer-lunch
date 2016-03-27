package appcontest.playabq;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;
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
        else return (String) m.get("PARKNAME");
    }

    public static double getLat(Map<String,Object> m)
    {
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
}
