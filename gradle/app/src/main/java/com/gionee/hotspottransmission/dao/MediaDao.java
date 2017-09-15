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
import com.gionee.hotspottransmission.utils.FileUtil;
import com.gionee.hotspottransmission.utils.LogUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luorw on 4/25/16.
 */
public class MediaDao implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "MediaDao";

    private List<FileInfo> mFileList;
    private Uri mUri;
    private String mSelection;
    private String mSortOrder;
    private IDataQueryCallBack mListener;
    private Activity mActivity;

    public MediaDao(Activity activity, IDataQueryCallBack listener) {
        mActivity = activity;
        mListener = listener;
    }

    public void queryMediaData(int type) {
        mActivity.getLoaderManager().restartLoader(type,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case Constants.TYPE_MUSIC:
                mUri = MediaStore.Audio.Media.getContentUri(Constants.VOLUME_NAME);
                mSelection = MediaStore.Audio.Media.IS_MUSIC + " != ''";
                mSortOrder = MediaStore.Files.FileColumns.MIME_TYPE + " asc, " + MediaStore.Files.FileColumns.TITLE + " asc";
                break;
            case Constants.TYPE_VIDEO:
                mUri = MediaStore.Video.Media.getContentUri(Constants.VOLUME_NAME);
                mSelection = MediaStore.MediaColumns.DISPLAY_NAME + " != ''";
                mSortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " desc";
                break;
            case Constants.TYPE_IMAGE:
                mUri = MediaStore.Images.Media.getContentUri(Constants.VOLUME_NAME);
                mSelection = MediaStore.MediaColumns.DISPLAY_NAME + " != ''";
                mSortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " desc";
                break;
        }
        String[] columns = new String[]{MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.DISPLAY_NAME,MediaStore.MediaColumns.DATA};
        CursorLoader cursorLoader = new CursorLoader(mActivity,mUri,columns,mSelection,null,mSortOrder);
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
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                fileInfo.setId(id);
                fileInfo.setFileName(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)));
                fileInfo.setFileSize(cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)));
                fileInfo.setModifiedDate(cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                fileInfo.setFilePath(path);
                fileInfo.setFileDir(FileUtil.getDirName(path));
                fileInfo.setFileType(loader.getId());
                fileInfo.setLoaderId(loader.getId());
                Uri uri = Uri.withAppendedPath(mUri, "" + id);
                fileInfo.setUriString(uri.toString());
                mFileList.add(fileInfo);
            }
            mListener.onQueryEnd(mFileList);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
