package in.splitc.share.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import in.splitc.share.R;
import in.splitc.share.data.Address;
import in.splitc.share.utils.GooglePlaceAutocompleteApi;

import java.util.ArrayList;

/**
 * Created by nik on 5/10/16.
 */
public class GooglePlaceAutocompleteAdapter extends BaseAdapter implements Filterable {
    ArrayList<Address> resultList;
    Context mContext;
    LayoutInflater inflater;
    String typeParam;
    GooglePlaceAutocompleteApi mPlaceAPI;
    ArrayList<Address> autocompleteStaticData = null;

    public GooglePlaceAutocompleteAdapter(Context context, String typeParam, String key) {
        mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.typeParam = typeParam;
        mPlaceAPI = new GooglePlaceAutocompleteApi(key);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        //noinspection RedundantIfStatement
        return position != (resultList.size() - 1);
    }

    @Override
    public Address getItem(int position) {
        return resultList.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_google_autocomplete_suggestion, null);
        }
        if (position == (resultList.size() - 1)) {
            convertView.findViewById(R.id.powered_by_google_image).setVisibility(View.VISIBLE);
            ((TextView) convertView.findViewById(R.id.mainSuggestionText)).setText("");
            ((TextView) convertView.findViewById(R.id.secondarySuggestionText)).setText("");
        } else {
            convertView.findViewById(R.id.powered_by_google_image).setVisibility(View.GONE);
            String result = resultList.get(position).getDisplayName();
            String[] resultSplitArray = result.split(",");
            if (resultSplitArray.length > 0) {
                ((TextView) convertView.findViewById(R.id.mainSuggestionText)).setText(resultSplitArray[0].trim());
                String secondaryText = "";
                for (int i=1; i<resultSplitArray.length; i++) {
                    secondaryText = secondaryText + ", " + resultSplitArray[i].trim();
                }
                ((TextView) convertView.findViewById(R.id.secondarySuggestionText)).setText(secondaryText);
            }
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    resultList = mPlaceAPI.autocomplete(constraint.toString(), typeParam, autocompleteStaticData);
                    if (resultList != null) {
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }
}
