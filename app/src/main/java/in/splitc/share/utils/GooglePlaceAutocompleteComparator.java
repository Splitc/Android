package in.splitc.share.utils;

import in.splitc.share.data.Address;

import java.util.Comparator;

/**
 * Created by apoorvarora on 12/10/16.
 */
public class GooglePlaceAutocompleteComparator {
    private static Comparator<Address> comparator;

    public static Comparator<Address> getComparator() {
        if (comparator == null) {
            comparator = new Comparator<Address>() {
                @Override
                public int compare(Address autocompleteObject, Address t1) {
                    return autocompleteObject.getDisplayName().compareToIgnoreCase(t1.getDisplayName());
                }
            };
        }
        return comparator;
    }
}
