package com.application.splitc.data;

import java.io.Serializable;

/**
 * Created by apoorvarora on 12/10/16.
 */
public class GooglePlaceAutocompleteObject implements Serializable {
    private String displayName;
    private String id;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
