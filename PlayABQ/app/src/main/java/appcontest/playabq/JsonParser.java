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
 */
public class JsonParser {
    final static String PARKS_FILE = "parks.json";
    final static ObjectMapper parkMapper = new ObjectMapper();
    final static String COMM_FILE = "community_centers.json";
    final static ObjectMapper commMapper = new ObjectMapper();


    public static Map getParkData(Context ct)  {
        try {
            InputStream stream = ct.getAssets().open(PARKS_FILE);
            Map map = parkMapper.readValue(stream, Map.class);
            return map;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List getParkList(Map parkData) {
        List features = (List) parkData.get("features");
        List parkList = new ArrayList();
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

    public static Map getAliases(Map data) {
        return (Map) data.get("fieldAliases");
    }

    public static Map getCommData(Context ct)  {
        try {
            InputStream stream = ct.getAssets().open(COMM_FILE);
            Map map = commMapper.readValue(stream, Map.class);
            return map;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List getCommList(Map commData) {
        List features = (List) commData.get("features");
        List commList = new ArrayList();
        for (Object ctr : features) {
            commList.add(ctr);
        }
        return commList;
    }


}
