package appcontest.playabq;

import android.location.Location;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by Stephen on 3/23/2016.
 *
 * Class meant to filter the parsed json community center and map information.
 */
public class Filter {
    private static ArrayList<Map<String, Object>> communityCenterList;
    private static ArrayList<Map<String, Object>> parkList;
    private static ArrayList<Map<String, Object>> currentFilteredLocations;
    private static Location userLocation;

    public static void init (ArrayList<Map<String, Object>> commList,  ArrayList<Map<String, Object>> prkList) {
        communityCenterList= commList;
        parkList=prkList;
        currentFilteredLocations= new ArrayList<>(commList);
        currentFilteredLocations.addAll(parkList);
    }

    /**
     *
     * @param requiredFeatures a list of preferred features for a park or community center.
     * @param usrLoc is the location of the user (found using google location services)
     * @return a list of community centers and parks that include all of the features in sorted
     * with increasing distance from user.
     */
    public static ArrayList<Map<String,Object>> intersectGetLocationsWith(List<String>requiredFeatures,
                                                                   Location usrLoc) {
        userLocation=usrLoc;
        currentFilteredLocations.clear();
            for (Map ctr : communityCenterList) {
                for (String requiredFeature:requiredFeatures) {
                    if (!resemblesTruth(ctr, requiredFeature)) {
                        currentFilteredLocations.remove(ctr);
                        break;
                    } else if (!currentFilteredLocations.contains(ctr)) {
                        currentFilteredLocations.add(ctr);
                    }
                }
            }
            for (Map prk : parkList) {
                for (String requiredFeature:requiredFeatures) {
                    if (!resemblesTruth(prk, requiredFeature)) {
                        currentFilteredLocations.remove(prk);
                        break;
                    } else if (!currentFilteredLocations.contains(prk)) {
                        currentFilteredLocations.add(prk);
                    }
                }
            }
        Collections.sort(currentFilteredLocations, new DistanceFromUserComparator());
        return currentFilteredLocations;
    }

    /**
     *
     * @param requiredFeatures a list of preferred features for a park or community center.
     * @param usrLoc is the location of the user (found using google location services)
     * @return a list of community centers and parks that include any of the features in sorted
     * with increasing distance from user.
     */
    public static ArrayList<Map<String, Object>> unionGetLocationsWith(List<String> requiredFeatures, Location usrLoc) {
        userLocation=usrLoc;
        currentFilteredLocations.clear();
        for (String requiredFeature:requiredFeatures) {
            for (Map ctr : communityCenterList) {
                if (resemblesTruth(ctr, requiredFeature) && !currentFilteredLocations.contains(ctr)) {
                    currentFilteredLocations.add(ctr);
                }
            }
            for (Map prk : parkList) {
                if (resemblesTruth(prk, requiredFeature) && !currentFilteredLocations.contains(prk)) {
                    currentFilteredLocations.add(prk);
                }
            }
        }
        Collections.sort(currentFilteredLocations, new DistanceFromUserComparator());
        return currentFilteredLocations;
    }


    /**
     *
     * @param location a park or community center
     * @param feature a feature of a park or community center
     * @return if the park or community center contains the feature based on the json file.
     */
    private static boolean resemblesTruth(Map location,String feature) {
        Object predicate;
        if ((predicate=location.get(feature))==null)
            return false;
        String strPred = (String) predicate.toString();
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
    public static ArrayList<Map<String, Object>> negateCurrentFiltered(){
        for (Map ctr : communityCenterList) {
           if (currentFilteredLocations.contains(ctr)) {
               currentFilteredLocations.remove(ctr);
           } else {
               currentFilteredLocations.add(ctr);
           }
        }
        for (Map prk : parkList) {
            if (currentFilteredLocations.contains(prk)) {
                currentFilteredLocations.remove(prk);
            } else {
                currentFilteredLocations.add(prk);
            }
        }
        Collections.sort(currentFilteredLocations, new DistanceFromUserComparator());
        return currentFilteredLocations;
    }

    public static ArrayList<Map<String, Object>> filtered()
    {
        return currentFilteredLocations;
    }

    public static ArrayList<Map<String, Object>> selectParks(ArrayList<Map<String, Object>> all) {
        ArrayList parks = new ArrayList();
        for(Map m : all) if(Util.isPark(m)) parks.add(m);
        return parks;
    }

    public static ArrayList<Map<String, Object>> selectCommCenters(ArrayList<Map<String, Object>> all) {
        ArrayList parks = new ArrayList();
        for(Map m : all) if(Util.isPark(m)) parks.add(m);
        return parks;
    }
}