package com.application.splitc.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by apoorvarora on 03/10/16.
 */
public class ParserJson {

    public static final Object[] parseData (int requestType, String responseJson) throws JSONException {
        if(requestType == UploadManager.LOGIN) {
            return parse_LoginResponse(new JSONObject(responseJson));
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

}
