package appcontest.playabq;

import android.app.Activity;
import android.database.DataSetObserver;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by david on 3/25/16.
 */
public class MapListAdapter implements ListAdapter {

    final private ArrayList<Map<String, Object>> recs;
    final private MainActivity context;
    private List observers = new ArrayList();

    public MapListAdapter(MainActivity context, ArrayList<Map<String, Object>> recs)
    {
        this.recs = recs;
        this.context = context;
    }




    /**
     * Indicates whether all the items in this adapter are enabled. If the
     * value returned by this method changes over time, there is no guarantee
     * it will take effect.  If true, it means all items are selectable and
     * clickable (there is no separator.)
     *
     * @return True if all items are enabled, false otherwise.
     * @see #isEnabled(int)
     */
    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    /**
     * Returns true if the item at the specified position is not a separator.
     * (A separator is a non-selectable, non-clickable item).
     * <p/>
     * The result is unspecified if position is invalid. An {@link ArrayIndexOutOfBoundsException}
     * should be thrown in that case for fast failure.
     *
     * @param position Index of the item
     * @return True if the item is not a separator
     * @see #areAllItemsEnabled()
     */
    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    /**
     * Register an observer that is called when changes happen to the data used by this adapter.
     *
     * @param observer the object that gets notified when the data set changes.
     */
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        observers.add(observer);
    }

    /**
     * Unregister an observer that has previously been registered with this
     * adapter via {@link #registerDataSetObserver}.
     *
     * @param observer the object to unregister.
     */
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        observers.remove(observer);
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return recs.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return recs.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return Util.getName(recs.get(position)).hashCode(); /* why not? */
    }

    /**
     * Indicates whether the item ids are stable across changes to the
     * underlying data.
     *
     * @return True if the same id always refers to the same object.
     */
    @Override
    public boolean hasStableIds() {
        return true; /* pretty sure this is true */
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inf = context.getLayoutInflater();
            convertView = inf.inflate(R.layout.row_layout, null);
        }
        TextView name = (TextView) convertView.findViewById(R.id.list_name_text);
        TextView dist = (TextView) convertView.findViewById(R.id.list_distance_text);
        ImageView ico = (ImageView) convertView.findViewById(R.id.list_icon);
        Map place = recs.get(position);

        ico.setImageResource(Util.isCommCenter(place) ?
                R.drawable.ic_com_center :
                R.drawable.ic_park);

        Location uLoc = context.getUserLocation();
        String distance = "";
        if(uLoc != null) {
            float[] results = new float[3];
            Location.distanceBetween(uLoc.getLatitude(), uLoc.getLongitude(),
                    Util.getLat(place), Util.getLon(place), results);
            distance = String.format("%.1f", Util.metersToMiles(results[0]));
        }
        dist.setText(distance);

        name.setText(Util.getName(place));

        return convertView;

    }

    /**
     * Get the type of View that will be created by {@link #getView} for the specified item.
     *
     * @param position The position of the item within the adapter's data set whose view type we
     *                 want.
     * @return An integer representing the type of View. Two views should share the same type if one
     * can be converted to the other in {@link #getView}. Note: Integers must be in the
     * range 0 to {@link #getViewTypeCount} - 1. {@link #IGNORE_ITEM_VIEW_TYPE} can
     * also be returned.
     * @see #IGNORE_ITEM_VIEW_TYPE
     */
    @Override
    public int getItemViewType(int position) {
        /* TODO: Generalize.  @see #getViewTypeCount */
        return 0;
    }

    /**
     * <p>
     * Returns the number of types of Views that will be created by
     * {@link #getView}. Each type represents a set of views that can be
     * converted in {@link #getView}. If the adapter always returns the same
     * type of View for all items, this method should return 1.
     * </p>
     * <p>
     * This method will only be called when when the adapter is set on the
     * the {@link android.widget.AdapterView}.
     * </p>
     *
     * @return The number of types of Views that will be created by this adapter
     */
    @Override
    public int getViewTypeCount() {
        /* TODO: Generalize? This is hard-coded.  Do we want more than one view here?
        * */
        return 1;
    }

    /**
     * @return true if this adapter doesn't contain any data.  This is used to determine
     * whether the empty view should be displayed.  A typical implementation will return
     * getCount() == 0 but since getCount() includes the headers and footers, specialized
     * adapters might want a different behavior.
     */
    @Override
    public boolean isEmpty() {
        return recs.isEmpty();
    }
}
