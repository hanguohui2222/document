package com.gionee.hotspottransmission.history.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.history.bean.DeviceInfo;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by rongdd on 16-4-28.
 */
public class DeviceAdapter extends BaseAdapter {

    private Context mContext;
    private List<DeviceInfo> mDeviceInfos;

    public DeviceAdapter(Context context,List<DeviceInfo> infos){
        mContext = context.getApplicationContext();
        this.mDeviceInfos = infos;
    }

    @Override
    public int getCount() {
        return mDeviceInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        DeviceInfo deviceInfo = mDeviceInfos.get(position);
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_devices,null);
            viewHolder.deviceImg = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
            viewHolder.deviceLastTime = (TextView) convertView.findViewById(R.id.device_details);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        viewHolder.deviceName.setText(deviceInfo.deviceName);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        viewHolder.deviceLastTime.setText("上次连接时间:"+sdf.format(deviceInfo.connectTime));
        return convertView;
    }

    class ViewHolder{
        ImageView deviceImg;
        TextView deviceName;
        TextView deviceLastTime;
    }
}
