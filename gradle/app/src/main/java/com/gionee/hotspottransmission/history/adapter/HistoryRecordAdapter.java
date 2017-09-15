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
import android.widget.TextView;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.history.bean.HistoryFileInfo;
import com.gionee.hotspottransmission.utils.CacheUtil;
import com.gionee.hotspottransmission.utils.FileUtil;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by zhuboqin on 16-6-28.
 */
public class HistoryRecordAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater inflater;
    private List<HistoryFileInfo> mRecordInfoList;
    private SimpleDateFormat mSimpleDateFormat;
    private DateFormat mDateFormat;

    private CacheUtil mCacheUtils;
    private static final int WITH_TITLE = 20;
    private static final int FILE_ONLY = 21;

    public HistoryRecordAdapter(Context context,List<HistoryFileInfo> historyList, Handler handler) {
        mContext = context.getApplicationContext();
        mRecordInfoList = historyList;
        inflater = LayoutInflater.from(mContext);
        mCacheUtils = new CacheUtil(handler,mContext);
        mSimpleDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
        mDateFormat = new SimpleDateFormat(Constants.TIME_FORMAT);
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
    public int getItemViewType(int position) {
        HistoryFileInfo historyFileInfoNow = mRecordInfoList.get(position);
        if(position > 0){
            HistoryFileInfo historyFileInfoLast = mRecordInfoList.get(position - 1);
            if(historyFileInfoLast.id != historyFileInfoNow.id){
                return WITH_TITLE;
            }else{
                return FILE_ONLY;
            }
        }else{
            return WITH_TITLE;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        LogUtil.i("adapter getView type  " + type);
        HistoryFileInfo historyFileInfo = mRecordInfoList.get(position);
        LogUtil.i("adapter getView HistoryFileInfo  " + historyFileInfo.toString());
        HistoryFileInfo historyFileInfoLat = null;
        if(position > 0){
            historyFileInfoLat = mRecordInfoList.get(position - 1);
        }

        FileInfo fileInfo = historyFileInfo.file;

        TitleViewHolder titleHolder = null;
        FileHolder fileHolder = null;

        if(convertView == null){
            if(type == WITH_TITLE){
                convertView = inflater.inflate(R.layout.item_history_record,null);
                titleHolder = new TitleViewHolder(convertView);
                convertView.setTag(titleHolder);
                LogUtil.i("holder  TitleViewHolder------------------------");
            }else if(type == FILE_ONLY){
                convertView = inflater.inflate(R.layout.item_history_file,null);
                fileHolder = new FileHolder(convertView);
                convertView.setTag(fileHolder);
                LogUtil.i("holder  FileHolder------------------------");
            }
        }else{
            fileHolder = (FileHolder)convertView.getTag();
            if(type == WITH_TITLE){
                if(fileHolder instanceof TitleViewHolder){
                    titleHolder = (TitleViewHolder) fileHolder;
                }else{
                    convertView = inflater.inflate(R.layout.item_history_record,null);
                    titleHolder = new TitleViewHolder(convertView);
                    convertView.setTag(titleHolder);
                }
            }else if(type == FILE_ONLY){
                if(fileHolder instanceof TitleViewHolder){
                    convertView = inflater.inflate(R.layout.item_history_file,null);
                    fileHolder = new FileHolder(convertView);
                    convertView.setTag(fileHolder);
                }
            }
        }

        if(type == WITH_TITLE){
            if(position == 0){
                titleHolder.line.setVisibility(View.GONE);
            }
            if(historyFileInfoLat != null && (mSimpleDateFormat.format(historyFileInfoLat.date)).equals(mSimpleDateFormat.format(historyFileInfo.date))){
                titleHolder.tv_history_title_date.setVisibility(View.GONE);
            }else{
                titleHolder.tv_history_title_date.setVisibility(View.VISIBLE);
                titleHolder.tv_history_title_date.setText(mSimpleDateFormat.format(historyFileInfo.date));
                LogUtil.i("date  " + mSimpleDateFormat.format(historyFileInfo.date));
            }

            if(historyFileInfo.isSender){
                titleHolder.tv_history_title_disc.setText("本机发送给" + historyFileInfo.deviceName + ", " + historyFileInfo.fileCount + "个文件, 共"
                        + FileUtil.convertStorage(historyFileInfo.fileSize));

            }else{
                titleHolder.tv_history_title_disc.setText(historyFileInfo.deviceName+"发送给本机"+", "+historyFileInfo.fileCount+"个文件, 共"
                        + FileUtil.convertStorage(historyFileInfo.fileSize));

            }
            setFileView(position, titleHolder, fileInfo, historyFileInfo);
        }

        if(type == FILE_ONLY){
            setFileView(position, fileHolder, fileInfo, historyFileInfo);
        }

        return convertView;
    }

    private void setFileView(int position, com.gionee.hotspottransmission.history.adapter.HistoryRecordAdapter.FileHolder fileHolder, FileInfo fileInfo, HistoryFileInfo historyFileInfo) {
        fileHolder.tv_history_file_name.setText(fileInfo.getFileName());
        fileHolder.tv_history_file_size.setText(FileUtil.convertStorage(fileInfo.getFileSize()));
        //added by luorw for GNSPR #18326 begin
        fileHolder.tv_history_file_time.setText(mDateFormat.format(historyFileInfo.date));
        //added by luorw for GNSPR #18326 end
        fileHolder.tv_history_file_time.setTag(position);
        //added by luorw for GNSPR #22731 begin
        fileHolder.iv_history_file_icon.setImageResource(R.drawable.default_image);
        switch (fileInfo.getFileType()){
            case Constants.TYPE_APPS:
                if(fileInfo.getState() == Constants.FILE_TRANSFER_CANCEL || fileInfo.getState() == Constants.FILE_TRANSFER_FAILURE){
                    fileHolder.iv_history_file_icon.setImageResource(R.drawable.apk_disable);
                }else{
                    fileHolder.iv_history_file_icon.setTag(Integer.valueOf(position));
                    Bitmap bitmap = mCacheUtils.getAPKThumbBitmap(mContext,fileInfo.getFilePath(),Integer.valueOf(position));
                    if (bitmap != null){
                        fileHolder.iv_history_file_icon.setImageBitmap(bitmap);
                    }
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
                        mCacheUtils.displayImageView(fileInfo.getFilePath(),fileHolder.iv_history_file_icon);
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
                        fileHolder.iv_history_file_icon.setTag(Integer.valueOf(position));
                        Bitmap fileIcon = mCacheUtils.getVideoThumbnail(fileInfo.getFilePath(), Integer.valueOf(position));
                        if(fileIcon !=null){
                            fileHolder.iv_history_file_icon.setImageBitmap(fileIcon);
                        }
                    }
                }
                break;
            default:
                break;
        }
        //added by luorw for GNSPR #22731 end
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

    class TitleViewHolder extends FileHolder{
        TextView tv_history_title_date;
        TextView tv_history_title_disc;
        View line;

        TitleViewHolder(View view){
            super(view);
            tv_history_title_date = (TextView)view.findViewById(R.id.tv_history_title_date);
            tv_history_title_disc = (TextView)view.findViewById(R.id.tv_history_title_disc);
            line = view.findViewById(R.id.line);
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

        FileHolder(View view){
            iv_history_file_icon = (ImageView) view.findViewById(R.id.iv_history_file_icon);
            tv_history_file_name = (TextView) view.findViewById(R.id.tv_history_file_name);
            tv_history_file_time = (TextView) view.findViewById(R.id.tv_history_file_time);
            tv_history_file_size = (TextView) view.findViewById(R.id.tv_history_file_size);
            tv_history_file_state_success = (ImageView) view.findViewById(R.id.tv_history_file_state_success);
            tv_history_file_state_fail = (TextView) view.findViewById(R.id.tv_history_file_state_fail);
            tv_history_file_state_cancel = (TextView) view.findViewById(R.id.tv_history_file_state_cancel);
        }
    }

}
