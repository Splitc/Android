package com.application.splitc.views;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.application.splitc.R;
import com.application.splitc.ZApplication;
import com.application.splitc.adapters.MyRidesAdapter;
import com.application.splitc.data.Ride;
import com.application.splitc.utils.CommonLib;
import com.application.splitc.utils.OnLoadMoreListener;
import com.application.splitc.utils.UploadManager;
import com.application.splitc.utils.UploadManagerCallback;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;

/**
 * Created by apoorvarora on 12/10/16.
 */
public class MyRidesFragment extends Fragment implements UploadManagerCallback {

    public static final  String TAG = MyRidesFragment.class.getSimpleName();
    private View mView;
    ZApplication zapp;
    private int width, height;
    private SharedPreferences prefs;
    private Activity activity;
    private boolean destroyed = false;
    private View getView;
    private RecyclerView recyclerView;
    private MyRidesAdapter mAdapter;
    List<Ride> rides = new ArrayList<>();

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private int mTotalRides = 0;
    private int start = 0;
    private int count = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_my_rides, container, false);
        destroyed = false;
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        getView = getView();
        prefs = activity.getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) getActivity().getApplication();
        width = activity.getWindowManager().getDefaultDisplay().getWidth();
        height = activity.getWindowManager().getDefaultDisplay().getHeight();
        mSwipeRefreshLayout = (SwipeRefreshLayout) getView.findViewById(R.id.swiperefresh);

        destroyed = false;
        UploadManager.addCallback(this);

        recyclerView = (RecyclerView) getView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        refreshView();

        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {

                start = rides.size();

                rides.add(null);
                mAdapter.notifyItemInserted(rides.size() - 1);

                String url = CommonLib.SERVER_URL + "ride/fetch?start=" + start + "&count=" + count;
                FormBody.Builder requestBuilder = new FormBody.Builder();
                requestBuilder.add("access_token", prefs.getString("access_token", ""));
                requestBuilder.add("client_id", CommonLib.CLIENT_ID);
                requestBuilder.add("app_type", CommonLib.APP_TYPE);
                UploadManager.postDataToServer(UploadManager.FETCH_RIDES_LOAD_MORE, url, requestBuilder);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshView();
                    }
                }
        );

    }

    private void refreshView() {
        rides = new ArrayList<Ride>();
        mAdapter = new MyRidesAdapter(rides, recyclerView);
        recyclerView.setAdapter(mAdapter);

        String url = CommonLib.SERVER_URL + "ride/fetch?start=" + 0 + "&count=" + count;
        FormBody.Builder requestBuilder = new FormBody.Builder();
        requestBuilder.add("access_token", prefs.getString("access_token", ""));
        requestBuilder.add("client_id", CommonLib.CLIENT_ID);
        requestBuilder.add("app_type", CommonLib.APP_TYPE);
        UploadManager.postDataToServer(UploadManager.FETCH_RIDES, url, requestBuilder);
    }

    @Override
    public void uploadStarted(int requestType, Object data) {

    }

    @Override
    public void uploadFinished(int requestType, Object data, boolean status, String errorMessage) {
        if (requestType == UploadManager.FETCH_RIDES) {
            if(!destroyed && status && data instanceof Object[] && ((Object[]) data).length == 3) {
                Object[] output = (Object[]) ((Object[]) data)[0];

                mTotalRides = (int) output[0];
                rides.addAll((ArrayList<Ride>) output[1]);
                mAdapter.notifyDataSetChanged();
                if (mTotalRides <= rides.size()) {
                    mAdapter.setLoaded();
                }

                mSwipeRefreshLayout.setRefreshing(false);
            }
        } else if (requestType == UploadManager.FETCH_RIDES_LOAD_MORE) {
            if(!destroyed && status) {
                Object[] output = (Object[]) ((Object[]) data)[0];
                rides.remove(rides.size() - 1);
                mAdapter.notifyItemRemoved(rides.size());

                mTotalRides = (int) output[0];
                rides.addAll((ArrayList<Ride>) output[1]);
                mAdapter.notifyDataSetChanged();
                if (mTotalRides <= rides.size()) {
                    mAdapter.setLoaded();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        UploadManager.removeCallback(this);
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        destroyed = true;
        super.onDestroyView();
    }

}
