package com.gionee.hotspottransmission.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.utils.CacheUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.gionee.hotspottransmission.R;
/**
 * Created by luorw on 4/18/16.
 */
public class ImageDirAdapter extends BaseAdapter{
    private List<String> mDirNameList;
    private Context mContext;
    private Map<String, List<FileInfo>> mDataInfoMap;

    public ImageDirAdapter(Context context, Handler handler){
        this.mContext = context.getApplicationContext();
    }

    public void notifyDataChanged(){
//        mDataInfoMap = QueryImageDataManager.getmDataInfoMap();
        initNameList();
        notifyDataSetChanged();
    }

    public void setDataInfoMap (Map<String, List<FileInfo>> dataInfoMap) {
    	mDataInfoMap = dataInfoMap;
    }
    
    protected void initNameList() {
        if (mDataInfoMap != null) {
            mDirNameList = new ArrayList<String>(this.mDataInfoMap.keySet());
        } else {
            mDirNameList = new ArrayList<String>();
        }
    }

    @Override
    public int getCount() {
        return mDirNameList==null?0:mDirNameList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDirNameList==null?null:mDirNameList.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.item_image_dir, null);
                holder.mIvIcon = (ImageView)convertView.findViewById(R.id.dir_icon);
                holder.mTvDirName = (TextView) convertView.findViewById(R.id.dir_name);
                holder.mTvDirFileCount = (TextView)convertView.findViewById(R.id.dir_file_count);
                convertView.setTag(holder);
            }
            holder = (ViewHolder)convertView.getTag();
            String dirName = mDirNameList.get(position);
            String path = mDataInfoMap.get(dirName).get(0).getFilePath();
            CacheUtil.displayImageView(path,holder.mIvIcon);
            holder.mTvDirName.setText(dirName);
            holder.mTvDirFileCount.setText(Integer.toString(mDataInfoMap.get(dirName).size()));
        return convertView;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder{
        ImageView mIvIcon;
        TextView mTvDirName;
        TextView mTvDirFileCount;
    }
}
