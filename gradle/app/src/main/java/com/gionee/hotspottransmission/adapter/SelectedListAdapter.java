package com.gionee.hotspottransmission.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.utils.CacheUtil;
import com.gionee.hotspottransmission.utils.FileUtil;
import java.util.List;

/**
 * Created by luorw on 4/18/16.
 */
public class SelectedListAdapter extends BaseAdapter {
    private List<FileInfo> mFileList;
    private Context mContext;
    private FileInfo mFileInfo;
    private View.OnClickListener mListener;
    private CacheUtil mCacheUtil;

    public SelectedListAdapter(Context context, View.OnClickListener listener, Handler imageHandler) {
        this.mFileList = (List<FileInfo>) FileSendData.getInstance().getFileSendList().clone();
        this.mContext = context.getApplicationContext();
        this.mListener = listener;
        mCacheUtil = new CacheUtil(imageHandler,context);
    }

    public List<FileInfo> getmFileList() {
        return mFileList;
    }

    public void setmFileList(List<FileInfo> mFileList) {
        this.mFileList = mFileList;
    }

    @Override
    public int getItemViewType(int position) {
        return mFileList.get(position).getFileType();
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mFileInfo = mFileList.get(position);
        int currentType = getItemViewType(position);
        ViewHolder mediaHolder = null;
        if (convertView == null) {
            mediaHolder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_seleced_files, null);
            mediaHolder.mIvIcon = (ImageView) convertView.findViewById(R.id.file_icon);
            mediaHolder.mTvName = (TextView) convertView.findViewById(R.id.file_name);
            mediaHolder.mTvSize = (TextView) convertView.findViewById(R.id.file_size);
            mediaHolder.mIvRemove = (ImageView) convertView.findViewById(R.id.iv_file_remove);
            convertView.setTag(mediaHolder);
        }
        mediaHolder = (ViewHolder) convertView.getTag();
        mediaHolder.mTvName.setText(mFileInfo.getFileName());
        mediaHolder.mTvSize.setText(FileUtil.convertStorage(mFileInfo.getFileSize()));
        if (currentType == Constants.TYPE_VIDEO) {
            mediaHolder.mIvIcon.setImageBitmap(mCacheUtil.getVideoThumbnail(mFileInfo.getFilePath(),position));
        }else if(currentType == Constants.TYPE_IMAGE){
            mediaHolder.mIvIcon.setTag(position);
            mediaHolder.mIvIcon.setImageBitmap(mCacheUtil.getImageBitmap(mFileInfo.getFilePath(),position));
        }else if(currentType == Constants.TYPE_APPS){
            mediaHolder.mIvIcon.setImageBitmap(mCacheUtil.getAPKThumbBitmap(mContext,mFileInfo.getFilePath(),position));
        }else if(currentType == Constants.TYPE_FILE){
            mediaHolder.mIvIcon.setImageResource(R.drawable.file_icon_folder);
        }else if(currentType == Constants.TYPE_MUSIC){
            mediaHolder.mIvIcon.setImageResource(R.drawable.audio_icon);
        }

        mediaHolder.mIvRemove.setTag(mFileInfo);
        mediaHolder.mIvRemove.setOnClickListener(mListener);
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        ImageView mIvIcon;
        TextView mTvName;
        TextView mTvSize;
        ImageView mIvRemove;
    }
}
