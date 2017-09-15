package com.gionee.hotspottransmission.history.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.history.adapter.DeviceAdapter;
import com.gionee.hotspottransmission.history.bean.DeviceInfo;
import com.gionee.hotspottransmission.history.view.FileRecordActivity;
import com.gionee.hotspottransmission.history.view.FileRecordActivity.DeviceInfoListener;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备
 * Created by rongdd on 16-4-27.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DeviceFragment extends Fragment {

    private final String TAG = "DeviceFragment";
    private List<DeviceInfo> mDeviceInfos = new ArrayList<>();
    private DeviceAdapter mAdapter;
    private FrameLayout fl_list_device;
    private ListView listView;
    private TextView tv_device_count;

    private LinearLayout ll_has_devices;
    private LinearLayout ll_no_device;
    private RelativeLayout rl_clear_able;
    private RelativeLayout rl_clear_disable;
    private ImageButton bt_clear;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i("设备 onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtil.i("设备 onCreateView");
        View view = inflater.inflate(R.layout.fragment_device, container, false);
        findViews(view);
        ((FileRecordActivity)getActivity()).setDeviceInfoListener(new DeviceInfoListener() {
            @Override
            public void setDevice(List<DeviceInfo> deviceInfos) {
                LogUtil.i("设备 devicefragment set deviceInfoListener");
                DeviceFragment.this.mDeviceInfos = deviceInfos;

                if (mDeviceInfos.size() != 0) {
                    tv_device_count.setText("连接过的设备(" + mDeviceInfos.size() + ")");
                    mAdapter = new DeviceAdapter(getActivity(), mDeviceInfos);
                    listView.setAdapter(mAdapter);
                }

                initView();
            }
        });

        return view;
    }

    private void findViews(View view){
        fl_list_device = (FrameLayout) view.findViewById(R.id.fl_list_device);
        listView = (ListView) view.findViewById(R.id.listview_device);
        tv_device_count = (TextView) view.findViewById(R.id.tv_device_count);

        ll_no_device = (LinearLayout) view.findViewById(R.id.ll_no_device);
        ll_has_devices = (LinearLayout) view.findViewById(R.id.ll_has_devices);

        rl_clear_able = (RelativeLayout) view.findViewById(R.id.rl_clear_able);
        rl_clear_disable = (RelativeLayout) view.findViewById(R.id.rl_clear_disable);
        bt_clear = (ImageButton) view.findViewById(R.id.bt_clear);

    }

    private void initView() {
        if(mDeviceInfos.size() == 0){
        	if(ll_has_devices != null){
        		ll_has_devices.setVisibility(View.GONE);
            }
        	if(ll_no_device != null){
        		ll_no_device.setVisibility(View.VISIBLE);
            }
        	if(rl_clear_able != null){
        		rl_clear_able.setVisibility(View.GONE);
            }
        	if(rl_clear_disable != null){
        		rl_clear_disable.setVisibility(View.VISIBLE);
            }
        	if(bt_clear != null){
        		bt_clear.setClickable(false);
            }
        }else {
        	if(ll_has_devices != null){
        		ll_has_devices.setVisibility(View.VISIBLE);
            }
        	if(ll_no_device != null){
        		ll_no_device.setVisibility(View.GONE);
        	}
        	if(rl_clear_able != null){
        		rl_clear_able.setVisibility(View.VISIBLE);
            }
        	if(rl_clear_disable != null){
        		rl_clear_disable.setVisibility(View.GONE);
            }
			if (bt_clear != null) {
				bt_clear.setClickable(true);
				bt_clear.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mDeviceInfos.clear();
						((FileRecordActivity) getActivity()).clearAll();
					}
				});
			}
        }
    }

    public void refreshListView(){
    	LogUtil.i("设备 refreshListView");
        if(mAdapter!=null){
            mAdapter.notifyDataSetChanged();
        }
        initView();
    }

}
