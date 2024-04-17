package android21ktpm3.group07.androidgallery.helpers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListHelper {
    /**
     * Add an item to a list in a sorted order
     *
     * @param list       the list to add the item to
     * @param item       the item to add
     * @param comparator the comparator to compare the items
     * @param <T>        the type of the items
     */
    public static <T> void addAndMaintainSorted(List<T> list, T item, Comparator<T> comparator) {
        int index = Collections.binarySearch(list, item, comparator);
        if (index < 0) index = ~index;
        list.add(index, item);
    }
}
