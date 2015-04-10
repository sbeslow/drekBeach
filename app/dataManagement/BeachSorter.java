package dataManagement;

import models.Beach;

import java.util.*;

/**
 * Created by scottbeslow on 4/7/15.
 */
public abstract class BeachSorter {


    public static List<Beach> sortByShittiest(List<Beach> beachList) {
        HashMap<Beach, Integer> map = new HashMap<>();
        ValueComparator bvc = new ValueComparator(map);
        TreeMap<Beach, Integer> sorted_map = new TreeMap<Beach, Integer>(bvc);

        for (Beach beach : beachList) {
            map.put(beach, beach.getSeasonalStats().score());
        }

        sorted_map.putAll(map);

        return new ArrayList<>(sorted_map.keySet());
    }
}

class ValueComparator implements Comparator<Beach> {

    Map<Beach, Integer> base;
    public ValueComparator(Map<Beach, Integer> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.
    public int compare(Integer a, Integer b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }

    @Override
    public int compare(Beach o1, Beach o2) {
        if (base.get(o1) >= base.get(o2)) {
            return -1;
        } else {
            return 1;
        }
    }
}