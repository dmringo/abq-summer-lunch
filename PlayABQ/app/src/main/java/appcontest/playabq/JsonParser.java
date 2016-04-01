package appcontest.playabq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;

/**
 * Created by jessica on 3/18/16.
 *
 * If we need to get the Parks JSON file again, be sure to use this link:
 * http://coagisweb.cabq.gov/arcgis/rest/services/public/recreation/MapServer/0/query?
 * where=1%3D1
 * &text=&objectIds=&time=&geometry=
 * &geometryType=esriGeometryEnvelope
 * &inSR=&spatialRel=esriSpatialRelIntersects
 * &relationParam=&outFields=*&returnGeometry=true
 * &maxAllowableOffset=&geometryPrecision=
 * &outSR=4326&returnIdsOnly=false&returnCountOnly=false
 * &orderByFields=&groupByFieldsForStatistics=
 * &outStatistics=&returnZ=false&returnM=false&gdbVersion=&f=pjson
 *
 * The important part for our location data is the &outSR=4326, which specifies that we want
 * geographic coordinates.
 */
public class JsonParser {
    final static String PARKS_FILE = "parks.json";
    final static ObjectMapper parkMapper = new ObjectMapper();
    final static String COMM_FILE = "community_centers.json";
    final static ObjectMapper commMapper = new ObjectMapper();


    public static Map<String,Object> getParkData(Context ct)  {
        try {
            InputStream stream = ct.getAssets().open(PARKS_FILE);
            Map<String,Object> map = parkMapper.readValue(stream, Map.class);
            return map;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static ArrayList<HashMap<String, Object>> getParkList(Map parkData) {
        ArrayList<HashMap<String,Object>> features = (ArrayList) parkData.get("features");
        ArrayList<HashMap<String,Object>> parkList = new ArrayList();
        List<String> parksRead = new ArrayList<String>();
        for (Map park : features) {
            HashMap<String,Object> parkMap = (HashMap<String, Object>) park.get("attributes");

            // See if is duplicate
            if (parksRead.contains(parkMap.get("PARKNAME"))) {
                String name = (String) parkMap.get("PARKNAME");
                Map oldMap = null;
                for (Map pk : parkList) {
                    if (pk.get("PARKNAME").equals(name)){
                        oldMap = pk;
                        break;
                    }
                }
                readDuplicate(oldMap,parkMap);
            }
            else {
                parkMap.put("geometry", park.get("geometry"));
                setParkCenter(parkMap);
                parkList.add(parkMap);
                parksRead.add((String) parkMap.get("PARKNAME"));
            }
        }
        return parkList;
    }

    private static void readDuplicate(Map<String, Object> oldMap, Map<String, Object> newMap) {
        String[] toIgnoreArr = {"geometry", "OBJECTID", "PARKNAME", "PARKSTATUS", "JURISDICTION", "ACRES", "DEVELOPEDACRES",
                "created_user", "created_date", "last_edited_user", "last_edited_date"};
        List toIgnore = Arrays.asList(toIgnoreArr);
        for (String key : oldMap.keySet()) {
            if (toIgnore.contains(key)) { continue; }
            else {
                int oldVal = (int) oldMap.get(key);
                int newVal = (int) newMap.get(key);
                int total = newVal + oldVal;
                oldMap.put(key,total);
            }
        }
    }

    public static HashMap<String,String> getAliases(Map data) {
        return (HashMap) data.get("fieldAliases");
    }

    public static Map<String,Object> getCommData(Context ct)  {
        try {
            InputStream stream = ct.getAssets().open(COMM_FILE);
            Map<String,Object> map = commMapper.readValue(stream, Map.class);
            return map;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static ArrayList<HashMap<String, Object>> getCommList(Map commData) {
        List features = (List) commData.get("features");
        ArrayList commList = new ArrayList();
        for (Object ctr : features) {
            commList.add(ctr);
        }
        return commList;
    }


    private static void setParkCenter(Map<String, Object> park)
    {
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        Map geometry = (Map) park.get("geometry");

        List<List<List<Double>>> rings = (List) geometry.get("rings");
        for(List<List<Double>> path : rings) {
            for (List<Double> coords : path)
                b.include(new LatLng(coords.get(1), coords.get(0)));
        }
        LatLng center = b.build().getCenter();
        geometry.put("x",center.longitude);
        geometry.put("y",center.latitude);
    }
}
