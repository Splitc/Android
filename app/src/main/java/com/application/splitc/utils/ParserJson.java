package com.application.splitc.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by apoorvarora on 03/10/16.
 */
public class ParserJson {

    public static final Object[] parseData (int requestType, String responseJson) throws JSONException {
        if(requestType == UploadManager.LOGIN) {
            return parse_GenericStringResponse(new JSONObject(responseJson));
        } else
            return parse_GenericStringResponse(new JSONObject(responseJson));
    }

    public static final Object[] parse_GenericStringResponse(JSONObject responseJson) throws JSONException {

        Object[] response = new Object[]{null, false, "Something went wrong"};

        if (responseJson == null)
            return response;

        if ( responseJson.has("httpCode") && responseJson.get("httpCode") instanceof Integer && responseJson.getInt("httpCode") == 200 )
            response[1] = true;

        if ( responseJson.has("errorString") && responseJson.get("errorString") != null )
            response[2] = String.valueOf(responseJson.get("errorString"));

        if (responseJson.has("object")) {
            response[0] = String.valueOf(responseJson.get("object"));
        }

        return response;
    }

}
