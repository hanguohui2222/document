package com.gionee.hotspottransmission.history.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.history.adapter.HistoryRecordAdapter;
import com.gionee.hotspottransmission.history.bean.HistoryFileInfo;
import com.gionee.hotspottransmission.history.view.FileRecordActivity;
import com.gionee.hotspottransmission.history.view.FileRecordActivity.HistoryListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rongdd on 16-4-27.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class RecordFragment extends Fragment {

    private final String TAG = "RecordFragment";
    private static List<HistoryFileInfo> mHisList = new ArrayList<>();
    private ListView listView;
    private HistoryRecordAdapter mAdapter;
    private LinearLayout ll_no_history;
    private FrameLayout fl_record;

    private RelativeLayout rl_clear_able;
    private RelativeLayout rl_clear_disable;
    private ImageButton bt_clear;

    private Handler mHandler = new Handler() {
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case Constants.SET_IMAGE_BITMAP:
                    ImageView imageView = (ImageView)listView.findViewWithTag(msg.arg1);
                    if (imageView != null){
                        imageView.setImageBitmap((Bitmap)msg.obj);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        initView(view);
        ((FileRecordActivity)getActivity()).setHistoryListener(new HistoryListener() {
            @Override
            public void setHistoryRecord(List<HistoryFileInfo> historyFileInfoList) {
                RecordFragment.this.mHisList = historyFileInfoList;
                //added by luorw for GNSPR #47541 20161008 begin
                if (mHisList.size() != 0 && getActivity() != null) {
                    mAdapter = new HistoryRecordAdapter(getActivity(), historyFileInfoList, mHandler);
                    listView.setAdapter(mAdapter);
                    listView.requestDisallowInterceptTouchEvent(true);
                }
                //added by luorw for GNSPR #47541 20161008 end
                showView();
            }
        });
        return view;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initView(View view) {
        listView = (ListView) view.findViewById(R.id.listview_record);
        fl_record = (FrameLayout) view.findViewById(R.id.fl_record);
        ll_no_history = (LinearLayout) view.findViewById(R.id.ll_no_history);

        rl_clear_able = (RelativeLayout) view.findViewById(R.id.rl_clear_able);
        rl_clear_disable = (RelativeLayout) view.findViewById(R.id.rl_clear_disable);
        bt_clear = (ImageButton) view.findViewById(R.id.bt_clear);
    }

    private void showView() {
        if (mHisList.size() == 0) {
            fl_record.setVisibility(View.GONE);
            ll_no_history.setVisibility(View.VISIBLE);

            rl_clear_able.setVisibility(View.GONE);
            rl_clear_disable.setVisibility(View.VISIBLE);
            bt_clear.setClickable(false);
        } else {
            fl_record.setVisibility(View.VISIBLE);
            ll_no_history.setVisibility(View.GONE);

            rl_clear_able.setVisibility(View.VISIBLE);
            rl_clear_disable.setVisibility(View.GONE);
            bt_clear.setClickable(true);
            bt_clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHisList.clear();
                    ((FileRecordActivity) getActivity()).clearAll();
                }
            });
        }
    }

    public void refreshListView() {
        mAdapter.notifyDataSetChanged();
        showView();
    }

}
