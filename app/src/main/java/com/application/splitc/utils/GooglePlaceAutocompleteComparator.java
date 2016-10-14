package com.application.splitc.utils;

import com.application.splitc.data.GooglePlaceAutocompleteObject;

import java.util.Comparator;

/**
 * Created by apoorvarora on 12/10/16.
 */
public class GooglePlaceAutocompleteComparator {
    private static Comparator<GooglePlaceAutocompleteObject> comparator;

    public static Comparator<GooglePlaceAutocompleteObject> getComparator() {
        if (comparator == null) {
            comparator = new Comparator<GooglePlaceAutocompleteObject>() {
                @Override
                public int compare(GooglePlaceAutocompleteObject autocompleteObject, GooglePlaceAutocompleteObject t1) {
                    return autocompleteObject.getDisplayName().compareToIgnoreCase(t1.getDisplayName());
                }
            };
        }
        return comparator;
    }
}
