package com.gionee.hotspottransmission.history.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.history.biz.ReceivedFileBiz;

import java.util.List;

public class ReceiveFileGridAdapter extends BaseAdapter {

    int[] imgs = {R.drawable.files_icon,R.drawable.history_app,R.drawable.history_pic,
            R.drawable.history_mic,R.drawable.history_video};
    String[] titles;
    private Context mContext;
    private ReceivedFileBiz mFileBiz;
    private List<Integer> mCountList;

    public ReceiveFileGridAdapter(Context context,List<Integer> countList){
        mContext = context.getApplicationContext();
        titles = mContext.getResources().getStringArray(R.array.select_files_tab);
        mCountList = countList;
        mFileBiz = new ReceivedFileBiz(mContext);
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Object getItem(int position) {
        return titles[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    private int count = 0;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        count = 0;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = View.inflate(mContext,R.layout.item_receive_file,null);
            viewHolder.fileImg = (ImageView) convertView.findViewById(R.id.receive_dir_img);
            viewHolder.fileName = (TextView) convertView.findViewById(R.id.receive_dir_name);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.fileImg.setImageResource(imgs[position]);
        viewHolder.fileName.setText(titles[position]+"("+mCountList.get(position)+")");
        return convertView;
    }

    class ViewHolder{
        ImageView fileImg;
        TextView fileName;
    }
}
