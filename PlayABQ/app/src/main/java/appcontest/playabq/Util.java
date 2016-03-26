package appcontest.playabq;

import java.util.Map;

/**
 * Created by david on 3/26/16.
 */
public class Util {

    public static boolean isCommCenter(Map m)
    {
        return m.containsKey("CENTERNAME");
    }

    public static boolean isPark(Map m)
    {
        return m.containsKey("PARKNAME");
    }

    public static String getName(Map m) {
        if(isCommCenter(m)) return (String) m.get("CENTERNAME");
        else return (String) m.get("PARKNAME");
    }
}
