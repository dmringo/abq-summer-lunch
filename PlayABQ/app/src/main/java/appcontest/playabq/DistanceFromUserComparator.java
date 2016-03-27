package appcontest.playabq;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by Stephen on 3/26/2016.
 * Comparator for sorting filtered lists based on distance from user.
 */
public class DistanceFromUserComparator implements Comparator {
    @Override
    public int compare(Object lhs, Object rhs) {
        double firstAreaDestance = Util.getDistanceFromUser((Map) lhs);
        double secondAreaDistance = Util.getDistanceFromUser((Map) rhs);
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
