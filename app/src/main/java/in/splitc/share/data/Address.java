package in.splitc.share.data;

import java.io.Serializable;

/**
 * Created by neo on 01/11/16.
 */
public class Address implements Serializable {

    private double latitude;
    private double longitude;
    private String placeId;
    private String displayName;

    public Address(){}

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
