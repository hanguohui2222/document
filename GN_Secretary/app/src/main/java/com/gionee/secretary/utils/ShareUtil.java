package com.gionee.secretary.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import com.gionee.secretary.R;


/**
 * Created by luorw on 12/14/16.
 */
public class ShareUtil {


    public static String getLauncherTopApp(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> appTasks = activityManager.getRunningTasks(1);
        if (null != appTasks && !appTasks.isEmpty()) {
            LogUtils.i("shareUtil", "getLauncherTopApp..." + appTasks.get(0).topActivity.getClassName());
            return appTasks.get(0).topActivity.getClassName();
        }
        LogUtils.i("shareUtil", "getLauncherTopApp..." + "null");
        return "";
    }

    public static Bitmap takeScreenShot(Activity activity) {
        // View是你须要截图的View
        View view = activity.getWindow().getDecorView();
        int height1 = activity.getWindow().getDecorView().getHeight();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        // 获取状况栏高度
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        // 获取屏幕长和高
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height1 - statusBarHeight);
        view.destroyDrawingCache();
        if (b1 != null && !b1.isRecycled()) {
            b1.recycle();
            b1 = null;
        }
        return b;
    }
    //modified by luorw for GNSPR #70215 20170303 begin

    /**
     * 截取scrollview的屏幕
     *
     * @param scrollView
     * @return
     */
    public static Bitmap getBitmapByView(ScrollView scrollView) {
        int h = 0;
        Bitmap bitmap = null;
        // 获取scrollview实际高度
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();
            scrollView.getChildAt(i).setBackgroundColor(
                    Color.parseColor("#ffffff"));
        }
        int screenHeight = DisplayUtils.getDisplayHeight(scrollView.getContext());
        if(h > screenHeight * 10)
            return null;

        // 创建对应大小的bitmap
        //Gionee <gn_by><zhengyt> <2017-4-1> add for Bug#100488 begin
        try {
            bitmap = Bitmap.createBitmap(scrollView.getWidth(), h,
                    Bitmap.Config.RGB_565);
            final Canvas canvas = new Canvas(bitmap);
            scrollView.draw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Gionee <gn_by><zhengyt> <2017-4-1> add for Bug#100488 end
        return bitmap;
    }
    //modified by luorw for GNSPR #70215 20170303 end


    /**
     * 压缩图片
     *
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        int options = 100;
        // 循环判断如果压缩后图片是否大于50m,大于继续压缩
        while (baos.toByteArray().length / 1024 > 50000) {
            // 重置baos
            baos.reset();
            // 这里压缩options%，把压缩后的数据存放到baos中
            if(options < 20)
                break;
            image.compress(Bitmap.CompressFormat.PNG, options, baos);
            // 每次都减少30
            options -= 30;
        }
        // 把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        // 把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    public static void savePic(Bitmap bm, File mFile, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(mFile);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            bm.recycle();
            bm = null;
            LogUtils.i("shareUtil", "savePic , path = " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void shareIntent(File mFile,Context context) {
        if (mFile != null && mFile.exists()) {
            Uri uri = Uri.fromFile(mFile);
            LogUtils.i("shareUtils", "uri = " + uri.toString());
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/*");
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.send_file)));
        } else {
            LogUtils.i("shareUtils", "文件不存在");
            Toast.makeText(context, "分享失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * added by luorw for 调用长截屏进行分享 2017-04-15
     *
     * @param uri
     * @param context
     */
    public static void shareTrainTimeTable(Uri uri, Context context) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/*");
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.send_file)));
    }
}
