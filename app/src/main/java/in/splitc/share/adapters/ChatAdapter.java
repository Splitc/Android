package in.splitc.share.adapters;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import in.splitc.share.R;
import in.splitc.share.ZApplication;
import in.splitc.share.data.Message;
import in.splitc.share.utils.CommonLib;
import in.splitc.share.utils.ImageLoader;

/**
 * Created by neo on 13/12/16.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<Message> messages;

    private Activity context;
    private ImageLoader loader;
    private SharedPreferences prefs;
    private int width, height;
    private int myUserId;

    public class RideViewHolder extends RecyclerView.ViewHolder {
        public TextView message_preview_left, date_left, message_preview_right, date_right;
        public RelativeLayout right_container, left_container;

        public RideViewHolder(View view) {
            super(view);
            message_preview_left = (TextView) view.findViewById(R.id.message_preview_left);
            date_left = (TextView) view.findViewById(R.id.date_left);
            message_preview_right = (TextView) view.findViewById(R.id.message_preview_right);
            date_right = (TextView) view.findViewById(R.id.date_right);
            right_container = (RelativeLayout) view.findViewById(R.id.right_container);
            left_container = (RelativeLayout) view.findViewById(R.id.left_container);
        }
    }

    public ChatAdapter(List<Message> messages, Activity context) {
        this.messages = messages;
        this.context = context;
        loader = new ImageLoader(context, (ZApplication) context.getApplication());
        this.width = context.getWindowManager().getDefaultDisplay().getWidth();
        this.height = context.getWindowManager().getDefaultDisplay().getHeight();
        prefs = context.getSharedPreferences("application_settings", 0);
        myUserId = prefs.getInt("userId", 0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_snippet, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RideViewHolder) {
            final Message message = messages.get(position);
            final RideViewHolder rideViewHolder = (RideViewHolder) holder;

            if (message.getSender() == myUserId) {
                rideViewHolder.message_preview_right.setText(message.getMessage());
                rideViewHolder.date_right.setText(CommonLib.convertSecondsToTime(message.getTimestamp()));
                rideViewHolder.left_container.setVisibility(View.GONE);
                rideViewHolder.right_container.setVisibility(View.VISIBLE);
            } else {
                rideViewHolder.message_preview_left.setText(message.getMessage());
                rideViewHolder.date_left.setText(CommonLib.convertSecondsToTime(message.getTimestamp()));
                rideViewHolder.right_container.setVisibility(View.GONE);
                rideViewHolder.left_container.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
    }

}