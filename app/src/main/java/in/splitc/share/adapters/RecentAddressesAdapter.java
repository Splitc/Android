package in.splitc.share.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import in.splitc.share.R;
import in.splitc.share.data.Address;
import in.splitc.share.data.Ride;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.GooglePlaceAutocompleteApi;
import in.splitc.share.utils.RandomCallback;

/**
 * Created by nik on 5/10/16.
 */
public class RecentAddressesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    ArrayList<Address> resultList;
    Context mContext;
    LayoutInflater inflater;
    RandomCallback callback;

    public RecentAddressesAdapter(ArrayList<Address> resultList, Context context, RandomCallback callback) {
        mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.resultList = resultList;
        this.callback = callback;
    }

    @Override
    public void onClick(View view) {
        if(view.findViewById(R.id.mainSuggestionText) != null ) {
            int position = (Integer) view.findViewById(R.id.mainSuggestionText).getTag();
            Object[] data = new Object[1];
            data[0] = resultList.get(position);
            callback.randomMethod(data);
        }
    }

    public class RideViewHolder extends RecyclerView.ViewHolder {
        public TextView mainSuggestionText, secondarySuggestionText;

        public RideViewHolder(View view) {
            super(view);
            mainSuggestionText = (TextView) view.findViewById(R.id.mainSuggestionText);
            secondarySuggestionText = (TextView) view.findViewById(R.id.secondarySuggestionText);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_address_snippet, parent, false);
        view.setOnClickListener(this);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RideViewHolder) {
            RideViewHolder rideViewHolder = (RideViewHolder) holder;
            String result = resultList.get(position).getDisplayName();
            String[] resultSplitArray = result.split(",");
            if (resultSplitArray.length > 0) {
                rideViewHolder.mainSuggestionText.setText(resultSplitArray[0].trim());
                String secondaryText = "";
                for (int i=1; i<resultSplitArray.length; i++) {
                    secondaryText = secondaryText + ", " + resultSplitArray[i].trim();
                }
                rideViewHolder.secondarySuggestionText.setText(secondaryText);
            }
            rideViewHolder.mainSuggestionText.setTag(position);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return resultList == null ? 0 : resultList.size();
    }

}
