package appcontest.playabq;

import android.location.Location;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Stephen on 3/23/2016.
 *
 * Class meant to filter the parsed json community center and map information.
 */
public class Filter {
    private static ArrayList<HashMap<String, Object>> communityCenterList;
    private static ArrayList<HashMap<String, Object>> parkList;
    private static ArrayList<HashMap<String, Object>> currentFilteredLocations;
    private static ArrayList<String> currentFields = new ArrayList<>();
    private static Location userLocation;
    private static Comparator<? super HashMap<String, Object>> comparator =
            new DistanceFromUserComparator();

    public static void init (ArrayList<HashMap<String, Object>> commList,
                             ArrayList<HashMap<String, Object>> prkList) {
        communityCenterList= commList;
        parkList=prkList;

        currentFilteredLocations= new ArrayList<>();
        currentFilteredLocations.addAll(prkList);
        currentFilteredLocations.addAll(commList);
        sort();
    }

    /**
     *
     * @param requiredFeatures a list of preferred features for a park or community center.
     * @return a list of community centers and parks that include all of the features in sorted
     * with increasing distance from user.
     */
    public static ArrayList<HashMap<String, Object>> intersectGetLocationsWith(List<String> requiredFeatures) {
        currentFilteredLocations.clear();
        currentFilteredLocations.addAll(parkList);
        currentFilteredLocations.addAll(communityCenterList);
            for (HashMap ctr : communityCenterList) {
                for (String requiredFeature:requiredFeatures) {
                    if (ctr.containsKey(requiredFeature))
                    {
                        if(!resemblesTruth(ctr, requiredFeature)) {
                            currentFilteredLocations.remove(ctr);
                            break;
                        }
                    }
                }
            }
            for (HashMap prk : parkList) {
                for (String requiredFeature:requiredFeatures) {
                    if(prk.containsKey(requiredFeature)) {
                        if (!resemblesTruth(prk, requiredFeature)) {
                            currentFilteredLocations.remove(prk);
                            break;
                        }
                    }
                }
            }
        sort();
        return currentFilteredLocations;
    }

    /**
     *
     * @param requiredFeatures a list of preferred features for a park or community center.
     * @return a list of community centers and parks that include any of the features in sorted
     * with increasing distance from user.
     */
    public static ArrayList<HashMap<String, Object>> unionGetLocationsWith(List<String> requiredFeatures) {
        currentFilteredLocations.clear();
        for (String requiredFeature:requiredFeatures) {
            for (HashMap ctr : communityCenterList) {
                if (ctr.containsKey(requiredFeature) &&
                        resemblesTruth(ctr, requiredFeature) &&
                        !currentFilteredLocations.contains(ctr)) {
                    currentFilteredLocations.add(ctr);
                }
            }
            for (HashMap prk : parkList) {
                if (prk.containsKey(requiredFeature) &&
                        resemblesTruth(prk, requiredFeature)
                        && !currentFilteredLocations.contains(prk)) {
                    currentFilteredLocations.add(prk);
                }
            }
        }
        sort();
        return currentFilteredLocations;
    }


    /**
     *
     * @param location a park or community center
     * @param feature a feature of a park or community center
     * @return if the park or community center contains the feature based on the json file.
     */
    public static boolean resemblesTruth(Map location,String feature) {
        Object predicate;
        if ((predicate=location.get(feature))==null)
            return false;
        String strPred = predicate.toString();
        return !(strPred.equalsIgnoreCase("false") || strPred.equals("0"));
    }

    /**
     * Convenience method to print the last filtered list of parks and centers
     */
    public static void printLocations()
    {
        for(Map location:currentFilteredLocations)
        {
            System.out.println(location.get("CENTERNAME"));
        }
    }

    /**
     * @return the current filtered list to contain every park and community center
     * it did not contain, while removing every area it did contain previously.
     */
    public static ArrayList<HashMap<String, Object>> negateCurrentFiltered(){
        for (HashMap ctr : communityCenterList) {
           if (currentFilteredLocations.contains(ctr)) {
               currentFilteredLocations.remove(ctr);
           } else {
               currentFilteredLocations.add(ctr);
           }
        }
        for (HashMap prk : parkList) {
            if (currentFilteredLocations.contains(prk)) {
                currentFilteredLocations.remove(prk);
            } else {
                currentFilteredLocations.add(prk);
            }
        }

        sort();
        return currentFilteredLocations;
    }

    public static ArrayList<HashMap<String, Object>> filtered()
    {
        return currentFilteredLocations;
    }

    public static ArrayList<Map<String, Object>> selectParks(ArrayList<HashMap<String, Object>> all) {
        ArrayList parks = new ArrayList();
        for(Map m : all) if(Util.isPark(m)) parks.add(m);
        return parks;
    }

    public static ArrayList<Map<String, Object>> selectCommCenters(ArrayList<HashMap<String, Object>> all) {
        ArrayList commCenters = new ArrayList();
        for(Map m : all) if(Util.isCommCenter(m)) commCenters.add(m);
        return commCenters;
    }

    public static void addRequirement(String key) {
        currentFields.add(key);

        Iterator<HashMap<String, Object>> iterator = currentFilteredLocations.iterator();
        while(iterator.hasNext())
        {
            HashMap m = iterator.next();
            if(m.containsKey(key) && !resemblesTruth(m, key))
            {
                iterator.remove();
            }
        }
        sort();
    }

    private static void sort() {
        Collections.sort(currentFilteredLocations, comparator);
    }


    public static void removeRequirement(String key) {
        currentFields.remove(key);
        intersectGetLocationsWith(currentFields);
    }
}