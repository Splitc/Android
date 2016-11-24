package in.splitc.share.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.data.Feed;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.ImageLoader;
import in.splitc.share.utils.OnLoadMoreListener;
import in.splitc.share.utils.RandomCallback;
import in.splitc.share.views.FeedItemDetailActivity;

/**
 * Created by neo on 30/10/16.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{

    private List<Feed> feedItems, filteredList;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean isLoading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    private Activity context;
    private ImageLoader loader;

    private int width, height;

    @Override
    public void onClick(View view) {
        int position = (Integer) view.getTag();
        Feed currentRide = feedItems.get(position);

        Intent intent = new Intent(context, FeedItemDetailActivity.class);
        intent.putExtra("ride", currentRide);
        context.startActivity(intent);
    }

    public class RideViewHolder extends RecyclerView.ViewHolder {
        public TextView user_trip_title, start_location, drop_location, pickup_timer;
        public ImageView user_image;
        public CardView feed_snippet_container;

        public RideViewHolder(View view) {
            super(view);
            user_trip_title = (TextView) view.findViewById(R.id.user_trip_title);
            start_location = (TextView) view.findViewById(R.id.start_location);
            drop_location = (TextView) view.findViewById(R.id.drop_location);
            pickup_timer = (TextView) view.findViewById(R.id.pickup_timer);
            user_image = (ImageView) view.findViewById(R.id.user_image);
            feed_snippet_container = (CardView) view.findViewById(R.id.feed_snippet_container);
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

    public FeedAdapter(List<Feed> feedItems, RecyclerView mRecyclerView, Activity context) {
        this.feedItems = feedItems;
        this.context = context;
        loader = new ImageLoader(context, (ZApplication) context.getApplication());
        this.width = context.getWindowManager().getDefaultDisplay().getWidth();
        this.height = context.getWindowManager().getDefaultDisplay().getHeight();

        this.filteredList = new ArrayList<Feed>();
        this.filteredList.addAll(feedItems);

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

    public void filter(final int type) {
        // Clear the filter list
        filteredList.clear();

        if (type == -1) {
            filteredList.addAll(feedItems);
            notifyDataSetChanged();
            return;
        }

        for (Feed feedItem : feedItems) {
            if(feedItem.getFeedType() == type)
                filteredList.add(feedItem);
        }

        // change the data set
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_snippet, parent, false);
            view.setOnClickListener(this);
            return new RideViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_layout, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return feedItems.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RideViewHolder) {
            final Feed feedItem = feedItems.get(position);
            final RideViewHolder rideViewHolder = (RideViewHolder) holder;
            rideViewHolder.start_location.setText(feedItem.getFromAddress());
            rideViewHolder.drop_location.setText(feedItem.getToAddress());
            rideViewHolder.pickup_timer.setText(CommonLib.getTimeFormattedString(feedItem.getCreated()));
            if (feedItem.getFeedType() == CommonLib.FEED_TYPE_RIDE) {
                rideViewHolder.user_trip_title.setText(context.getResources().getString(R.string.travel_title_string, feedItem.getUser().getUserName()));
            } else
                rideViewHolder.user_trip_title.setText(context.getResources().getString(R.string.need_ride_string, feedItem.getUser().getUserName()));

            if (feedItem.getUser().getProfilePic() != null)
                loader.setImageFromUrlOrDisk(feedItem.getUser().getProfilePic(), rideViewHolder.user_image, "", width, height, false);
            else
                rideViewHolder.user_image.setImageBitmap(CommonLib.getBitmap(context, R.drawable.user, width, height));
            rideViewHolder.feed_snippet_container.setTag(position);
            rideViewHolder.user_image.setTag(position);
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return feedItems == null ? 0 : feedItems.size();
    }

    public void setLoaded() {
        isLoading = false;
    }
}