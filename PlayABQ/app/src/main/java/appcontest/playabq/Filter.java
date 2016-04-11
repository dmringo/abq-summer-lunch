package appcontest.playabq;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static appcontest.playabq.Filter.FilterMode.INTERSECT;

/**
 * Created by Stephen on 3/23/2016.
 *
 * Class meant to filter the parsed json community center and map information.
 */
public class Filter {
    static ArrayList<HashMap<String, Object>> commList;
    static ArrayList<HashMap<String, Object>> parkList;
    private static ArrayList<HashMap<String, Object>> allLocs = new ArrayList<>();
    private static ArrayList<HashMap<String, Object>> currentFilteredLocations = new ArrayList<>();
    private static ArrayList<String> currentFields = new ArrayList<>();
    private static DistanceFromUserComparator comparator =
            new DistanceFromUserComparator();

    private static String TAG = Filter.class.getSimpleName();
    private static FilterMode MODE = INTERSECT;

    public enum FilterMode{
        UNION, INTERSECT
    }
    public static void init (ArrayList<HashMap<String, Object>> cmmList,
                             ArrayList<HashMap<String, Object>> prkList) {
        commList = cmmList;
        parkList=prkList;

        currentFilteredLocations.addAll(prkList);
        currentFilteredLocations.addAll(cmmList);
        allLocs.addAll(currentFilteredLocations);
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
        currentFilteredLocations.addAll(allLocs);

        for (HashMap<String, Object> ctr : allLocs) {
            for (String requiredFeature:requiredFeatures) {
                if(!resemblesTruth(ctr, requiredFeature)) {
                    currentFilteredLocations.remove(ctr);
                    break;
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
            for (HashMap<String, Object> ctr : allLocs) {
                if (resemblesTruth(ctr, requiredFeature) &&
                        !currentFilteredLocations.contains(ctr)) {
                    currentFilteredLocations.add(ctr);
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
    public static boolean resemblesTruth(Map<String, Object> location,String feature) {
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
        for(Map<String, Object> location:currentFilteredLocations)
        {
            System.out.println(location.get("CENTERNAME"));
        }
    }

    /**
     * @return the current filtered list to contain every park and community center
     * it did not contain, while removing every area it did contain previously.
     */
    public static ArrayList<HashMap<String, Object>> negateCurrentFiltered(){
        for (HashMap<String, Object> ctr : commList) {
           if (currentFilteredLocations.contains(ctr)) {
               currentFilteredLocations.remove(ctr);
           } else {
               currentFilteredLocations.add(ctr);
           }
        }
        for (HashMap<String, Object> prk : parkList) {
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
        ArrayList<Map<String, Object>> parks = new ArrayList<>();
        for(Map<String, Object> m : all) if(Util.isPark(m)) parks.add(m);
        return parks;
    }

    public static ArrayList<Map<String, Object>> selectCommCenters(ArrayList<HashMap<String, Object>> all) {
        ArrayList<Map<String, Object>> commCenters = new ArrayList<>();
        for(Map<String, Object> m : all) if(Util.isCommCenter(m)) commCenters.add(m);
        return commCenters;
    }

    public static void addRequirement(String key) {
        currentFields.add(key);
        switch(MODE)
        {
            case UNION:
                for(HashMap<String, Object> c : allLocs)
                    if(!currentFilteredLocations.contains(c) &&
                            resemblesTruth(c,key))
                        currentFilteredLocations.add(c);
                break;
            case INTERSECT:
                Iterator<HashMap<String, Object>> iterator = currentFilteredLocations.iterator();
                while(iterator.hasNext())
                    if(!resemblesTruth(iterator.next(), key))
                        iterator.remove();
                break;
        }
        sort();
    }

    public static void sort() {
        Collections.sort(currentFilteredLocations, comparator);
    }


    public static void removeRequirement(String key) {
        currentFields.remove(key);
        switch (MODE)
        {
            case UNION:
                unionGetLocationsWith(currentFields);
                break;
            case INTERSECT:
                intersectGetLocationsWith(currentFields);
                break;
        }
    }

    public static void filter(FilterOpts opts)
    {
        Log.i(TAG,opts.toString());
        switch(opts.mode)
        {
            case AND:
                filterAND(opts.groups);
                break;
            case OR:
                filterOR(opts.groups);
                break;
        }

        sort();
    }

    private static void filterOR(Map<Integer, FilterGroup> opts) {
        currentFilteredLocations.clear();
        for(FilterGroup p : opts.values())
        {
            if(p.isEnabled()) {
                currentFilteredLocations.addAll(p.locs);
                for (HashMap<String, Object> loc : p.locs)
                    if (!anySatisfy(loc, p.reqs)) currentFilteredLocations.remove(loc);
            }
        }
    }

    private static boolean anySatisfy(HashMap<String,Object> loc, List<String> reqs) {
        for(String req : reqs) if(resemblesTruth(loc,req)) return true;
        return false;
    }

    private static void filterAND(Map<Integer, FilterGroup> opts) {
        currentFilteredLocations.clear();
        for(FilterGroup p : opts.values())
        {
            if(p.isEnabled()) {
                for (HashMap<String, Object> map : p.locs)
                    if (allSatisfy(map, p.reqs)) currentFilteredLocations.add(map);
            }
        }
    }

    private static boolean allSatisfy(HashMap<String, Object> map, List<String> reqs) {
        for(String s : reqs) if (!resemblesTruth(map, s)) return false;
        return true;
    }


    public static class FilterOpts{

        private static final String CLASSTAG = FilterOpts.class.getSimpleName();

        public void toggleMode() {
            mode = mode.complement();
        }

        enum Mode{
            AND, OR;

            public Mode complement(){
                if (this == AND) return OR;
                else return AND;
            }

            public boolean toBool(){return this == AND;}

        };
        Mode mode;
        private Map<Integer, FilterGroup> groups = new LinkedHashMap<>();


        public FilterOpts (Mode m, FilterGroup... pairs)
        {
            mode = m;
            for(FilterGroup group : pairs) groups.put(group.tag,group);
        }

        public static FilterOpts newDefault() {

            FilterGroup parks = new FilterGroup(parkList, new ArrayList<String>(), R.id.park_filters_grp);
            FilterGroup comms = new FilterGroup(commList, new ArrayList<String>(), R.id.comm_filters_grp);
            return new FilterOpts(Mode.AND, parks, comms);
        }

        public FilterGroup getGroupByTag(int tag) {
            return groups.get(tag);
        }

        public void toggleReq(int tag, @Nullable String req)
        {
            FilterGroup p = groups.get(tag);
            if (p != null && req != null) {
                if (p.reqs.contains(req)) p.reqs.remove(req);
                else p.reqs.add(req);
            }
            else Log.e(CLASSTAG, String.format("toggleReq(tag = %d, req = %s)%n", tag, req));
        }

        @Override
        public String toString() {
            return String.format("FilterOpts{\n" +
                    "mode=" + mode +
                    "\ngroups=" + groups +
                    "\n}");
        }
    }

    public static class FilterGroup
    {
        final ArrayList<HashMap<String, Object>> locs;
        final List<String> reqs;
        final int tag;
        private boolean enabled = true;

        public FilterGroup(ArrayList<HashMap<String, Object>> locs, List<String> reqs, int tag)
        {
            this.locs = locs;
            this.reqs = reqs;
            this.tag = tag;
        }

        @Override
        public String toString() {
            return String.format("FilterGroup{" +
                    "\nreqs=" + reqs +
                    "\ntag=" + tag +
                    "\nenabled=" + enabled +
                    "\n}");
        }

        public boolean isEnabled(){return enabled;}

        public void enable(){ enabled = true;}

        public void disable(){ enabled = false;}

        public void toggle(){ enabled = !enabled;}

    }


}