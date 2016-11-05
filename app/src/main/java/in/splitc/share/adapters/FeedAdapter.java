package in.splitc.share.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.data.Address;
import in.splitc.share.data.Ride;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.ImageLoader;
import in.splitc.share.utils.OnLoadMoreListener;
import in.splitc.share.utils.RandomCallback;

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
    private ImageLoader loader;

    private int width, height;

    public class RideViewHolder extends RecyclerView.ViewHolder {
        public TextView user_trip_title, start_location, drop_location, pickup_timer, description, accept;
        public ImageView user_image;
        public RideViewHolder(View view) {
            super(view);
            user_trip_title = (TextView) view.findViewById(R.id.user_trip_title);
            start_location = (TextView) view.findViewById(R.id.start_location);
            drop_location = (TextView) view.findViewById(R.id.drop_location);
            pickup_timer = (TextView) view.findViewById(R.id.pickup_timer);
            description = (TextView) view.findViewById(R.id.description);
            user_image = (ImageView) view.findViewById(R.id.user_image);
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

    public FeedAdapter(List<Ride> moviesList, RecyclerView mRecyclerView, Address startAddress, Context context, RandomCallback callback, ZApplication zapp, int width, int height) {
        this.moviesList = moviesList;
        this.startAddress = startAddress;
        this.context = context;
        this.callback = callback;
        prefs = context.getSharedPreferences("application_settings", 0);
        loader = new ImageLoader(context, zapp);
        this.width = width;
        this.height = height;

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
            rideViewHolder.start_location.setText(movie.getFromAddress());
            rideViewHolder.drop_location.setText(movie.getToAddress());
            rideViewHolder.pickup_timer.setText(CommonLib.getTimeFormattedString(movie.getCreated()));
            rideViewHolder.description.setText(movie.getDescription());
            rideViewHolder.user_trip_title.setText(context.getResources().getString(R.string.travel_title_string, movie.getUser().getUserName()));
            loader.setImageFromUrlOrDisk(movie.getUser().getProfilePic(), rideViewHolder.user_image, "", width, height, false);

            rideViewHolder.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Object[] requestParams = new Object[3];
                    requestParams[0] = movie;
                    requestParams[1] = startAddress;
//                    requestParams[2] = rideViewHolder.title.getText().toString();
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