package in.splitc.share.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import in.splitc.share.data.Feed;
import in.splitc.share.data.Ride;
import in.splitc.share.data.User;

/**
 * Created by apoorvarora on 03/10/16.
 */
public class ParserJson {

    public static final Object[] parseData (int requestType, String responseJson) throws JSONException {
        if(requestType == UploadManager.LOGIN) {
            return parse_LoginResponse(new JSONObject(responseJson));
        } else if(requestType == UploadManager.NEW_RIDE || requestType == UploadManager.FEED_RIDE_ACCEPT) {
            return parse_NewRideJson(new JSONObject(responseJson));
        } else if(requestType == UploadManager.FETCH_RIDES || requestType == UploadManager.FETCH_RIDES_LOAD_MORE) {
            return parse_RidesJson(new JSONObject(responseJson));
        } else if(requestType == UploadManager.FEED_RIDES || requestType == UploadManager.FEED_RIDES_LOAD_MORE) {
            return parse_FeedJson(new JSONObject(responseJson));
        } else
            return parse_GenericStringResponse(new JSONObject(responseJson));
    }

    public static final Object[] parse_LoginResponse(JSONObject responseJson) throws JSONException {

        Object[] response = new Object[] { null, false, "Something went wrong"};

        if (responseJson == null)
            return response;

        if (responseJson.has("status")) {
            if (responseJson.getString("status").equals("success")) {
                response[1] = true;
                if (responseJson.has("response")) {
                    response[0] = new JSONObject(String.valueOf(responseJson.get("response")));
                }
            } else {
                if (responseJson.has("errorMessage")) {
                    response[2] = responseJson.getString("errorMessage");
                }
            }
        }

        return response;
    }

    public static final Object[] parse_GenericStringResponse(JSONObject responseJson) throws JSONException {

        Object[] response = new Object[] { null, false, "Something went wrong"};

        if (responseJson == null)
            return response;

        if (responseJson.has("status")) {
            if (responseJson.getString("status").equals("success")) {
                response[1] = true;
                if (responseJson.has("response")) {
                    response[0] = String.valueOf(responseJson.get("response"));
                }
            } else {
                if (responseJson.has("errorMessage")) {
                    response[2] = responseJson.getString("errorMessage");
                }
            }
        }

        return response;
    }

    public static final Object[] parse_NewRideJson(JSONObject responseJson) throws JSONException {

        Object[] response = new Object[] { null, false, "Something went wrong"};

        if (responseJson == null)
            return response;

        if (responseJson.has("status")) {
            if (responseJson.getString("status").equals("success")) {
                response[1] = true;
                if (responseJson.has("response") && responseJson.get("response") instanceof JSONObject) {
                    response[0] = parse_Ride(responseJson.getJSONObject("response"));
                }
            } else {
                if (responseJson.has("errorMessage")) {
                    response[2] = responseJson.getString("errorMessage");
                }
            }
        }

        return response;
    }

    public static final Object[] parse_RidesJson(JSONObject responseJson) throws JSONException {

        Object[] response = new Object[] { null, false, "Something went wrong"};

        if (responseJson == null)
            return response;

        if (responseJson.has("status")) {
            if (responseJson.getString("status").equals("success")) {
                response[1] = true;
                if (responseJson.has("response") && responseJson.get("response") instanceof JSONObject) {
                    JSONObject responseJSONObject = responseJson.getJSONObject("response");

                    int size = 0;
                    ArrayList<Ride> myRides = new ArrayList<Ride>();

                    if(responseJSONObject.has("total") && responseJSONObject.get("total") instanceof Integer)
                        size = responseJSONObject.getInt("total");

                    if(responseJSONObject.has("rides") && responseJSONObject.get("rides") instanceof JSONArray) {
                        JSONArray ridesJsonArray = responseJSONObject.getJSONArray("rides");

                        for (int i=0; i<ridesJsonArray.length(); i++) {
                            Ride ride = parse_Ride(ridesJsonArray.getJSONObject(i));
                            myRides.add(ride);
                        }
                    }

                    Object[] output = new Object[2];
                    output[0] = size;
                    output[1] = myRides;

                    response[0] = output;
                }
            } else {
                if (responseJson.has("errorMessage")) {
                    response[2] = responseJson.getString("errorMessage");
                }
            }
        }

        return response;
    }

    public static final Object[] parse_FeedJson(JSONObject responseJson) throws JSONException {

        Object[] response = new Object[] { null, false, "Something went wrong"};

        if (responseJson == null)
            return response;

        if (responseJson.has("status")) {
            if (responseJson.getString("status").equals("success")) {
                response[1] = true;
                if (responseJson.has("response") && responseJson.get("response") instanceof JSONObject) {
                    JSONObject responseJSONObject = responseJson.getJSONObject("response");

                    int size = 0;
                    ArrayList<Feed> myRides = new ArrayList<Feed>();

                    if(responseJSONObject.has("total") && responseJSONObject.get("total") instanceof Integer)
                        size = responseJSONObject.getInt("total");

                    if(responseJSONObject.has("feedItems") && responseJSONObject.get("feedItems") instanceof JSONArray) {
                        JSONArray ridesJsonArray = responseJSONObject.getJSONArray("feedItems");

                        for (int i=0; i<ridesJsonArray.length(); i++) {
                            Feed ride = parse_Feed(ridesJsonArray.getJSONObject(i));
                            myRides.add(ride);
                        }
                    }

                    Object[] output = new Object[2];
                    output[0] = size;
                    output[1] = myRides;

                    response[0] = output;
                }
            } else {
                if (responseJson.has("errorMessage")) {
                    response[2] = responseJson.getString("errorMessage");
                }
            }
        }

        return response;
    }

    public static final Ride parse_Ride(JSONObject rideJson) throws JSONException {
        if (rideJson == null)
            return null;

        Ride ride = new Ride();

        if (rideJson.has("fromAddress")) {
            ride.setFromAddress(String.valueOf(rideJson.get("fromAddress")));
        }

        if (rideJson.has("rideId") && rideJson.get("rideId") instanceof Integer) {
            ride.setRideId(rideJson.getInt("rideId"));
        }

        if (rideJson.has("startLat") && rideJson.get("startLat") instanceof Double) {
            ride.setStartLat(rideJson.getDouble("startLat"));
        }

        if (rideJson.has("startLon") && rideJson.get("startLon") instanceof Double) {
            ride.setStartLon(rideJson.getDouble("startLon"));
        }

        if (rideJson.has("startGooglePlaceId")) {
            ride.setStartGooglePlaceId(String.valueOf(rideJson.get("startGooglePlaceId")));
        }

        if (rideJson.has("toAddress")) {
            ride.setToAddress(String.valueOf(rideJson.get("toAddress")));
        }

        if (rideJson.has("dropLat") && rideJson.get("dropLat") instanceof Double) {
            ride.setDropLat(rideJson.getDouble("dropLat"));
        }

        if (rideJson.has("dropLon") && rideJson.get("dropLon") instanceof Double) {
            ride.setDropLon(rideJson.getDouble("dropLon"));
        }

        if (rideJson.has("dropGooglePlaceId")) {
            ride.setDropGooglePlaceId(String.valueOf(rideJson.get("dropGooglePlaceId")));
        }

        if (rideJson.has("status") && rideJson.get("status") instanceof Integer) {
            ride.setStatus(rideJson.getInt("status"));
        }

        if (rideJson.has("created") && rideJson.get("created") instanceof Long) {
            ride.setCreated(rideJson.getLong("created"));
        }

        if (rideJson.has("startTime") && rideJson.get("startTime") instanceof Long) {
            ride.setStartTime(rideJson.getLong("startTime"));
        }

        if (rideJson.has("requiredPersons") && rideJson.get("requiredPersons") instanceof Integer) {
            ride.setRequiredPersons(rideJson.getInt("requiredPersons"));
        }

        if (rideJson.has("description")) {
            ride.setDescription(String.valueOf(rideJson.get("description")));
        }

        if (rideJson.has("user") && rideJson.get("user") instanceof JSONObject) {
            ride.setUser(parse_User(rideJson.getJSONObject("user")));
        }

        if (rideJson.has("accepted_users") && rideJson.get("accepted_users") instanceof JSONArray) {
            ArrayList<User> acceptedUsers = new ArrayList<User>();
            JSONArray usersArr = rideJson.getJSONArray("accepted_users");
            for(int i=0; i<usersArr.length(); i++) {
                User user = parse_User(usersArr.getJSONObject(i));
                acceptedUsers.add(user);
            }
            ride.setAcceptedUsers(acceptedUsers);
        }

        return ride;
    }

    public static final Feed parse_Feed(JSONObject rideJson) throws JSONException {
        if (rideJson == null)
            return null;

        Feed ride = new Feed();

        if (rideJson.has("fromAddress")) {
            ride.setFromAddress(String.valueOf(rideJson.get("fromAddress")));
        }

        if (rideJson.has("feedId") && rideJson.get("feedId") instanceof Integer) {
            ride.setFeedId(rideJson.getInt("feedId"));
        }

        if (rideJson.has("startLat") && rideJson.get("startLat") instanceof Double) {
            ride.setStartLat(rideJson.getDouble("startLat"));
        }

        if (rideJson.has("startLon") && rideJson.get("startLon") instanceof Double) {
            ride.setStartLon(rideJson.getDouble("startLon"));
        }

        if (rideJson.has("startGooglePlaceId")) {
            ride.setStartGooglePlaceId(String.valueOf(rideJson.get("startGooglePlaceId")));
        }

        if (rideJson.has("toAddress")) {
            ride.setToAddress(String.valueOf(rideJson.get("toAddress")));
        }

        if (rideJson.has("dropLat") && rideJson.get("dropLat") instanceof Double) {
            ride.setDropLat(rideJson.getDouble("dropLat"));
        }

        if (rideJson.has("dropLon") && rideJson.get("dropLon") instanceof Double) {
            ride.setDropLon(rideJson.getDouble("dropLon"));
        }

        if (rideJson.has("dropGooglePlaceId")) {
            ride.setDropGooglePlaceId(String.valueOf(rideJson.get("dropGooglePlaceId")));
        }

        if (rideJson.has("status") && rideJson.get("status") instanceof Integer) {
            ride.setStatus(rideJson.getInt("status"));
        }

        if (rideJson.has("created") && rideJson.get("created") instanceof Long) {
            ride.setCreated(rideJson.getLong("created"));
        }

        if (rideJson.has("startTime") && rideJson.get("startTime") instanceof Long) {
            ride.setStartTime(rideJson.getLong("startTime"));
        }

        if (rideJson.has("requiredPersons") && rideJson.get("requiredPersons") instanceof Integer) {
            ride.setRequiredPersons(rideJson.getInt("requiredPersons"));
        }

        if (rideJson.has("description")) {
            ride.setDescription(String.valueOf(rideJson.get("description")));
        }

        if (rideJson.has("user") && rideJson.get("user") instanceof JSONObject) {
            ride.setUser(parse_User(rideJson.getJSONObject("user")));
        }

        return ride;
    }

    public static final User parse_User(JSONObject userJson) throws JSONException {
        if (userJson == null)
            return null;

        User user = new User();

        if (userJson.has("userId") && userJson.get("userId") instanceof Integer) {
            user.setUserId(userJson.getInt("userId"));
        }

        if (userJson.has("user_name")) {
            user.setUserName(String.valueOf(userJson.get("user_name")));
        }

        if (userJson.has("profilePic")) {
            user.setProfilePic(String.valueOf(userJson.get("profilePic")));
        }

        return user;
    }

}
