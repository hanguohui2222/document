package com.gionee.hotspottransmission.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;
import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by luorw on 17/5/22.
 */
public class CacheUtil {
    private Handler mHandler;
    private LruCache<String, Bitmap> mMemoryCache;
    private String CACHE_DIR;
    public static final String SCHEME = "file://";

    public CacheUtil(Handler handler, Context context) {
        CACHE_DIR = context.getCacheDir().getAbsolutePath();
        mHandler = handler;
        int maxMemorySize = (int) (Runtime.getRuntime().maxMemory() / 4);

        mMemoryCache = new LruCache<String, Bitmap>(maxMemorySize) {

            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    public static void displayImageView(String path, ImageView iv){
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();
        ImageLoader.getInstance().displayImage(SCHEME + path,iv,options);
    }

    public Bitmap getVideoThumbnail(String path, int position){
        Bitmap bm = getBitmapFromMemory(path);
        if (bm == null){
            bm = getBitmapFromLocal(path);
            if (bm != null){
                putBitmap2Memory(path,bm);
            }else{
                createImage(new VideoThumbnailRunnable(path,position));
            }
        }
        return bm;
    }

    public Bitmap getVideoThumbnail(String path, FileInfo fileInfo){
        Bitmap bm = getBitmapFromMemory(path);
        if (bm == null){
            bm = getBitmapFromLocal(path);
            if (bm != null){
                putBitmap2Memory(path,bm);
            }else{
                createImage(new VideoThumbnailRunnable(path,fileInfo));
            }
        }
        return bm;
    }

    public Bitmap getAPKThumbBitmap(Context context,String path,int position){
        Bitmap bm = getBitmapFromMemory(path);
        if (bm == null){
            bm = getBitmapFromLocal(path);
            if (bm != null){
                putBitmap2Memory(path,bm);
            }else{
                createImage(new APKThumbnailRunnable(context,path,position));
            }
        }
        return bm;
    }

    public Bitmap getAPKThumbBitmap(Context context,String path,FileInfo fileInfo){
        Bitmap bm = getBitmapFromMemory(path);
        if (bm == null){
            bm = getBitmapFromLocal(path);
            if (bm != null){
                putBitmap2Memory(path,bm);
            }else{
                createImage(new APKThumbnailRunnable(context,path,fileInfo));
            }
        }
        return bm;
    }

    public Bitmap getImageBitmap(String path, int position){
        Bitmap bm = getBitmapFromMemory(path);
        if (bm == null){
            bm = getBitmapFromLocal(path);
            if (bm != null){
                putBitmap2Memory(path,bm);
            }else{
                createImage(new ImageRunnable(path, position));
            }
        }
        return bm;
    }

    public Bitmap getImageBitmap(String path, FileInfo fileInfo){
        Bitmap bm = getBitmapFromMemory(path);
        if (bm == null){
            bm = getBitmapFromLocal(path);
            if (bm != null){
                putBitmap2Memory(path,bm);
            }else{
                createImage(new ImageRunnable(path,fileInfo));
            }
        }
        return bm;
    }

    private void putBitmap2Memory(String path, Bitmap bm) {
        mMemoryCache.put(path, bm);
    }

    public Bitmap getBitmapFromMemory(String path) {
        return mMemoryCache.get(path);
    }

    public void putBitmap2Local(String path, Bitmap bm) {
        try {
            String fileName = md5Encode(path);
            File file = new File(CACHE_DIR, fileName); 
            File parentFile = file.getParentFile();
            if(!parentFile.exists()) {
                parentFile.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(file);
            // 把图片通过流写到本地
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap getBitmapFromLocal(String path) {
        FileInputStream is = null;
        try {
            String fileName = md5Encode(path);
            File file = new File(CACHE_DIR, fileName);
            if(file.exists()) {
                is = new FileInputStream(file);
                Bitmap bm = BitmapFactory.decodeStream(is);
                return bm;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(is!=null)
            {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private String md5Encode(String string) throws Exception {
        byte[] hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    private void createImage(Runnable runnable){
        ThreadPoolManager.getInstance().executeRunnable(runnable);
    }

    class ImageRunnable implements Runnable {
        private String path;
        private int position;
        FileInfo fileInfo;

        public ImageRunnable(String path,FileInfo fileInfo) {
            this.path = path;
            this.fileInfo = fileInfo;
        }

        public ImageRunnable(String path, int position) {
            this.path = path;
            this.position = position;
        }

        @Override
        public void run() {
            Bitmap bm = FileUtil.resizeImageByOptions(path, 300,300);
            if (bm != null){
                if(fileInfo!=null){
                    fileInfo.setFileIcon(FileUtil.getBytes(bm));
                }
                putBitmap2Local(path,bm);
                putBitmap2Memory(path,bm);

                if(mHandler != null){
                    Message message = mHandler.obtainMessage();
                    message.obj = bm;
                    message.what = Constants.SET_IMAGE_BITMAP;
                    message.arg1 = position;
                    mHandler.sendMessage(message);
                }else {
                    bm.recycle();
                }
            }
        }
    }

    class VideoThumbnailRunnable implements Runnable{
        private String path;
        private int position;
        private FileInfo fileInfo;

        public VideoThumbnailRunnable(String path, int position) {
            this.path = path;
            this.position = position;
        }

        public VideoThumbnailRunnable(String path, FileInfo fileInfo) {
            this.path = path;
            this.fileInfo = fileInfo;
        }

        @Override
        public void run() {
            Bitmap bm = FileUtil.getVideoThumbnail(path);
            if (bm != null){
                if(fileInfo!=null){
                    fileInfo.setFileIcon(FileUtil.getBytes(bm));
                }
                if(mHandler != null) {
                    Message message = mHandler.obtainMessage();
                    message.obj = bm;
                    message.what = Constants.SET_IMAGE_BITMAP;
                    message.arg1 = position;
                    mHandler.sendMessage(message);
                }
                putBitmap2Local(path,bm);
                putBitmap2Memory(path,bm);
            }
        }
    }

    class APKThumbnailRunnable implements Runnable{
        private String path;
        private int position;
        private Context mContext;
        private FileInfo fileInfo;

        public APKThumbnailRunnable(Context context,String path, int position) {
            this.path = path;
            this.position = position;
            mContext = context.getApplicationContext();
        }

        public APKThumbnailRunnable(Context context,String path,FileInfo fileInfo) {
            this.path = path;
            mContext = context.getApplicationContext();
            this.fileInfo = fileInfo;
        }

        @Override
        public void run() {
            Bitmap bm = FileUtil.getAPKThumbnail(mContext,path);
            if (bm != null){
                if(fileInfo!=null){
                    fileInfo.setFileIcon(FileUtil.getBytes(bm));
                }
                if(mHandler!=null){
                    Message message = mHandler.obtainMessage();
                    message.obj = bm;
                    message.what = Constants.SET_IMAGE_BITMAP;
                    message.arg1 = position;
                    mHandler.sendMessage(message);
                }
                putBitmap2Local(path,bm);
                putBitmap2Memory(path,bm);
            }
        }
    }
}
