package appcontest.playabq;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

    public static ArrayList<Map<String, Object>> getParkList(Map parkData) {
        ArrayList features = (ArrayList) parkData.get("features");
        ArrayList<Map<String,Object>> parkList = new ArrayList();
        for (int i = 0; i < features.size(); i++) {
            Map park = (Map) features.get(i);
            Map parkMap = new LinkedHashMap();
            Set<Entry> attributes = ((Map) park.get("attributes")).entrySet();
            for (Map.Entry<String, String> entry : attributes) {
                parkMap.put(entry.getKey(),entry.getValue());
            }
            parkMap.put("geometry", park.get("geometry"));
            parkList.add(parkMap);
        }
        return parkList;
    }

    public static Map<String,String> getAliases(Map data) {
        return (Map) data.get("fieldAliases");
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


    public static ArrayList<Map<String,Object>> getCommList(Map commData) {
        List features = (List) commData.get("features");
        ArrayList commList = new ArrayList();
        for (Object ctr : features) {
            commList.add(ctr);
        }
        return commList;
    }


}
