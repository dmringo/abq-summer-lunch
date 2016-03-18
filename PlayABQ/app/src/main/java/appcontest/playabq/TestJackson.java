package appcontest.playabq;


import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class TestJackson {
    final static String TEST_FILE = "west_bluff.json";
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
