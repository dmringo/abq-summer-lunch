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
    final static ObjectMapper mapper = new ObjectMapper();

    public static Map getParkData(Context ct)  {
        try {
            InputStream stream = ct.getAssets().open(PARKS_FILE);
            Map map = mapper.readValue(stream, Map.class);
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

    public static Map getParkAliases(Map parkData) {
        return (Map) parkData.get("fieldAliases");
    }

}
