package in.splitc.share.adapters;

import android.app.Activity;
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
import in.splitc.share.data.Message;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.ImageLoader;
import in.splitc.share.utils.OnLoadMoreListener;
import in.splitc.share.views.ChatActivity;
import in.splitc.share.views.UserProfileActivity;

/**
 * Created by neo on 30/10/16.
 */
public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{

    private List<Message> feedItems;

    private Activity context;
    private ImageLoader loader;
    private SharedPreferences prefs;
    private int width, height;

    @Override
    public void onClick(View view) {
        int position = (Integer) view.getTag();
        Message currentRide = feedItems.get(position);
        switch (view.getId()) {
            case R.id.feed_snippet_container: {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userId", currentRide.getTo() == prefs.getInt("userId", 0) ? currentRide.getSender():currentRide.getTo());
                context.startActivity(intent);
            }
        }
    }

    public class RideViewHolder extends RecyclerView.ViewHolder {
        public TextView user_name, last_message, start_location, drop_location;
        public ImageView user_image;
        public CardView feed_snippet_container;

        public RideViewHolder(View view) {
            super(view);
            user_name = (TextView) view.findViewById(R.id.user_name);
            start_location = (TextView) view.findViewById(R.id.start_location);
            drop_location = (TextView) view.findViewById(R.id.drop_location);
            last_message = (TextView) view.findViewById(R.id.last_message);
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

    public UsersAdapter(List<Message> feedItems, Activity context) {
        this.feedItems = feedItems;
        this.context = context;
        loader = new ImageLoader(context, (ZApplication) context.getApplication());
        this.width = context.getWindowManager().getDefaultDisplay().getWidth();
        this.height = context.getWindowManager().getDefaultDisplay().getHeight();
        prefs = context.getSharedPreferences("application_settings", 0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_snippet, parent, false);
        view.setOnClickListener(this);
        view.findViewById(R.id.user_image).setOnClickListener(this);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RideViewHolder) {
            final Message feedItem = feedItems.get(position);
            final RideViewHolder rideViewHolder = (RideViewHolder) holder;
            rideViewHolder.last_message.setText(feedItem.getMessage());
            rideViewHolder.user_name.setText(feedItem.getUserName());
            if (feedItem.getProfilePic() != null)
                loader.setImageFromUrlOrDisk(feedItem.getProfilePic(), rideViewHolder.user_image, "", width, height, false);
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

}