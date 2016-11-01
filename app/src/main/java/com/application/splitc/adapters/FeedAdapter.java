package com.application.splitc.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.application.splitc.R;
import com.application.splitc.data.Address;
import com.application.splitc.data.Ride;
import com.application.splitc.utils.CommonLib;
import com.application.splitc.utils.OnLoadMoreListener;
import com.application.splitc.utils.RandomCallback;
import com.application.splitc.utils.UploadManager;
import com.application.splitc.views.HomeFragment;

import java.util.List;

import okhttp3.FormBody;

/**
 * Created by neo on 30/10/16.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Ride> moviesList;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    private Address startAddress;
    private Context context;
    private SharedPreferences prefs;
    private RandomCallback callback;

    public class RideViewHolder extends RecyclerView.ViewHolder {
        public TextView title, accept;

        public RideViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            accept = (TextView) view.findViewById(R.id.accept);
        }

    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    public FeedAdapter(List<Ride> moviesList, RecyclerView mRecyclerView, Address startAddress, Context context, RandomCallback callback) {
        this.moviesList = moviesList;
        this.startAddress = startAddress;
        this.context = context;
        this.callback = callback;
        prefs = context.getSharedPreferences("application_settings", 0);

        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onLoadMore();
                    }
                    isLoading = true;
                }
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_snippet, parent, false);
            return new RideViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_layout, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return moviesList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RideViewHolder) {
            final Ride movie = moviesList.get(position);
            final RideViewHolder rideViewHolder = (RideViewHolder) holder;
            rideViewHolder.title.setText(movie.getDescription()+"");
            rideViewHolder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Object[] requestParams = new Object[3];
                    requestParams[0] = movie;
                    requestParams[1] = startAddress;
                    requestParams[2] = rideViewHolder.title.getText().toString();
                    callback.randomMethod(requestParams);
                }
            });
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return moviesList == null ? 0 : moviesList.size();
    }

    public void setLoaded() {
        isLoading = false;
    }
}