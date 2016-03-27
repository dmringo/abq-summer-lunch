package appcontest.playabq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CSV2JSON converts the community center CSV file to JSON that roughly matches the structure
 * of the Parks JSON from Albuquerque's Open Data source.  The CSV is generated from the Google Sheets
 * document created by Jessica, Stephen and David from the community center information found on
 * the City of Albuquerque's website.
 * Spreadsheet:
 * https://docs.google.com/spreadsheets/d/1oytoQlTWVYquts0ayYsOwJjfM39MyKaydve105dxJLQ/
 *
 * City of Albuquerque Community Center info:
 * http://www.cabq.gov/family/facilities-centers/community-center-locations/community-center-listing
 *
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

    static void sayOne(){
        System.out.println("ONE");
    }

    /**
     * Produces the Java objects from the Community Center CSV that the Jackson library works with
     * to write JSON files.
     *
     * @param infile CSV file to process
     * @return Map representing the CSV data
     */
    private static Map processCommCSV(String infile) {

        Map<Object, Object> map = new LinkedHashMap<>();
        ArrayList<String> lines = new ArrayList<>();

        /* just read all the lines into a list in a single try block.  There are only a few, so
           no need to process as a stream
         */
        try {
            BufferedReader r = new BufferedReader(new FileReader(infile));
            String s;

            /* K&R approved */
            while ((s = r.readLine()) != null) lines.add(s);


        } catch (IOException e) {
            e.printStackTrace();
        }

        /* which field we use to best identify a given center */
        map.put("displayFieldName", "CENTERNAME");

        /* first element in list is CSV headers */
        Map aliases = buildCommAliases(lines.get(0));

        /* aliases for each center's fields (human readable, in essence */
        map.put("fieldAliases", aliases);

        /* Centers are identified by a single point */
        map.put("geometryType", "esriGeometryPoint");

        /* describes the coordinate system used to locate centers (normal latitude and longitude) */
        map.put("spatialReference", spatialRefMap());

        /* actually process each the lines of the CSV file (each line is a center) */
        map.put("features", buildFeatureMaps(aliases.keySet(), lines.subList(1, lines.size())));

        return map;
    }


    /**
     * Processes the lines representing community centers from the CSV.
     *
     * @param fieldSet fields (headers) from the CSV
     * @param csvLines List of lines (Strings) to process
     * @return A List of Maps, each Map represents a single community center (Header -> Data)
     */
    private static List<Map> buildFeatureMaps(Set fieldSet, List<String> csvLines)
    {
        List<Map> features = new ArrayList<>();

        for (String line : csvLines){
            Map<String, Object> cMap = new LinkedHashMap<>();

            /* depending on which field we're looking at, we want ot process it slightly
            differently, so, we keep track of the field IDs as we process elements */
            Object[] fields = fieldSet.toArray();

            double lat = 0;
            double lon = 0;
            int i = 0;

            /* process each of the fields of the CSV in turn */
            for (String elem : line.split(",")) {

                String key= (String) fields[i++];

                /* latitude and longitude values are treated specially (don't want string types)
                   Their values are used to construct the Point object later
                 */
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

            /* make the Point object (center location) */
            cMap.put("geometry", esriPointMap(lon, lat));

            /* add the current ceter to the "features" list */
            features.add(cMap);
        }

        return features;
    }

    /**
     * Build the map for representing a single point in ArcGIS JSON
     * @param lon longitude coord
     * @param lat latitude coord
     * @return Map representing the coordinate point
     */
    private static Map<String, Double> esriPointMap(double lon, double lat) {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("x", lon);
        map.put("y", lat);
        return map;
    }

    /**
     * @return Spatial Reference information specifying the coordinate system for the location data
     * of a center
     */
    private static Map<String, Integer> spatialRefMap() {
        /*
          The spatial reference defines the format of the location data for each center.
          Well Known ID 4326 specifies something like "regular" geographic coordinates.
          More (better) information:
          http://pro.arcgis.com/en/pro-app/help/mapping/properties/coordinate-systems-and-projections.htm
         */
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("wkid", 4326);
        map.put("latestWkid", 4326);
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
}
