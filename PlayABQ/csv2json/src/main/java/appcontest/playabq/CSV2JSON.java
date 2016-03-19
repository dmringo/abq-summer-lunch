package appcontest.playabq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Created by david on 3/18/16.
 */
public class CSV2JSON {
    static final String commCenterCSV = "assets/community_centers.csv";
    static final String commCenterJSON = "assets/community_centers.json";

    public static void main(String[] args) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(new File(commCenterJSON),processCommCSV(commCenterCSV));

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    static Map processCommCSV(String infile) {
        Map<Object, Object> map = new LinkedHashMap<>();
        ArrayList<String> lines = new ArrayList<>();
        try {
            BufferedReader r = new BufferedReader(new FileReader(infile));
            String s;
            while ((s = r.readLine()) != null) {
                lines.add(s);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        map.put("displayFieldName", "CENTERNAME");

        /* first element in list is CSV headers */
        Map aliases = buildCommAliases(lines.get(0));
        map.put("fieldAliases", aliases);
        map.put("geometryType", "esriGeometryPoint");
        map.put("spatialReference", spatialRefMap());
        map.put("features", buildFeatureMaps(aliases.keySet(), lines.subList(1, lines.size())));
        return map;
    }

    private static List<Map> buildFeatureMaps(Set fieldSet, List<String> csvLines)
    {
        List<Map> features = new ArrayList<>();
        for (String line : csvLines.subList(1, csvLines.size())) {
            Map<String, Object> cMap = new LinkedHashMap<>();
            Iterator keys = fieldSet.iterator();
            double lat = 0;
            double lon = 0;
            String[] vals = line.split(",");
            for (String elem : vals) {
                String key=null;
                try {
                    key = (String) keys.next();
                }
                catch(NoSuchElementException e)
                {
                    System.out.println(fieldSet);
                    System.out.println(fieldSet.size());
                    System.out.println(Arrays.toString(vals));
                    System.out.println(vals.length);
                    System.out.println(keys);
                    System.exit(1);
                }


                if (key.equalsIgnoreCase("latitude")) {
                    lat = Double.parseDouble(elem);
                    cMap.put(key, lat);
                } else if (key.equalsIgnoreCase("longitude")) {
                    lon = Double.parseDouble(elem);
                    cMap.put(key, lon);
                } else
                    switch (elem) {
                    /* all empty fields are implicitly False (indicating whether a center has a
                       feature or not */
                        case "":
                            cMap.put(key, Boolean.FALSE);
                            break;
                        case "Y":
                            cMap.put(key, Boolean.TRUE);
                            break;
                    /* non-boolean fields */
                        default:
                            cMap.put(key, elem);
                    }
            }
            cMap.put("geometry", esriPointMap(lon, lat));
            features.add(cMap);
        }
        return features;
    }

    private static Map<String, Double> esriPointMap(double lon, double lat) {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("x", lon);
        map.put("y", lat);
        return map;
    }

    private static Map<String, Integer> spatialRefMap() {
        /*
         "spatialReference":
         {
            "wkid": 102100,
            "latestWkid": 3857
         }
         */
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("wkid", 102100);
        map.put("latestWkid", 3857);
        return map;
    }


    /**
     * Builds the map of field name aliases from the Community Center CSV headers.
     * With a few exceptions, each field name is the header, minus whitespace, in
     * all caps.  "Name" is given "CENTERNAME" as its field name, like "PARKNAME" in the Parks JSON
     * file.
     * The headers themselves are used as the aliases (i.e. the printable name).
     *
     * @param headers Community Center headers in CSV string
     * @return Map of field names to aliases; metadata, in effect, for the eventual JSON object
     */
    private static Map buildCommAliases(String headers) {
        Map<Object, Object> map = new LinkedHashMap<>();
        for (String header : headers.split(",")) {
            if (header.equalsIgnoreCase("name")) map.put("CENTERNAME", header);
            else map.put(header.replaceAll(" ", "").toUpperCase(), header);
        }

        return map;
    }

    public static String csv2json(String src, String trg) {
        return null;
    }

}
