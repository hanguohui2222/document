package com.gionee.secretary.utils;

import android.content.Context;
import android.os.Build;
import android.view.WindowManager;
import android.widget.ImageView;
import com.gionee.secretary.R;


/**
 * Created by hangh on 1/18/17.
 */
public class DisplayUtils {
    public static int getDisplayWidth(Context mContext) {
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth() - 2 * dip2px(mContext, 14);
        return width;
    }

    public static int getDisplayHeight(Context mContext) {
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);

        int height = wm.getDefaultDisplay().getHeight();
        return height;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static boolean isFullScreen(){
        String model = Build.MODEL;
        if("GIONEE W919".equalsIgnoreCase(model) || "M6SPlus".equalsIgnoreCase(model)){
            //非全面屏
            return false;
        }else{
            //全面屏
            return true;
        }
    }

    public static void setBackIcon(ImageView iv){
        if(isFullScreen()){
            iv.setImageResource(R.drawable.back_icon_full);
        }else {
            iv.setImageResource(R.drawable.back_icon);
        }
    }


}
