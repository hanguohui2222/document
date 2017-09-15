package com.gionee.hotspottransmission.history.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.utils.FileUtil;

import java.util.List;

public class RecodeAppReceiveAdapter extends BaseAdapter {
    private Context mContext;
    private List<ApplicationInfo> mListAppInfo;
    public RecodeAppReceiveAdapter(Context context,List<ApplicationInfo> listAppInfo){
        mContext = context.getApplicationContext();
        mListAppInfo = listAppInfo;
    }

    @Override
    public int getCount() {
        return mListAppInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mListAppInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(null == holder){
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.item_record , null);
            holder.appIcon = (ImageView) convertView.findViewById(R.id.record_file_icon);
            holder.appName = (TextView) convertView.findViewById(R.id.record_file_name);
            holder.appSize = (TextView) convertView.findViewById(R.id.record_file_size);
            holder.fileClick = (TextView) convertView.findViewById(R.id.record_file_click);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ApplicationInfo appInfo = mListAppInfo.get(position);
        if(null != appInfo){
            holder.appIcon.setImageDrawable(appInfo.loadIcon(mContext.getPackageManager()));
            holder.appName.setText(appInfo.loadLabel(mContext.getPackageManager()).toString());
            holder.fileClick.setText("安装");
            try {
                holder.appSize.setText(FileUtil.formatDateToStr(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), position).firstInstallTime, "yyyy/MM/dd"));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }

    private static class ViewHolder{
        ImageView appIcon;
        TextView appName;
        TextView appSize;
        TextView fileClick;
    }
}
