package com.gionee.hotspottransmission.dao;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.callback.IDataQueryCallBack;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.utils.FileTransferUtil;
import com.gionee.hotspottransmission.utils.FileUtil;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class FileDao implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "FileDao";
    private List<FileInfo> mFileList;
    private Uri mUri;
    String[] mColumns;
    private String mSelection;
    private String mSortOrder;
    private IDataQueryCallBack mListener;
    private Activity mActivity;

    public FileDao(Activity activity, IDataQueryCallBack listener) {
        mActivity = activity;
        mListener = listener;
    }

    public void queryFileData(int type) {
        mActivity.getLoaderManager().restartLoader(type, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mUri = MediaStore.Files.getContentUri("external");
        mColumns = new String[]{MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE, MediaStore.Files.FileColumns.DATE_MODIFIED};
        mSortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " desc";
        switch (id) {
            case Constants.TYPE_DOCUMENT:
                mSelection = MediaStore.Files.FileColumns.DATA + " LIKE '%.doc' or "
                        + MediaStore.Files.FileColumns.DATA + " LIKE '%.docx' or "
                        + MediaStore.Files.FileColumns.DATA + " LIKE '%.xlsx' or "
                        + MediaStore.Files.FileColumns.DATA + " LIKE '%.ppt' or "
                        //added by luorw for  GNSPR #32680 begin
                        + MediaStore.Files.FileColumns.DATA + " LIKE '%.pptx' or "
                        + MediaStore.Files.FileColumns.DATA + " LIKE '%.xls' or "
                        //added by luorw for  GNSPR #32680 end
                        + MediaStore.Files.FileColumns.DATA + " LIKE '%.wps'";
                break;
            case Constants.TYPE_COMPRESS:
                mSelection = MediaStore.Files.FileColumns.DATA + " LIKE '%.zip' or "
                        + MediaStore.Files.FileColumns.DATA + " LIKE '%.rar' or "
                        + MediaStore.Files.FileColumns.DATA + " LIKE '%.iso' or "
                        + MediaStore.Files.FileColumns.DATA + " LIKE '%.7z'";
                break;
            case Constants.TYPE_EBOOK:
                mSelection = MediaStore.Files.FileColumns.DATA + " LIKE '%.umd' or "
                        + MediaStore.Files.FileColumns.DATA + " LIKE '%.pdf' or "
                        + MediaStore.Files.FileColumns.DATA + " LIKE '%.txt' or "
                        + MediaStore.Files.FileColumns.DATA + " LIKE '%.ebk' or "
                        + MediaStore.Files.FileColumns.DATA + " LIKE '%.chm' or "
                        + MediaStore.Files.FileColumns.DATA + " LIKE '%.epub'";
                break;
            case Constants.TYPE_APK:
                mSelection = MediaStore.Files.FileColumns.DATA + " LIKE '%.apk'";
                break;
        }
        CursorLoader cursorLoader = new CursorLoader(mActivity, mUri, mColumns, mSelection, null, mSortOrder);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        LogUtil.i("onQueryComplete.");
        if (cursor != null) {
            LogUtil.i("result count: " + cursor.getCount());
            mFileList = new ArrayList<FileInfo>();
            mFileList.clear();
            while (cursor.moveToNext()) {
                final FileInfo fileInfo = new FileInfo();
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
                fileInfo.setId(id);
                fileInfo.setModifiedDate(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                String filename = FileUtil.getNameFromFilepath(path);
                fileInfo.setFileName(filename);
                fileInfo.setFilePath(path);
                //added by luorw for GNSPR #46111 #51999 20160907 begin 媒体库的文件大小与真实的文件大小不一致导致文件传输失败
                if(filename.endsWith("txt") || filename.endsWith("amr") || filename.endsWith("3gpp")){
                    LogUtil.i("FileDao txt---amr---3gpp---: ");
                    fileInfo.setFileSize(FileTransferUtil.getFileAvailableSize(path));
                }else{
                    fileInfo.setFileSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)));
                }
                //added by luorw for GNSPR #46111 #51999 20160907 end
                fileInfo.setFileDir(FileUtil.getDirName(path));
                fileInfo.setLoaderId(loader.getId());
                if (loader.getId() == Constants.TYPE_APK) {
                    fileInfo.setFileType(Constants.TYPE_APPS);
                } else {
                    fileInfo.setFileType(Constants.TYPE_FILE);
                }
                Uri uri = Uri.withAppendedPath(mUri, "" + id);
                fileInfo.setUriString(uri.toString());
                LogUtil.i("FileDao : " + uri.toString() +" , path = "+path+" , size = " + fileInfo.getFileSize());
                mFileList.add(fileInfo);
            }
            mListener.onQueryEnd(mFileList);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
