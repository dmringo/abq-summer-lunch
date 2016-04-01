package appcontest.playabq;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by david on 3/26/16.
 */
public class Util {


    private static String ccBaseColor = "#859bff";
    private static String parkBaseColor = "#9ad48f";
    private static String usrBaseColor = "#ffff80";

    public static boolean isTrackingUser = false;
    public static HashMap <String, String> reverseAliasMap = new HashMap<>();
    public static HashMap <String, String> aliases;
    public static ArrayList<HashMap<String, Object>> parkList;
    public static ArrayList<HashMap<String, Object>> commList;

    private static Location userLocation = getDefaultLocation();

    public static Location getUserLocation() {
        return userLocation;
    }

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




    public static MarkerOptions getUserMkrOpt() {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(usrBaseColor), hsv);
        BitmapDescriptor color = BitmapDescriptorFactory.defaultMarker(hsv[0]);
        return new MarkerOptions().position(
                new LatLng(getUserLocation().getLatitude(), getUserLocation().getLongitude()))
                .title("My Location").icon(color);
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



    public static MarkerOptions getMarker(Map<String, Object> loc, Context ctx)
    {
        int drawableId;
        boolean isPark = isPark(loc);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            drawableId = isPark ? R.drawable.ic_park : R.drawable.ic_comm;
        else
            drawableId = isPark ? R.drawable.ic_park_bmp : R.drawable.ic_comm_bmp;
        double lat = getLat(loc);
        double lon = getLon(loc);
        BitmapDescriptor ico = makeMarkerBitmapDescr(ctx, drawableId);

        return new MarkerOptions().position(new LatLng(lat, lon)).title(getName(loc)).icon(ico);
    }


    /*  */

    /**
     * This is a sort of hacky way of setting the icons to a reasonable size.
     * Not sure if it's really portable.  - David
     * TODO: test on other devices
     *
     * @param ctx  Resources for app
     * @param drawableId Resource ID for the drawable object to make a marker for
     * @return BitmapDescriptor to use for a MarkerOptions object
     */

    private static BitmapDescriptor makeMarkerBitmapDescr(Context ctx, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(ctx, drawableId);
        Bitmap bm;

        if(drawable instanceof BitmapDrawable) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 3;
            bm = BitmapFactory.decodeResource(ctx.getResources(), drawableId,
                    opts);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                drawable instanceof VectorDrawable){
            int h = drawable.getIntrinsicHeight();
            int w = drawable.getIntrinsicWidth();
            drawable.setBounds(0, 0, w, h);
            bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            drawable.draw(canvas);
        }
        else throw new IllegalArgumentException("Bad drawable for Markers");

        BitmapDescriptor ico = BitmapDescriptorFactory.fromBitmap(bm);
        return ico;
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

    public static Location getDefaultLocation (){
        Location defaultLocation = new Location("Default");
        defaultLocation.setLatitude(35.113281);
        defaultLocation.setLongitude(-106.621216);
        return defaultLocation;
    }

    public static String keyByAlias(String title) {
        return reverseAliasMap.get(title);
    }

    public static void init(Context ctx) {
        setUserLocation(getDefaultLocation());
        isTrackingUser = false;

        Map parkData = JsonParser.getParkData(ctx);
        Map commData = JsonParser.getCommData(ctx);
        aliases = JsonParser.getAliases(parkData);
        aliases.putAll(JsonParser.getAliases(commData));
        parkList = JsonParser.getParkList(parkData);
        commList = JsonParser.getCommList(commData);

        for(String key : aliases.keySet()) reverseAliasMap.put(aliases.get(key), key);

        for (Map ctr : commList) {
            MarkerOptions mkrOpt = getMarker(ctr, ctx);
            ctr.put("MarkerOptions", mkrOpt);
        }
        for (Map park : parkList) {
            MarkerOptions mkrOpt = getMarker(park, ctx);
            park.put("MarkerOptions", mkrOpt);
        }

    }
}
