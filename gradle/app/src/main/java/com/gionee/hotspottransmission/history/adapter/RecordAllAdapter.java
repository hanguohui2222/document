package com.gionee.hotspottransmission.history.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.history.bean.HistoryInfo;
import com.gionee.hotspottransmission.utils.CacheUtil;
import com.gionee.hotspottransmission.utils.FileUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rongdd on 16-4-29.
 */
public class RecordAllAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private List<HistoryInfo> mRecordInfoList;
    private SimpleDateFormat mSimpleDateFormat;
    private DateFormat mDateFormat;

    private CacheUtil mCacheUtils;


    public RecordAllAdapter(Context context,List<HistoryInfo> historyList, Handler handler) {
        mContext = context.getApplicationContext();
        mRecordInfoList = historyList;
        inflater = LayoutInflater.from(mContext);
        mCacheUtils = new CacheUtil(handler,mContext);
    }

    @Override
    public int getCount() {
        return mRecordInfoList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mRecordInfoList.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HistoryInfo mHistoryInfo = mRecordInfoList.get(position);
        HistoryInfo mUpHistoryInfo = null;
        if( position -1 >= 0 )
            mUpHistoryInfo = mRecordInfoList.get(position - 1);

        TitleViewHolder titleViewHolder = null;
        List<View> viewList;
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.item_history_record,null);
            titleViewHolder = new TitleViewHolder(convertView);
            convertView.setTag(titleViewHolder);
        }else{
            titleViewHolder = (TitleViewHolder)convertView.getTag();
            titleViewHolder.ll_history_record.removeAllViews();
        }
        mSimpleDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);

        if(null != mUpHistoryInfo && (mSimpleDateFormat.format(mUpHistoryInfo.date)).equals(mSimpleDateFormat.format(mHistoryInfo.date))){
            titleViewHolder.tv_history_title_date.setVisibility(View.GONE);
        }else{
            titleViewHolder.tv_history_title_date.setVisibility(View.VISIBLE);
            titleViewHolder.tv_history_title_date.setText(mSimpleDateFormat.format(mHistoryInfo.date));
        }
        if(mHistoryInfo.isSender){
            titleViewHolder.tv_history_title_disc.setText("本机发送给" + mHistoryInfo.deviceName + ", " + mHistoryInfo.fileCount + "个文件, 共"
                    + FileUtil.convertStorage(mHistoryInfo.fileSize));
            viewList = addFileList(mHistoryInfo,true,position);
        }else{
            titleViewHolder.tv_history_title_disc.setText(mHistoryInfo.deviceName+"发送给本机"+", "+mHistoryInfo.fileCount+"个文件, 共"
                    + FileUtil.convertStorage(mHistoryInfo.fileSize));
            viewList = addFileList(mHistoryInfo,false,position);
        }

        for(View view : viewList){
            titleViewHolder.ll_history_record.addView(view);
        }

        return convertView;
    }

    private List<View> addFileList(HistoryInfo mHistoryInfo,boolean isSender,int position) {
        List<FileInfo> fileInfoList = mHistoryInfo.files;
        mDateFormat = new SimpleDateFormat(Constants.TIME_FORMAT);
        List<View> viewList = new ArrayList<>();
        for(int i=0; i<fileInfoList.size(); i++){
            FileInfo fileInfo = fileInfoList.get(i);
            final View fileView = inflater.inflate(R.layout.item_history_file, null);
            FileHolder fileHolder = new FileHolder(fileView);
            fileView.setTag(fileInfo);
            fileHolder.tv_history_file_name.setText(fileInfo.getFileName());
            fileHolder.tv_history_file_size.setText(FileUtil.convertStorage(fileInfo.getFileSize()));
            //added by luorw for GNSPR #18326 begin
            fileHolder.tv_history_file_time.setText(mDateFormat.format(mHistoryInfo.date));
            //added by luorw for GNSPR #18326 end
            fileHolder.tv_history_file_time.setTag(position+""+i);
            //added by luorw for GNSPR #22731 begin
            switch (fileInfo.getFileType()){
                case Constants.TYPE_APPS:
                    if(fileInfo.getState() == Constants.FILE_TRANSFER_CANCEL || fileInfo.getState() == Constants.FILE_TRANSFER_FAILURE){
                        fileHolder.iv_history_file_icon.setImageResource(R.drawable.apk_disable);
                    }else{
                        fileHolder.iv_history_file_icon.setImageBitmap(getApkBitmap(fileInfo.getFilePath()));
                    }
                    break;
                case Constants.TYPE_FILE:
                    if(fileInfo.getState() == Constants.FILE_TRANSFER_CANCEL || fileInfo.getState() == Constants.FILE_TRANSFER_FAILURE){
                        fileHolder.iv_history_file_icon.setImageResource(R.drawable.file_disable);
                    }else{
                        fileHolder.iv_history_file_icon.setImageResource(R.drawable.file_icon);
                    }
                    break;
                case Constants.TYPE_IMAGE:
                    if(fileInfo.getState() == Constants.FILE_TRANSFER_CANCEL || fileInfo.getState() == Constants.FILE_TRANSFER_FAILURE){
                        fileHolder.iv_history_file_icon.setImageResource(R.drawable.image_disable);
                    }else{
                        if(null != fileInfo.getFilePath()){
                            Bitmap fileIcon = mCacheUtils.getImageBitmap(fileInfo.getFilePath(), Integer.valueOf(position+""+i));
                            fileHolder.iv_history_file_icon.setTag(Integer.valueOf(position+""+i));
                            fileHolder.iv_history_file_icon.setImageBitmap(fileIcon);
                        }
                    }
                    break;
                case Constants.TYPE_MUSIC:
                    if(fileInfo.getState() == Constants.FILE_TRANSFER_CANCEL || fileInfo.getState() == Constants.FILE_TRANSFER_FAILURE){
                        fileHolder.iv_history_file_icon.setImageResource(R.drawable.music_disable);
                    }else{
                        fileHolder.iv_history_file_icon.setImageResource(R.drawable.audio_icon);
                    }
                    break;
                case Constants.TYPE_VIDEO:
                    if(fileInfo.getState() == Constants.FILE_TRANSFER_CANCEL || fileInfo.getState() == Constants.FILE_TRANSFER_FAILURE){
                        fileHolder.iv_history_file_icon.setImageResource(R.drawable.video_disable);
                    }else{
                        if(null != fileInfo.getFilePath()){
                            Bitmap fileIcon = mCacheUtils.getVideoThumbnail(fileInfo.getFilePath(), Integer.valueOf(position+""+i));
                            fileHolder.iv_history_file_icon.setTag(Integer.valueOf(position+""+i));
                            fileHolder.iv_history_file_icon.setImageBitmap(fileIcon);
                        }
                    }
                    break;
                default:
                    break;
            }
            //added by luorw for GNSPR #22731 begin
            fileHolder.tv_history_file_state_cancel.setVisibility(View.GONE);
            fileHolder.tv_history_file_state_fail.setVisibility(View.GONE);
            fileHolder.tv_history_file_state_success.setVisibility(View.GONE);
            switch (fileInfo.getState()){
                case Constants.FILE_TRANSFER_SUCCESS:
                    fileHolder.tv_history_file_state_success.setVisibility(View.VISIBLE);
                    break;
                case Constants.FILE_TRANSFER_FAILURE:
                    fileHolder.tv_history_file_state_fail.setVisibility(View.VISIBLE);
                    break;
                case Constants.FILE_TRANSFER_CANCEL:
                    fileHolder.tv_history_file_state_cancel.setVisibility(View.VISIBLE);
                    break;
                default:
                    fileHolder.tv_history_file_state_fail.setVisibility(View.VISIBLE);
                    break;
            }
            if(i == fileInfoList.size()-1)
                fileHolder.line.setVisibility(View.VISIBLE);
            else
                fileHolder.line.setVisibility(View.GONE);

            viewList.add(fileView);
        }
        return viewList;
    }

    private Bitmap getApkBitmap(String path) {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if(pi!=null){
            ApplicationInfo applicationInfo = pi.applicationInfo;
            applicationInfo.sourceDir = path;
            applicationInfo.publicSourceDir = path;
            BitmapDrawable bitmapDrawable = (BitmapDrawable)applicationInfo.loadIcon(pm);
            Bitmap bitmap = bitmapDrawable.getBitmap();
            return bitmap;
        }
        return null;
    }

    class TitleViewHolder{
        TextView tv_history_title_date;
        TextView tv_history_title_disc;
        LinearLayout ll_history_record;
        TitleViewHolder(View view){
            tv_history_title_date = (TextView)view.findViewById(R.id.tv_history_title_date);
            tv_history_title_disc = (TextView)view.findViewById(R.id.tv_history_title_disc);
        }
    }

    class FileHolder{
        ImageView iv_history_file_icon;
        TextView tv_history_file_name;
        TextView tv_history_file_time;
        TextView tv_history_file_size;
        ImageView tv_history_file_state_success;
        TextView tv_history_file_state_fail;
        TextView tv_history_file_state_cancel;

        View line;
        FileHolder(View view){
            iv_history_file_icon = (ImageView) view.findViewById(R.id.iv_history_file_icon);
            tv_history_file_name = (TextView) view.findViewById(R.id.tv_history_file_name);
            tv_history_file_time = (TextView) view.findViewById(R.id.tv_history_file_time);
            tv_history_file_size = (TextView) view.findViewById(R.id.tv_history_file_size);
            tv_history_file_state_success = (ImageView) view.findViewById(R.id.tv_history_file_state_success);
            tv_history_file_state_fail = (TextView) view.findViewById(R.id.tv_history_file_state_fail);
            tv_history_file_state_cancel = (TextView) view.findViewById(R.id.tv_history_file_state_cancel);
            line = view.findViewById(R.id.line);
        }
    }

}
