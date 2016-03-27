package appcontest.playabq;

import android.location.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Stephen on 3/23/2016.
 *
 * Class meant to filter the parsed json community center and map information.
 */
public class Filter {
    private ArrayList<Map<String, Object>> communityCenterList;
    private ArrayList<Map<String, Object>> parkList;
    private ArrayList<Map<String, Object>> currentFilteredLocations;
    private Location userLocation;

    public Filter (ArrayList<Map<String, Object>> commList,  ArrayList<Map<String, Object>> prkList) {
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
    public ArrayList<Map<String,Object>> intersectGetLocationsWith(List<String>requiredFeatures,
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
    public ArrayList<Map<String, Object>> unionGetLocationsWith(List<String> requiredFeatures, Location usrLoc) {
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
    public void printLocations()
    {
        for(Map location:currentFilteredLocations)
        {
            System.out.println(location.get("CENTERNAME"));
        }
    }

    public ArrayList<Map<String, Object>> filtered()
    {
        return currentFilteredLocations;
    }


    /**
     * Comparator for sorting filtered lists based on distance from user.
     */
    public class DistanceFromUserComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            float firstAreaDestance = ((DistanceAwareArea) lhs).getDistanceFromUser();
            float secondAreaDistance = ((DistanceAwareArea) rhs).getDistanceFromUser();
            if (firstAreaDestance>secondAreaDistance){
                return 1;
            }
            else if (firstAreaDestance<secondAreaDistance) {
                return -1;
            }
            else {
                return 0;
            }
        }
    }

    /**
     * Convenience class for comparator to use when comparing distances of areas.
     */
    public class DistanceAwareArea {
        public float getDistanceFromUser() {
                Map thisJSonDataObj = (Map) this;
                Map geometry = (Map) thisJSonDataObj.get("geometry");
                Location areaLoc = new Location("Area Loc");
                areaLoc.setLatitude((double) geometry.get("y"));
                areaLoc.setLongitude((double) geometry.get("x"));
                return userLocation.distanceTo(areaLoc);
        }
    }
}