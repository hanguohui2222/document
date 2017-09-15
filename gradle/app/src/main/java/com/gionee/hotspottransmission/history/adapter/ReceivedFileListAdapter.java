package com.gionee.hotspottransmission.history.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.utils.CacheUtil;
import com.gionee.hotspottransmission.utils.FileTransferUtil;
import com.gionee.hotspottransmission.utils.FileUtil;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhuboqin on 6/05/16.
 */
public class ReceivedFileListAdapter extends BaseExpandableListAdapter {

    private Map<String,List<FileInfo>> mFileReceiveList;
    private Context mContext;
    private int mType;

    private CacheUtil mCacheUtils;
    private ExpandableListView mListView;

    private Handler mHandler = new Handler() {
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case Constants.SET_IMAGE_BITMAP:
                    LogUtil.i("客户端 mHandler: msg.arg1  : " + msg.arg1);
                    LogUtil.i("客户端 mHandler: msg.obj  : " + ((Bitmap) msg.obj).toString());

                    ImageView imageView = (ImageView)mListView.findViewWithTag(msg.arg1);
                    LogUtil.i("客户端 mHandler: imageView  : " + imageView);
                    LogUtil.i("-------------mHandler-------------------");
                    if (imageView != null){
                        imageView.setImageBitmap((Bitmap)msg.obj);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    public ReceivedFileListAdapter(Context context,Map<String,List<FileInfo>> fileReceiveList,int type, ExpandableListView listView) {
        this.mFileReceiveList = fileReceiveList;
        this.mContext = context.getApplicationContext();
        this.mType = type;
        this.mListView = listView;
        mCacheUtils = new CacheUtil(mHandler,context);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<FileInfo> fileInfoList = getFileInfos(groupPosition);
        return fileInfoList.size();
    }

    @Override
    public int getGroupCount() {
        return mFileReceiveList.size();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return getFileInfos(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return getKey(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        FileInfo fileInfo = getFileInfos(groupPosition).get(childPosition);
        int fileType = fileInfo.getFileType();
        final FileViewHolder viewHolder;
        if(convertView == null){
            convertView = View.inflate(mContext,R.layout.item_record,null);
            viewHolder = new FileViewHolder();
            viewHolder.fileImg = (ImageView) convertView.findViewById(R.id.record_file_icon);
            viewHolder.fileName = (TextView) convertView.findViewById(R.id.record_file_name);
            viewHolder.fileSize = (TextView) convertView.findViewById(R.id.record_file_size);
            viewHolder.fileClick = (TextView) convertView.findViewById(R.id.record_file_click);
            viewHolder.line = convertView.findViewById(R.id.line);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (FileViewHolder)convertView.getTag();
        }
        viewHolder.fileImg.setTag(fileType + "" + groupPosition + "" + childPosition + "");

        viewHolder.fileImg.setImageResource(R.drawable.default_image);

        viewHolder.fileClick.setTag(fileInfo);
        if(viewHolder.fileImg.getTag() != null && viewHolder.fileImg.getTag().equals(fileType + "" + groupPosition + "" + childPosition + "")){
            viewHolder.fileClick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FileTransferUtil.openFileFromUri(mContext, (FileInfo) viewHolder.fileClick.getTag());
                }
            });
        }
        viewHolder.fileName.setText(fileInfo.getFileName());
        switch (fileType){
            case Constants.TYPE_APPS:
                viewHolder.fileClick.setText("安装");
                String path = fileInfo.getFilePath();

                viewHolder.fileImg.setTag(Integer.valueOf(fileType + "" + groupPosition + "" + childPosition + ""));
                Bitmap bitmap = mCacheUtils.getAPKThumbBitmap(mContext,path,Integer.valueOf(fileType + "" + groupPosition + "" + childPosition + ""));
                if (bitmap != null){
                    viewHolder.fileImg.setImageBitmap(bitmap);
                }

                break;
            case Constants.TYPE_FILE:
                //viewHolder.fileImg.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.file_icon));
                viewHolder.fileImg.setImageResource(R.drawable.file_icon);
                viewHolder.fileClick.setText("查看");
                break;
            case Constants.TYPE_IMAGE:
//                if(null != fileInfo.getFilePath()){
//                    Log.e("zhubq18485", "客户端 ReceivedFileListAdapter: TYPE_IMAGE  保存文件路径为: " + fileInfo.getFilePath());
//                    Log.e("zhubq18485", "客户端 ReceivedFileListAdapter: TYPE_IMAGE  tag为: " + fileType+" "+groupPosition+" "+childPosition+" ");
//                    Log.e("zhubq18485", "客户端 ReceivedFileListAdapter: TYPE_IMAGE  tag-integet为: " + Integer.valueOf(fileType+""+groupPosition+""+childPosition+""));
//                    viewHolder.fileImg.setTag(Integer.valueOf(fileType+""+groupPosition+""+childPosition+""));
//                    Bitmap fileIcon = mCacheUtils.getImageBitmap(fileInfo.getFilePath(), Integer.valueOf(fileType+""+groupPosition+""+childPosition+""));
//                    if(fileIcon != null){
//                        Log.e("zhubq18485", "客户端 ReceivedFileListAdapter: TYPE_IMAGE  : " + fileIcon.toString());
//                        Log.e("zhubq18485", "-------------------------------------------");
//                    }
//                    viewHolder.fileImg.setImageBitmap(fileIcon);
//                }
                if(null != fileInfo.getFilePath()){
                    mCacheUtils.displayImageView(fileInfo.getFilePath(),viewHolder.fileImg);
                }
                viewHolder.fileClick.setText("查看");
                break;
            case Constants.TYPE_MUSIC:
                //viewHolder.fileImg.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.audio_icon));
                viewHolder.fileImg.setImageResource(R.drawable.audio_icon);
                viewHolder.fileClick.setText("播放");
                break;
            case Constants.TYPE_VIDEO:
                if (null != fileInfo.getFilePath()) {
                    LogUtil.i("客户端 ReceivedFileListAdapter: TYPE_VIDEO  保存文件路径为: " + fileInfo.getFilePath());
                    viewHolder.fileImg.setTag(Integer.valueOf(fileType + "" + groupPosition + "" + childPosition + ""));
                    Bitmap fileIcon = mCacheUtils.getVideoThumbnail(fileInfo.getFilePath(), Integer.valueOf(fileType+""+groupPosition+""+childPosition+""));
                    viewHolder.fileImg.setImageBitmap(fileIcon);
                }
                viewHolder.fileClick.setText("播放");
                break;
        }
        viewHolder.fileSize.setText(FileUtil.convertStorage(fileInfo.getFileSize()));
        if(childPosition == getFileInfos(groupPosition).size() - 1){
            viewHolder.line.setVisibility(View.VISIBLE);
        }else{
            viewHolder.line.setVisibility(View.GONE);
        }
        return convertView;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String key = getKey(groupPosition);
        int listSize = getFileInfos(groupPosition).size();
        TitleViewHolder titleViewHolder = null;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_record_title, parent, false);
            titleViewHolder = new TitleViewHolder();
            titleViewHolder.recordTime = (TextView) convertView.findViewById(R.id.record_time);
            titleViewHolder.recordNumber = (TextView) convertView.findViewById(R.id.record_total_size);
            convertView.setTag(titleViewHolder);
        }else{
            titleViewHolder = (TitleViewHolder)convertView.getTag();
        }
        titleViewHolder.recordNumber.setText(" ("+listSize + ")");
        titleViewHolder.recordTime.setText(key);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    public List<FileInfo> getFileInfos(int groupPosition) {
        String key = getKey(groupPosition);
        return mFileReceiveList.get(key);
    }

    public String getKey(int groupPosition) {
        Set<String> keySet = mFileReceiveList.keySet();
        Iterator<String> iterator = keySet.iterator();
        List<String> keyList = new ArrayList<>();
        while (iterator.hasNext()){
            String key = iterator.next();
            keyList.add(key);
        }
        return keyList.get(groupPosition);
    }

    private Bitmap getApkBitmap(String path) {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if(pi != null){
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
        TextView recordTime;
        TextView recordNumber;
    }

    class FileViewHolder implements Serializable{
        ImageView fileImg;
        TextView fileName;
        TextView fileSize;
        TextView fileClick;
        View line;
    }
}