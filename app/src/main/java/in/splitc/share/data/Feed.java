package in.splitc.share.data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by neo on 20/11/16.
 */
public class Feed implements Serializable {

    private int feedId;
    private int feedType; // FEED_TYPE_RIDE, FEED_TYPE_RIDE_REQUEST

    private String fromAddress;
    private double startLat;
    private double startLon;
    private String startGooglePlaceId;

    private String toAddress;
    private double dropLat;
    private double dropLon;
    private String dropGooglePlaceId;

    private int status;
    private long created;
    private long startTime;

    private int requiredPersons;
    private String description;

    private int userId; // wish posting user
    private User user;

    public Feed(){}

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }

    public int getFeedType() {
        return feedType;
    }

    public void setFeedType(int feedType) {
        this.feedType = feedType;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public double getStartLat() {
        return startLat;
    }

    public void setStartLat(double startLat) {
        this.startLat = startLat;
    }

    public double getStartLon() {
        return startLon;
    }

    public void setStartLon(double startLon) {
        this.startLon = startLon;
    }

    public String getStartGooglePlaceId() {
        return startGooglePlaceId;
    }

    public void setStartGooglePlaceId(String startGooglePlaceId) {
        this.startGooglePlaceId = startGooglePlaceId;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public double getDropLat() {
        return dropLat;
    }

    public void setDropLat(double dropLat) {
        this.dropLat = dropLat;
    }

    public double getDropLon() {
        return dropLon;
    }

    public void setDropLon(double dropLon) {
        this.dropLon = dropLon;
    }

    public String getDropGooglePlaceId() {
        return dropGooglePlaceId;
    }

    public void setDropGooglePlaceId(String dropGooglePlaceId) {
        this.dropGooglePlaceId = dropGooglePlaceId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getRequiredPersons() {
        return requiredPersons;
    }

    public void setRequiredPersons(int requiredPersons) {
        this.requiredPersons = requiredPersons;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}