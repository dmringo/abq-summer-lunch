package appcontest.playabq;


import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.UTF8JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class TestJackson {
    final static String TEST_FILE = "community_centers.json";
    final static ObjectMapper mapper = new ObjectMapper();

    public static void testJackson(InputStream stream) {
        Map map = parseFile(stream);
        if (null != map) {
            for (Object key : map.keySet()) {

                String msg = String.format("key class: %s, key: %s", key.getClass().toString(), key.toString());
                Log.i("TestJackson", msg);
                Object val = map.get(key);
                msg = String.format("value class: %s, value: %s", val.getClass().toString(), val.toString());
                Log.i("TestJackson", msg);
            }
            try {
                String s = mapper.writeValueAsString(map);
                Log.i("TestJackson", String.format("ValueAsString: %n%s", s));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            List features = (List) map.get("features");
            for (Object item : features) {
                String msg = String.format("feature class: %s, value: %s", item.getClass().toString(), item.toString());
                Log.i("feature", msg);
                Map itemMap = (Map) item;
                for (Object element : itemMap.keySet()) {
                    String elMsg = String.format("feature key class: %s, feature: %s", element.getClass().toString(), element.toString());
                    Log.i("feature key ",elMsg);
                    Object elVal = itemMap.get(element);
                    String valMsg = String.format("feature val class: %s, value: %s", elVal.getClass().toString(), elVal.toString());
                    Log.i("feature val ", valMsg);

                }
            }
        }

    }

    private static Map parseFile(InputStream stream) {
        try {
            return mapper.readValue(stream, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
