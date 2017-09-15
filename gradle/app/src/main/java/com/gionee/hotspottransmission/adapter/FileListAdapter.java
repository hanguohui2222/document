package com.gionee.hotspottransmission.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
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
public class FileListAdapter extends BaseAdapter{
    private List<FileInfo> mFileList;//文件列表
    private Context mContext;
    private FileInfo mFileInfo;
    private CacheUtil mCacheUtil;
    private List<FileInfo> mFileSendList;//已选文件列表

    public FileListAdapter(Context context, List<FileInfo> list,Handler handler){
        this.mFileList = list;
        this.mContext = context.getApplicationContext();
        mCacheUtil = new CacheUtil(handler,context);
        mFileSendList = FileSendData.getInstance().getFileSendList();
    }

    public void setmFileList(List<FileInfo> mFileList) {
        this.mFileList = mFileList;
    }

    public  int getLoaderId(int position){
        return mFileList.get(position).getLoaderId();
    }

    @Override
    public int getItemViewType(int position) {
        return mFileList.get(position).getFileType();
    }
    @Override
    public int getCount() {
        return mFileList == null?0:mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList==null?null:mFileList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mFileInfo= mFileList.get(position);
        Bitmap bitmap = null;
        int currentType = getLoaderId(position);
        switch (currentType){
            case Constants.TYPE_IMAGE:
                ImageViewHolder holder = null;
                if(convertView == null) {
                    holder = new ImageViewHolder();
                    convertView = View.inflate(mContext, R.layout.item_image_list, null);
                    holder.mIvIcon = (ImageView)convertView.findViewById(R.id.image_show_iv);
                    holder.mIvIconBg = (ImageView)convertView.findViewById(R.id.image_show_checkbox_bg);
                    holder.mIsSelected = (CheckBox) convertView.findViewById(R.id.image_show_checkbox);
                    convertView.setTag(holder);
                }
                holder = (ImageViewHolder)convertView.getTag();
                if(mFileSendList.contains(mFileInfo)){
                    holder.mIvIconBg.setVisibility(View.VISIBLE);
                    holder.mIvIconBg.bringToFront();
                    holder.mIsSelected.setChecked(true);
                }else {
                    holder.mIvIconBg.setVisibility(View.GONE);
                    holder.mIsSelected.setChecked(false);
                }
                CacheUtil.displayImageView(mFileInfo.getFilePath(),holder.mIvIcon);
                break;
            case Constants.TYPE_MUSIC:
            case Constants.TYPE_VIDEO:
                MediaViewHolder mediaHolder = null;
                if(convertView == null) {
                    mediaHolder = new MediaViewHolder();
                    convertView = View.inflate(mContext, R.layout.item_music_and_video, null);
                    mediaHolder.mIvIcon = (ImageView)convertView.findViewById(R.id.file_icon);
                    mediaHolder.mTvName = (TextView) convertView.findViewById(R.id.file_name);
                    mediaHolder.mTvSize = (TextView) convertView.findViewById(R.id.file_size);
                    mediaHolder.mIsSelected = (CheckBox) convertView.findViewById(R.id.file_checkbox);
                    convertView.setTag(mediaHolder);
                }
                mediaHolder = (MediaViewHolder)convertView.getTag();
                mediaHolder.mTvName.setText(mFileInfo.getFileName());
                if(mFileSendList.contains(mFileInfo)){
                    mediaHolder.mIsSelected.setVisibility(View.VISIBLE);
                    mediaHolder.mIsSelected.setChecked(true);
                }else {
                    mediaHolder.mIsSelected.setVisibility(View.GONE);
                    mediaHolder.mIsSelected.setChecked(false);
                }
                mediaHolder.mTvSize.setText(FileUtil.convertStorage(mFileInfo.getFileSize()));
                if(currentType == Constants.TYPE_VIDEO){
                    mediaHolder.mIvIcon.setTag(position);
                    bitmap = mCacheUtil.getVideoThumbnail(mFileInfo.getFilePath(),position);
                    mediaHolder.mIvIcon.setImageBitmap(bitmap);
                }else{
                    mediaHolder.mIvIcon.setImageResource(R.drawable.audio_icon);
                }
                break;
            case Constants.TYPE_APPS:
                AppViewHolder appHolder = null;
                if (null == convertView) {
                    LayoutInflater inflater = LayoutInflater.from(mContext);
                    convertView = inflater.inflate(R.layout.item_appinfo, null);
                    appHolder = new AppViewHolder();
                    appHolder.appIcon = (ImageView) convertView.findViewById(R.id.item_iv_app_info_icon);
                    appHolder.appName = (TextView) convertView.findViewById(R.id.item_tv_app_info_appname);
                    appHolder.checkbox = (CheckBox) convertView.findViewById(R.id.item_app_checkbox);
                    appHolder.appIconBg = (ImageView) convertView.findViewById(R.id.item_iv_app_checkbox_bg);
                    convertView.setTag(appHolder);
                } else {
                    appHolder = (AppViewHolder) convertView.getTag();
                }
                FileInfo appInfo = mFileList.get(position);
                if (null != appInfo) {
                    appHolder.appIcon.setTag(position);
                    bitmap = mCacheUtil.getAPKThumbBitmap(mContext,appInfo.getFilePath(),position);
                    if (bitmap != null){
                        appHolder.appIcon.setImageBitmap(bitmap);
                    }
                    appHolder.appName.setText(appInfo.getFileName());
                    if(mFileSendList.contains(appInfo)){
                        appHolder.checkbox.setVisibility(View.VISIBLE);
                        appHolder.checkbox.setChecked(true);
                        appHolder.appIconBg.setVisibility(View.VISIBLE);
                    }else {
                        appHolder.checkbox.setVisibility(View.GONE);
                        appHolder.checkbox.setChecked(false);
                        appHolder.appIconBg.setVisibility(View.GONE);
                    }
                }
                break;
            case Constants.TYPE_DOCUMENT:
            case Constants.TYPE_COMPRESS:
            case Constants.TYPE_EBOOK:
            case Constants.TYPE_APK:
                FileHolder fileHolder = null;
                if(convertView == null){
                    convertView = View.inflate(mContext,R.layout.item_file,null);
                    fileHolder = new FileHolder();
                    fileHolder.fileName = (TextView)convertView.findViewById(R.id.text1);
                    fileHolder.fileSize = (TextView)convertView.findViewById(R.id.text2);
                    fileHolder.fileImg = (ImageView)convertView.findViewById(R.id.file_img);
                    fileHolder.checkBox = (CheckBox)convertView.findViewById(R.id.file_checkbox);
                    convertView.setTag(fileHolder);
                }else {
                    fileHolder = (FileHolder)convertView.getTag();
                }
                FileInfo fileInfo = mFileList.get(position);
                if(fileInfo != null){
                    fileHolder.fileName.setText(fileInfo.getFileName());
                    fileHolder.fileSize.setText(FileUtil.convertStorage(fileInfo.getFileSize()));
                    if(mFileSendList.contains(fileInfo)){
                        fileHolder.checkBox.setVisibility(View.VISIBLE);
                        fileHolder.checkBox.setChecked(true);
                    }else {
                        fileHolder.checkBox.setVisibility(View.GONE);
                        fileHolder.checkBox.setChecked(false);
                    }
                }
                if(currentType == Constants.TYPE_APK){
                    fileHolder.fileImg.setTag(position);
                    bitmap = mCacheUtil.getAPKThumbBitmap(mContext, mFileInfo.getFilePath(),position);
                    fileHolder.fileImg.setImageBitmap(bitmap);
                }else{
                    fileHolder.fileImg.setImageResource(R.drawable.file_icon);
                }
                break;
            default:
                break;
        }
        return convertView;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    class ImageViewHolder{
        ImageView mIvIcon;
        CheckBox mIsSelected;
        ImageView mIvIconBg;
    }

    class MediaViewHolder{
        ImageView mIvIcon;
        TextView mTvName;
        TextView mTvSize;
        CheckBox mIsSelected;
    }

    class AppViewHolder {
        ImageView appIcon;
        ImageView appIconBg;
        TextView appName;
        public CheckBox checkbox;
    }
    class FileHolder{
        TextView fileName;
        TextView fileSize;
        ImageView fileImg;
        CheckBox checkBox;
    }
}
