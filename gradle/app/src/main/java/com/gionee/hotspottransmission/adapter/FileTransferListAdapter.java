package com.gionee.hotspottransmission.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.utils.FileTransferUtil;
import com.gionee.hotspottransmission.utils.FileUtil;
import com.gionee.hotspottransmission.utils.ImageLoader;
import com.gionee.hotspottransmission.utils.LogUtil;
import com.gionee.hotspottransmission.manager.ReceivedImageSourceManager;
import com.gionee.hotspottransmission.view.ImageShowActivity;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import java.util.ArrayList;
import java.util.List;
import amigoui.widget.AmigoButton;
import amigoui.widget.AmigoProgressBar;

/**
 * @author luorw
 */
public class FileTransferListAdapter extends BaseAdapter {
    private static final String TAG = "FileTransferListAdapter";
    private Context mContext;
    private ArrayList<FileInfo> mFileList;
    private boolean mIsSender;
    private OnClickListener mListener;

    public FileTransferListAdapter(Context context, ArrayList<FileInfo> datas, boolean isSender, OnClickListener listener) {
        mContext = context.getApplicationContext();
        mFileList = datas;
        mIsSender = isSender;
        mListener = listener;
    }

    public void setFileDatas(ArrayList<FileInfo> datas){
        mFileList = datas;
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
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            LayoutInflater factory = LayoutInflater.from(mContext);
            convertView = factory.inflate(R.layout.item_file_transfer_list, parent, false);
            viewHolder.fileIcon = (ImageView) convertView.findViewById(R.id.file_icon);
            viewHolder.fileName = (TextView) convertView.findViewById(R.id.file_name);
            viewHolder.fileSize = (TextView) convertView.findViewById(R.id.file_size);
            viewHolder.fileSuccess = (TextView) convertView.findViewById(R.id.file_transfer_cancel_or_fail);
            viewHolder.fileCancel = (Button) convertView.findViewById(R.id.file_transfer_cancel);
            viewHolder.openFile = (AmigoButton) convertView.findViewById(R.id.open_file);
            viewHolder.fileTransferProgressBar = (AmigoProgressBar) convertView.findViewById(R.id.file_transfer_progressbar);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            convertView.setTag(viewHolder);
        }

        final FileInfo fileData = mFileList.get(position);
        if (fileData != null) {
            if (viewHolder.fileIcon != null) {
                switch (fileData.getFileType()) {
                    case Constants.TYPE_IMAGE:
                        viewHolder.openFile.setText(R.string.open);
                        break;
                    case Constants.TYPE_MUSIC:
                        viewHolder.openFile.setText(R.string.play);
                        break;
                    case Constants.TYPE_VIDEO:
                        viewHolder.openFile.setText(R.string.play);
                        break;
                    case Constants.TYPE_FILE:
                        viewHolder.openFile.setText(R.string.open);
                        break;
                    case Constants.TYPE_APPS:
                        viewHolder.openFile.setText(R.string.install);
                    default:
                        break;
                }
            }
            if(fileData.getFileIcon() != null){
                ImageLoader.getInstance().loadImage(fileData,viewHolder.fileIcon);
            }
            viewHolder.fileCancel.setTag(position);
            viewHolder.fileCancel.setOnClickListener(mListener);
            if(!mIsSender){
                viewHolder.openFile.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(fileData.getFileType() == Constants.TYPE_IMAGE){
                            List receivedImages = ReceivedImageSourceManager.getInstance(mContext).getReceivedImagePathes();
                            String imageUri = ImageDownloader.Scheme.FILE.wrap(fileData.getFilePath());
                            int currentItem = receivedImages.indexOf(imageUri);
                            LogUtil.i("点击查看图片, path:"+fileData.getFilePath()+"    currentItem:"+currentItem);
                            ReceivedImageSourceManager.getInstance(mContext).setCurrentItem(currentItem);
                            Intent showImage = new Intent(mContext, ImageShowActivity.class);
                            showImage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(showImage);
                        }else{
                            FileTransferUtil.openFileFromUri(mContext, fileData);
                        }
                    }
                });
            }
            viewHolder.fileName.setVisibility(View.VISIBLE);
            viewHolder.fileIcon.setVisibility(View.VISIBLE);
            viewHolder.fileSize.setVisibility(View.VISIBLE);
            viewHolder.fileTransferProgressBar.setVisibility(View.GONE);
            viewHolder.fileSuccess.setVisibility(View.GONE);
            viewHolder.openFile.setVisibility(View.GONE);
            viewHolder.fileCancel.setVisibility(View.GONE);
            if (viewHolder.fileName != null) {
                viewHolder.fileName.setText(fileData.getFileName());
                viewHolder.fileSize.setText(FileUtil.convertStorage(fileData.getFileSize()));
                long maxProgress = fileData.getFileSize();
                long progress = fileData.getTransferingSize();
                if (Integer.MAX_VALUE < maxProgress) {
                    LogUtil.i("Integer.MAX_VALUE < maxProgress---");
                    maxProgress = maxProgress / 10;
                    progress = progress / 10;
                }
                viewHolder.fileTransferProgressBar.setMax((int) maxProgress);
                viewHolder.fileCancel.setVisibility(View.VISIBLE);
                switch (fileData.getState()) {
                    case Constants.FILE_TRANSFER_START:
                        break;
                    case Constants.FILE_TRANSFERING:
                        viewHolder.fileCancel.setVisibility(View.VISIBLE);
                        viewHolder.fileTransferProgressBar.setProgress((int) progress);
                        break;
                    case Constants.FILE_TRANSFER_SUCCESS:
                        viewHolder.fileCancel.setVisibility(View.GONE);
                        if(mIsSender){
                            LogUtil.i("FILE_TRANSFER_SUCCESS = " + fileData.getFileName() + ",position = "+position);
                            viewHolder.fileSuccess.setVisibility(View.VISIBLE);
                            viewHolder.fileSuccess.setBackgroundResource(R.drawable.send_success);
                            viewHolder.fileSuccess.setText("");
                        }else{
                            viewHolder.openFile.setVisibility(View.VISIBLE);
                        }
                        break;
                    case Constants.FILE_TRANSFER_FAILURE:
                        LogUtil.i("FILE_TRANSFER_FAILURE = " + fileData.getFileName() + ",position = "+position);
                        viewHolder.fileCancel.setVisibility(View.GONE);
                        viewHolder.fileSuccess.setVisibility(View.VISIBLE);
                        viewHolder.fileSuccess.setText(R.string.file_transfer_fail);
                        viewHolder.fileSuccess.setBackground(null);
                        viewHolder.fileSuccess.setTextColor(mContext.getResources().getColor(R.color.file_send_failed_color));
                        break;
                    case Constants.FILE_TRANSFER_CANCEL:
                        viewHolder.fileCancel.setVisibility(View.GONE);
                        viewHolder.fileSuccess.setVisibility(View.VISIBLE);
                        viewHolder.fileSuccess.setText(R.string.file_aready_cancel);
                        viewHolder.fileSuccess.setBackground(null);
                        viewHolder.fileSuccess.setTextColor(mContext.getResources().getColor(R.color.file_send_cancel_color));
                        break;
                    default:
                        break;
                }
            }
        }
        return convertView;
    }

    public static class ViewHolder {
        ImageView fileIcon;
        TextView fileName;
        TextView fileSize;
        Button fileCancel;
        AmigoButton openFile;
        TextView fileSuccess;
        public AmigoProgressBar fileTransferProgressBar;
    }
}
