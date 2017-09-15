package com.gionee.hotspottransmission.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.ArrayMap;
import android.util.Log;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.utils.CacheUtil;
import com.gionee.hotspottransmission.utils.FileTransferUtil;
import com.gionee.hotspottransmission.utils.FileUtil;
import com.gionee.hotspottransmission.utils.LogUtil;

public class BaseSendData {
	
	public ArrayList<FileInfo> mAllFileDatas;
	public static BaseSendData mBaseSendData;
	public String mFileType;
	public ArrayList<Uri> mFileUris = null;
	public long mAllFileSize;
	public long mTotalTransferedSize = 0;
    public ArrayList<FileInfo> mAppDatas;
    public Map<String, List<FileInfo>> mImageDatas;
    public ArrayList<FileInfo> mMusicDatas;
    public ArrayList<FileInfo> mVideoDatas;

    public boolean isCancelAllSend;
    public boolean isAllSendComplete;
    public boolean isSending;
    public int mCurrentSendIndex;
    public boolean isConnected;

    public BaseSendData() {
        mAllFileDatas = new ArrayList<FileInfo>();
        mAppDatas = new ArrayList<FileInfo>();
        mImageDatas = new ArrayMap<>();
        mMusicDatas = new ArrayList<FileInfo>();
        mVideoDatas = new ArrayList<FileInfo>();
    }


    public boolean isSending() {
        return isSending;
    }

    public void setSending(boolean sending) {
        isSending = sending;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean isCancelAllSend() {
        return isCancelAllSend;
    }

    public void setCancelAllSend(boolean cancelAllSend) {
        isCancelAllSend = cancelAllSend;
    }

    public boolean isAllSendComplete() {
        return isAllSendComplete;
    }

    public void setAllSendComplete(boolean allSendComplete) {
        isAllSendComplete = allSendComplete;
        if (allSendComplete) {
            setSending(false);
        }
    }

    public int getmCurrentSendIndex() {
        return mCurrentSendIndex;
    }

    public void setmCurrentSendIndex(int mCurrentSendIndex) {
        this.mCurrentSendIndex = mCurrentSendIndex;
    }

    public ArrayList<FileInfo> getFileSendList() {
        return mAllFileDatas;
    }

    public void setFileSendList(ArrayList<FileInfo> list) {
        mAllFileDatas = list;
    }

    public void addFileTransferList(FileInfo info) {
        switch (info.getFileType()) {
            case Constants.TYPE_APPS:
                if (info.getLoaderId() != Constants.TYPE_APK) {
                    if (!mAppDatas.contains(info)) {
                        mAppDatas.add(info);
                    }
                }
                break;
            case Constants.TYPE_MUSIC:
                if (!mMusicDatas.contains(info)) {
                    mMusicDatas.add(info);
                }
                break;
            case Constants.TYPE_VIDEO:
                if (!mVideoDatas.contains(info)) {
                    mVideoDatas.add(info);
                }
                break;
            default:
                break;
        }
        if (!mAllFileDatas.contains(info)) {
            mAllFileDatas.add(info);
        }
    }

    public void addImgTransferList(FileInfo info, String imgDirName) {
        List<FileInfo> list = mImageDatas.get(imgDirName);
        if (list == null) {
            list = new ArrayList<>();
            list.add(info);
            mImageDatas.put(imgDirName, list);
        } else {
            list.add(info);
        }
        if (!mAllFileDatas.contains(info)) {
            Log.i("luorw","111111111111111111");
            mAllFileDatas.add(info);
        }
    }

    public void addImgTransferList(List<FileInfo> list, String imgDirName) {
        List<FileInfo> imgList = mImageDatas.get(imgDirName);
        if (imgList != null && mAllFileDatas.containsAll(imgList)) {
            Log.i("luorw","111111111111111111 , size = "+imgList.size());
            mAllFileDatas.removeAll(imgList);
            imgList.clear();
            imgList.addAll(list);
            mAllFileDatas.addAll(list);
        }
        if (imgList == null) {
            imgList = new ArrayList<>();
            imgList.addAll(list);
            mImageDatas.put(imgDirName,imgList);
            mAllFileDatas.addAll(list);
        }
    }

    public void addFileTransferList(int type, List<FileInfo> list) {
        switch (type) {
            case Constants.TYPE_APPS:
                if (mAllFileDatas.containsAll(mAppDatas)) {
                    mAllFileDatas.removeAll(mAppDatas);
                    mAppDatas.clear();
                    mAppDatas.addAll(list);
                }
                break;
            case Constants.TYPE_MUSIC:
                if (mAllFileDatas.containsAll(mMusicDatas)) {
                    mAllFileDatas.removeAll(mMusicDatas);
                    mMusicDatas.clear();
                    mMusicDatas.addAll(list);
                }
                break;
            case Constants.TYPE_VIDEO:
                if (mAllFileDatas.containsAll(mVideoDatas)) {
                    mAllFileDatas.removeAll(mVideoDatas);
                    mVideoDatas.clear();
                    mVideoDatas.addAll(list);
                }
                break;
            default:
                break;
        }
        mAllFileDatas.addAll(list);
    }

    public void removeImgTransferInfo(FileInfo info, String imgDirName) {
        List<FileInfo> imgList = mImageDatas.get(imgDirName);
        if (imgList.contains(info)) {
            imgList.remove(info);
        }
        mAllFileDatas.remove(info);
    }

    public void removeFileTransferInfo(FileInfo info) {
        switch (info.getFileType()) {
            case Constants.TYPE_APPS:
                if (info.getLoaderId() != Constants.TYPE_APK) {
                    if (mAppDatas.contains(info)) {
                        mAppDatas.remove(info);
                    }
                }
                break;
            case Constants.TYPE_MUSIC:
                if (mMusicDatas.contains(info)) {
                    mMusicDatas.remove(info);
                }
                break;
            case Constants.TYPE_VIDEO:
                if (mVideoDatas.contains(info)) {
                    mVideoDatas.remove(info);
                }
                break;
            default:
                break;
        }
        mAllFileDatas.remove(info);
    }

    public void removeImgTransferInfo(List<FileInfo> list, String imgDirName) {
        mImageDatas.get(imgDirName).clear();
        mAllFileDatas.removeAll(list);
    }

    public void removeFileTransferInfo(int type, List<FileInfo> list) {
        switch (type) {
            case Constants.TYPE_APPS:
                mAppDatas.clear();
                break;
            case Constants.TYPE_MUSIC:
                mMusicDatas.clear();
                break;
            case Constants.TYPE_VIDEO:
                mVideoDatas.clear();
                break;
            default:
                break;
        }
        mAllFileDatas.removeAll(list);
    }

    public void addFileTransferData(Context context, ArrayList<Uri> mFileUris, String[] fileNames, long[] fileSizes, String[] filePaths) {
        if (mAllFileDatas == null) {
            return;
        } else {
            clearAllFiles();
        }

        for (int i = 0; i < fileNames.length && i < fileSizes.length; i++) {
            FileInfo fileTransferData = null;
            if (filePaths != null && filePaths.length > i) {
                fileTransferData = new FileInfo(context, mFileUris.get(i), fileNames[i], fileSizes[i], filePaths[i]);
            } else {
                fileTransferData = new FileInfo(mFileUris.get(i), fileNames[i], fileSizes[i]);
            }
            addFileTransferData(i, fileTransferData);
        }
    }

    //added by luorw for 仅用于下载管理里分享文件 begin
    public void addFileTransferDataForDM(Context context, ArrayList<Uri> mFileUris, String[] fileNames, long[] fileSizes) {
        if (mAllFileDatas == null) {
            return;
        } else {
            clearAllFiles();
        }

        for (int i = 0; i < fileNames.length && i < fileSizes.length; i++) {
            FileInfo fileTransferData = new FileInfo(mFileUris.get(i), fileNames[i], fileSizes[i]);
            String filePath = "/storage/emulated/0/GN_Gou/" + fileNames[i];
            fileTransferData.setFilePath(filePath);
            String fileMimeType = FileTransferUtil.getFileMimeType(filePath);
            fileTransferData.setFileType(FileTransferUtil.getFileTypeFromMimeType(fileMimeType));
            LogUtil.i("下载管理中的文件路径 = " + fileTransferData.getFilePath());
            addFileTransferData(i, fileTransferData);
        }
    }
    //added by luorw for 仅用于下载管理里分享文件 end

    public ResponseInfo getResponseList() {
        ResponseInfo responseList = new ResponseInfo();
        responseList.setFilesList(mAllFileDatas);
        return responseList;
    }
    public MultiCommandInfo getMultiInfo() {
        MultiCommandInfo multiCommandInfo = new MultiCommandInfo();
        multiCommandInfo.setFilesList(mAllFileDatas);
        return multiCommandInfo;
    }

    public void addFileTransferData(int index, FileInfo fileTransferData) {
        if (mAllFileDatas != null) {
            mAllFileDatas.add(index, fileTransferData);
        }
    }

    public void clearAllFiles() {
        mAppDatas.clear();
        mVideoDatas.clear();
        mMusicDatas.clear();
        mImageDatas.clear();
        mAllFileDatas.clear();
        mAllFileSize = 0;
        mTotalTransferedSize = 0;
    }

    public void setFileTransferSize(int index, long fileSize) {
        if (mAllFileDatas == null || index >= mAllFileDatas.size()) {
            return;
        }
        FileInfo fileTransferData = mAllFileDatas.get(index);
        fileTransferData.setTransferingSize(fileSize);
    }

    public boolean createTransferData(Context context, Intent intent) {
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            mFileType = intent.getType();
            Uri fileUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            CharSequence extra_text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
            mFileUris = new ArrayList<Uri>();
            if (fileUri != null && mFileType != null) {
                LogUtil.i("Get ACTION_SEND intent: Uri = " + fileUri + "; mimetype = " + mFileType);
                mFileUris.add(fileUri);
            } else if (extra_text != null && mFileType != null) {
                LogUtil.i("Get ACTION_SEND intent with Extra_text = " + extra_text.toString()
                        + "; mimetype = " + mFileType);
                Uri uri = FileTransferUtil.creatFileForSharedContent(context, extra_text);
                if (uri != null) {
                    LogUtil.i("created file uri : " + uri);
                    mFileUris.add(uri);
                } else {
                    LogUtil.i("create file uri is null, finish!");
                    return false;
                }
            } else {
                LogUtil.i("type is null; or sending file URI is null");
                return false;
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
            mFileType = intent.getType();
            mFileUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (mFileType == null || mFileUris == null) {
                LogUtil.i("type is null; or sending files URIs are null");
                return false;
            }
        } else if (null == mFileUris) {
            LogUtil.i("mFileUris is null;");
            return false;
        }
        ArrayList<Uri> tmp = new ArrayList<Uri>();
        for (int i = 0; i < mFileUris.size(); i++) {
            Uri uri = mFileUris.get(i);
            tmp.add(tryContentMediaUri(context, uri));
        }
        mFileUris.clear();
        for (int j = 0; j < tmp.size(); j++) {
            mFileUris.add(tmp.get(j));
        }
        return createDataList(context);
    }


    // added by Gionee luorw on 20160714 begin
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
    // added by Gionee luorw on 20160714 end

    public Uri tryContentMediaUri(Context context, Uri uri) {
        LogUtil.i("tryContentMediaUri uri = " + uri.toString());
        if (null == uri) {
            return null;
        }
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            return uri;
        } else {
            if (uri.toString().contains("com.android.contacts")) {
                return uri;
            }
            //added by luorw for GNSPR #26106 20160714 begin
            if (uri.toString().contains("com.android.providers.downloads")) {
                LogUtil.i("createTransferData uri11111111 = " + uri);
                String path = getPath(context, uri);
                LogUtil.i("createTransferData path = " + path);
                Uri fileUri = Uri.fromFile(new File(path));
                LogUtil.i("createTransferData uri22222222 = " + fileUri);
                return fileUri;
            }
            //added by luorw for GNSPR #26106 20160714 end
            String path = FileTransferUtil.getRealPathFromURI(context, uri);
            if (path != null) {
                uri = Uri.fromFile(new File(path));
            } else {
                return null;
            }
            return uri;
        }
    }

    //modified by luorw for GNSPR #26106 20160705 begin
    public boolean createDataList(Context context) {
//        mFileSendData = FileSendData.getInstance();
        Uri fileUri = mFileUris.get(0);
        if (fileUri != null && fileUri.toString().contains("com.android.providers.downloads")) {
            FileTransferUtil.getFileDataFromDownLoad(context, mFileUris);
            String[] fileNames = FileTransferUtil.downLoadFileName;
            if (fileNames == null) {
                return false;
            }
            long[] fileSizes = FileTransferUtil.downLoadFileSize;
            if (fileSizes == null) {
                return false;
            }
            mBaseSendData.addFileTransferDataForDM(context, mFileUris, FileTransferUtil.downLoadFileName, FileTransferUtil.downLoadFileSize);
            return true;
        } else {
            String[] fileNames = FileTransferUtil.getFileNamesFromURI(
                    context, mFileUris, mFileType);
            if (fileNames == null) {
                return false;
            }
            String[] filePaths = FileTransferUtil.getFilePathsFromURI(
                    context, mFileUris, mFileType);
            long[] fileSizes = FileTransferUtil.getFileSizesFromURI(
                    context, mFileUris, mFileType);
            if (fileSizes == null) {
                return false;
            }
            mBaseSendData.addFileTransferData(context, mFileUris, fileNames, fileSizes,
                    filePaths);
            return true;
        }
    }

    //modified by luorw for GNSPR #26106 20160705 end
    public void calculateAllFileSize() {
        for (FileInfo info : mAllFileDatas) {
            mAllFileSize += info.getFileSize();
        }
        LogUtil.i("calculateAllFileSize = " + mAllFileSize);
    }

    public long getAllFileSize() {
        return mAllFileSize;
    }

    public void setAllFileSize(long size) {
        mAllFileSize = size;
    }

    public long updateAllFileSize(long cancelSize) {
        return mAllFileSize - cancelSize;
    }

    public void setTotalTransferedSize(long size) {
        mTotalTransferedSize += size;
    }

    public long getTotalTransferedSize() {
        return mTotalTransferedSize;
    }

    public void updateCancelAllState(int currentTransferIndex) {
        for (int i = 0; i < mAllFileDatas.size(); i++) {
            FileInfo info = mAllFileDatas.get(i);
            if (info.getState() == Constants.DEFAULT_FILE_TRANSFER_STATE || info.getState() == Constants.FILE_TRANSFERING || i == currentTransferIndex) {
                info.setState(Constants.FILE_TRANSFER_CANCEL);
            }
        }
    }

    public void setAllFileIcon(Context context) {
        CacheUtil cacheUtil = new CacheUtil(null, context);
        Bitmap bitmap = null;
        Bitmap fileBitmape = BitmapFactory.decodeResource(context.getResources(), R.drawable.file_icon_folder);
        Bitmap musicBitmape = BitmapFactory.decodeResource(context.getResources(), R.drawable.audio_icon);
        //modified by luorw for GNSPR #24579 begin
        //modified by luorw for GNSPR #26748 20160706 begin
        Iterator<FileInfo> iterator = mAllFileDatas.iterator();
        LogUtil.i("setAllFileIcon,mAllFileDatas.size = " + mAllFileDatas.size());
        int docCount = 0, appsCount = 0, imageCount = 0, musicCount = 0, videoCount = 0;
        while (iterator.hasNext()) {
            try {
                FileInfo fileInfo = iterator.next();
                //modified by luorw for GNSPR #26748 20160706 end
                switch (fileInfo.getFileType()) {
                    case Constants.TYPE_FILE:
                        docCount++;
                        fileInfo.setFileIcon(FileUtil.getBytes(fileBitmape));
                        break;
                    case Constants.TYPE_APPS:
                        appsCount++;
                        bitmap = cacheUtil.getAPKThumbBitmap(context, fileInfo.getFilePath(), fileInfo);
                        if (bitmap != null) {
                            fileInfo.setFileIcon(FileUtil.getBytes(bitmap));
                            bitmap.recycle();
                        }
                        break;
                    case Constants.TYPE_IMAGE:
                        imageCount++;
                        bitmap = cacheUtil.getImageBitmap(fileInfo.getFilePath(), fileInfo);
                        if (bitmap != null) {
                            fileInfo.setFileIcon(FileUtil.getBytes(bitmap));
                            bitmap.recycle();
                        }
                        break;
                    case Constants.TYPE_MUSIC:
                        musicCount++;
                        fileInfo.setFileIcon(FileUtil.getBytes(musicBitmape));
                        break;
                    case Constants.TYPE_VIDEO:
                        videoCount++;
                        bitmap = cacheUtil.getVideoThumbnail(fileInfo.getFilePath(), fileInfo);
                        if (bitmap != null) {
                            fileInfo.setFileIcon(FileUtil.getBytes(bitmap));
                            bitmap.recycle();
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.i("setAllFileIcon : " + e.toString());
                break;
            }
        }
        fileBitmape.recycle();
        musicBitmape.recycle();
        fileBitmape = null;
        musicBitmape = null;
        bitmap = null;
        //modified by luorw for GNSPR #24579 end
//        //added by luorw for GNNCR #51917 20161031 begin
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put(Constants.TRANSFER_LIST_COUNT, mAllFileDatas.size());
//        map.put(Constants.TRANSFER_DOC_COUNT, docCount);
//        map.put(Constants.TRANSFER_APPS_COUNT, appsCount);
//        map.put(Constants.TRANSFER_IMAGE_COUNT, imageCount);
//        map.put(Constants.TRANSFER_MUSIC_COUNT, musicCount);
//        map.put(Constants.TRANSFER_VIDEO_COUNT, videoCount);
//        //YouJuAgent.onEvent (context, Constants.EVENT_ID, Constants.TRANSFER_LIST_EVENT, map);
//        YouJuAgent.onEvent(context,"传输文件列表","传输文件类型",map);
//        //added by luorw for GNNCR #51917 20161031 end
    }

    public void updateDisconnectAllState() {
        for (FileInfo info : mAllFileDatas) {
            if (info.getState() == Constants.DEFAULT_FILE_TRANSFER_STATE || info.getState() == Constants.FILE_TRANSFERING) {
                LogUtil.i("updateDisconnectAllState=" + info.getFileName());
                info.setState(Constants.FILE_TRANSFER_FAILURE);
            }
        }
    }

}
