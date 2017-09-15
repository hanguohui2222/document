package com.gionee.hotspottransmission.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.gionee.hotspottransmission.R;

/**
 * Created by luorw on 16-4-28.
 */
public class FilesGridAdapter extends BaseAdapter {

    private int[] imgs = {R.drawable.files_icon,R.drawable.zip_icon,R.drawable.ebook_icon,
            R.drawable.apks_icon};
    private Context mContext;
    private String[] filesType;

    public FilesGridAdapter(Context context){
        mContext = context.getApplicationContext();
        this.filesType = context.getResources().getStringArray(R.array.files_types);
    }

    @Override
    public int getCount() {
        return filesType.length;
    }

    @Override
    public Object getItem(int position) {
        return filesType[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = View.inflate(mContext,R.layout.item_files_type,null);
            viewHolder.fileImg = (ImageView) convertView.findViewById(R.id.file_type_img);
            viewHolder.fileName = (TextView) convertView.findViewById(R.id.file_type_name);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.fileImg.setImageResource(imgs[position]);
        viewHolder.fileName.setText(filesType[position]);
        return convertView;
    }
    class ViewHolder{
        ImageView fileImg;
        TextView fileName;
    }
}
