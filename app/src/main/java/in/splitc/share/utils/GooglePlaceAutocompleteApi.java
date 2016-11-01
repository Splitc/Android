package in.splitc.share.utils;

import in.splitc.share.data.Address;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Callable;

/**
 * Created by nik on 5/10/16.
 */
public class GooglePlaceAutocompleteApi implements Callable<ArrayList<String>> {

    private static final String TAG = GooglePlaceAutocompleteApi.class.getSimpleName();
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    ArrayList<String> resultList = null;
    ArrayList<Address> resultObjectList = null;
    String key = null;

    public GooglePlaceAutocompleteApi(String key) {
        this.key = key;
    }

    public ArrayList<Address> autocomplete (final String input, String typeParam, ArrayList<Address> autocompleteStaticData) {
        java.net.URL url;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=").append(key);
            sb.append("&types=(").append(typeParam).append(")");
            sb.append("&input=").append(URLEncoder.encode(input, "utf8"));
            sb.append("&components=country:ind");

            url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            return resultObjectList;
        } catch (IOException e) {
            return resultObjectList;
        } catch (Exception e) {
            return resultObjectList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        creatResultArrayList(jsonResults.toString(), autocompleteStaticData, input);
        return resultObjectList;
    }

    private void creatResultArrayList(String response, ArrayList<Address> autocompleteStaticData, String input) {
        try {
            JSONObject jsonObj = new JSONObject(response);
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            resultList = new ArrayList<String>(predsJsonArray.length());
            resultObjectList = new ArrayList<Address>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                String result = predsJsonArray.getJSONObject(i).getString("description");
                String[] resultSplitArray = result.split(",");
                ArrayList<String> autocompleteResultArrayList = new ArrayList<>();
                for (int j= resultSplitArray.length-1; j>=0; j--) {
                    if (resultSplitArray[j].trim().equalsIgnoreCase("India") || autocompleteResultArrayList.contains(resultSplitArray[j].trim())) {
                        continue;
                    }
                    autocompleteResultArrayList.add(resultSplitArray[j].trim());
                    if (autocompleteResultArrayList.size() == 3) {
                        break;
                    }
                }
                Collections.reverse(autocompleteResultArrayList);
                String suggestionText = "";
                if (autocompleteResultArrayList != null && autocompleteResultArrayList.size() > 0) {
                    suggestionText += autocompleteResultArrayList.get(0).trim();
                    for (int j=1; j<autocompleteResultArrayList.size(); j++) {
                        suggestionText = suggestionText + ", " + autocompleteResultArrayList.get(j).trim();
                    }
                }
                if (!resultList.contains(suggestionText)) {
                    resultList.add(suggestionText);
                    Address temp = new Address();
                    temp.setDisplayName(suggestionText);
                    temp.setPlaceId(predsJsonArray.getJSONObject(i).getString("place_id"));
                    resultObjectList.add(temp);
                }
            }
            Comparator<Address> comparator = GooglePlaceAutocompleteComparator.getComparator();
            if (autocompleteStaticData != null && autocompleteStaticData.size() > 0) {
                int flag = 0;
                for (Address temp : autocompleteStaticData) {
                    if (temp.getDisplayName().toLowerCase().contains(input.toLowerCase()) && temp.getDisplayName().substring(0, 1).equalsIgnoreCase(input.substring(0, 1))) {
                        if (!resultList.contains(temp.getDisplayName())) {
                            resultList.add(temp.getDisplayName());
                            resultObjectList.add(temp);
                            flag = 1;
                        }
                    }
                }
                if (flag == 1) {
                    Collections.sort(resultObjectList, comparator);
                }
            }
            Address poweredByGoogleObject = new Address();
            poweredByGoogleObject.setDisplayName("PoweredByGoogle");
            poweredByGoogleObject.setPlaceId(null);
            resultObjectList.add(poweredByGoogleObject);
        } catch (JSONException e) {
        }
    }

    @Override
    public ArrayList<String> call() throws Exception {
        return null;
    }
}