package com.gionee.hotspottransmission.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.gionee.hotspottransmission.callback.IReceivedImagesDataChangListener;
import com.gionee.hotspottransmission.utils.LogUtil;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import java.util.ArrayList;
import java.util.List;

public class ReceivedImageSourceManager {
    private static ReceivedImageSourceManager mImageDataSource;
    private List<String> mImagesUri;
    private List<String> mImagesName;
    private int mCurrentItem;
    private IReceivedImagesDataChangListener mDataChangListener;
    private static Context mContext;

    private ReceivedImageSourceManager() {

    }

    public static ReceivedImageSourceManager getInstance(Context context) {
        if (mImageDataSource == null) {
            synchronized (ReceivedImageSourceManager.class) {
                if (mImageDataSource == null){
                    mContext = context.getApplicationContext();
                    mImageDataSource = new ReceivedImageSourceManager();
                }
            }
        }
        return mImageDataSource;
    }

    private Handler mHandler = new Handler(mContext.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    String filePath = (String) msg.obj;
                    String imageUrl = ImageDownloader.Scheme.FILE.wrap(filePath);
                    mImagesUri.add(imageUrl);
                    LogUtil.i("imageShow....ReceivedImagedataSource:" + filePath);
                    if(mDataChangListener != null){
                        mDataChangListener.onDataChanage();
                    }
                    break;
            }
        }
    };

    public void setReceivedImagePath(String filePath) {
        if (mImagesUri == null){
            mImagesUri = new ArrayList<String>();
        }
        Message msg = new Message();
        msg.obj = filePath;
        msg.what = 1;
        mHandler.sendMessage(msg);
    }

    public void setReceivedImageName(String name){
        if (mImagesName == null){
            mImagesName = new ArrayList<String>();
        }
        mImagesName.add(name);
    }

    public List<String> getReceivedImagePathes() {
        return mImagesUri;
    }

    public List<String> getmImagesNames() {
        return mImagesName;
    }

    public void clearAllReceivedImagePath() {
        if (mImagesUri != null) {
            mImagesUri.clear();
        }
    }
    public void clearAllReceivedImageTitle() {
        if (mImagesName != null) {
            mImagesName.clear();
        }
    }
    public void setCurrentItem(int currentItem){
        mCurrentItem = currentItem;
    }

    public int getCurrentItem(){
        return mCurrentItem;
    }

    public void setListener(IReceivedImagesDataChangListener dataChanageListener){
        mDataChangListener = dataChanageListener;
    }

}
