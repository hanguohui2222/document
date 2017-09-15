package com.gionee.hotspottransmission.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.constants.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by luorw on 5/9/17.
 */
public class FileUtil {
    private static String passwordForHotspot;

    public static void setPasswordForHotspot(String passwordForHotspot) {
        FileUtil.passwordForHotspot = passwordForHotspot;
    }

    private static boolean isCardMounted() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static void createReceiveDir(Context context) {
        if (isCardMounted()) {
            String path = Environment.getExternalStorageDirectory() + "/" + context.getResources().getString(R.string.dir_name) + "/";
            File kuaichuanFile = new File(path);
            if (!kuaichuanFile.exists()) {
                kuaichuanFile.mkdir();
            }
            File appsFile = new File(path + Constants.DIR_APP);
            if (!appsFile.exists()) {
                appsFile.mkdir();
            }
            File pictureFile = new File(path + Constants.DIR_IMAGE);
            if (!pictureFile.exists()) {
                pictureFile.mkdir();
            }
            File audiosFile = new File(path + Constants.DIR_MUSIC);
            if (!audiosFile.exists()) {
                audiosFile.mkdir();
            }
            File videosFile = new File(path + Constants.DIR_VIDEO);
            if (!videosFile.exists()) {
                videosFile.mkdir();
            }
            File filesFile = new File(path + Constants.DIR_FILE);
            if (!filesFile.exists()) {
                filesFile.mkdir();
            }
        }
    }

    private static void passwordMd5Encode(boolean isGroupTransfer) {
        byte[] hash = new byte[0];
        String pwd = null;
        if(isGroupTransfer){
            pwd = Constants.WIFI_HOT_SPOT_GROUP_TRANSFER_PREFIX;
        }else{
            pwd = Constants.WIFI_HOT_SPOT_SSID_PREFIX;
        }
        try {
            hash = MessageDigest.getInstance("MD5").digest(pwd.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        passwordForHotspot = hex.toString().substring(0, 10);
    }

    public static String getPasswordForHotspot(boolean isGroupTransfer) {
        if (passwordForHotspot == null || "".equals(passwordForHotspot)) {
            passwordMd5Encode(isGroupTransfer);
        }
        return passwordForHotspot;
    }

    // 使用BitmapFactory.Options的inSampleSize参数来缩放
    public static Bitmap resizeImageByOptions(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;// 不加载bitmap到内存中
        BitmapFactory.decodeFile(path, options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 1;

        if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
            int sampleSize = (outWidth / width + outHeight / height) / 2;
            options.inSampleSize = sampleSize;
        }

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static String getDirName(String filePath){
        String[] str = filePath.split("/");
        int index = str.length-2;
        return str[index];
    }

    public static Bitmap getVideoThumbnail(String filePath) {
        return ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MICRO_KIND);
    }

    public static Bitmap getAPKThumbnail(Context context,String path){
        File file = new File(path);
        if(!file.exists()){
            return null;
        }
        Drawable mDrawable = null;
        Bitmap mBitmap = null;
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if(info != null){
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = path;
            appInfo.publicSourceDir = path;
            try {
                mDrawable = appInfo.loadIcon(pm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            mDrawable = context.getResources().getDrawable(R.drawable.ic_launcher);
        }
        if(mDrawable != null){
            BitmapDrawable bd = (BitmapDrawable)mDrawable;
            mBitmap = bd.getBitmap();
            return mBitmap;
        }
        return null;
    }

    public static String formatDateString(Context context, long time) {
        DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(context);
        DateFormat timeFormat = android.text.format.DateFormat
                .getTimeFormat(context);
        Date date = new Date(time);
        return dateFormat.format(date) + " " + timeFormat.format(date);
    }

    public static String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format("%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format("%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    public static int getTransferPercent(long transferSize,long totalSize){
        float value = (((float)transferSize/totalSize)*100);
        return (int)value;
    }

    public static long getFileSize(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            return 0;
        }
        return getFileSizes(file);
    }

    public static long getFileSizes(File f) throws Exception {// 取得文件大小
        long s = 0;
        if (f.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(f);
            s = fis.available();
        } else {
            f.createNewFile();
            System.out.println("文件不存在");
        }
        return s;
    }

    /**
     *
     * @param filePath
     *            文件绝对路径
     * @return true：文件存在 false：文件不存在，或者该路径为目录
     */
    public static boolean isFileExist(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        if (file.isFile()) {
            return true;
        }
        return false;
    }

    public static String formatDateToStr(long time, String type) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(type);
        Date date = new Date(time);
        return dateFormat.format(date);
    }

    public static String getNameFromFilepath(String filepath) {
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            return filepath.substring(pos + 1);
        }
        return "";
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream baops = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baops);
        return baops.toByteArray();
    }

    public static void removeFileByPath(String filePath){
        if(filePath != null && !"".equals(filePath)){
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
