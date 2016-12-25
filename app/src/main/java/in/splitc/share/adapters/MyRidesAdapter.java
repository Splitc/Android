package in.splitc.share.adapters;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.data.Ride;
import in.splitc.share.data.User;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.ImageLoader;
import in.splitc.share.utils.OnLoadMoreListener;
import in.splitc.share.utils.ZCircularImageView;

/**
 * Created by neo on 30/10/16.
 */
public class MyRidesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Ride> moviesList;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private Activity context;
    private ImageLoader loader;

    public class RideViewHolder extends RecyclerView.ViewHolder {
        public TextView start_location, drop_location, pickup_timer;
        public RideViewHolder(View view) {
            super(view);
            start_location = (TextView) view.findViewById(R.id.start_location);
            drop_location = (TextView) view.findViewById(R.id.drop_location);
            pickup_timer = (TextView) view.findViewById(R.id.pickup_timer);
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

    public MyRidesAdapter(List<Ride> moviesList, Activity context, RecyclerView mRecyclerView) {
        this.moviesList = moviesList;
        this.context = context;
        loader = new ImageLoader(context, (ZApplication) context.getApplication());
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_rides_snippet, parent, false);
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
            Ride movie = moviesList.get(position);
            RideViewHolder rideViewHolder = (RideViewHolder) holder;
            rideViewHolder.start_location.setText(movie.getFromAddress());
            rideViewHolder.drop_location.setText(movie.getToAddress());
            rideViewHolder.pickup_timer.setText(CommonLib.getTimeFormattedString(movie.getCreated()));
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