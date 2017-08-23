package com.gionee.secretary.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.bean.FlightSchedule;
import com.gionee.secretary.constants.Constants;

import amigoui.widget.AmigoTextView;

public class CardDetailsUtils {
    private final static String COLON = "：";

    public static void setTextView(TextView tv, String item, String content) {
        if ((content != null) && !content.equals("null") && (!content.trim().equals(""))) {
            tv.setText(item + COLON + content);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    public static void setTextView(TextView tv, String item, String content, boolean isImportantInfo) {
        if ((content != null) && !content.equals("null") && (!content.trim().equals(""))) {
            tv.setText(item + COLON + content);
        } else {
            if (isImportantInfo) {
                tv.setText(item + COLON + " -- ");
            } else {
                tv.setVisibility(View.GONE);
            }
        }
    }

    public static void setTextViewWithoutColon(Context context, TextView tv, String prefix, String content) {
        if ((content != null) && !content.equals("null") && !prefix.equals("null") && NavigateUtil.canNavigate(context)) {
            tv.setText(prefix + content);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    public static void setTextViewWithTimeTable(TextView tv, String prefix, String content) {
        if ((content != null) && !content.equals("null") && !prefix.equals("null")) {
            tv.setText(prefix + content);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    public static void setTextViewWithPreSuffix(Context context, TextView tv, String prefix, String content, String suffix) {
        if ((content != null) && !content.equals("null") && NavigateUtil.canNavigate(context)) {
            tv.setText(prefix + content + suffix);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    public static void setTextViewWithPreSuffix(Context context, TextView tv, String prefix, String content, String suffix, boolean isImportantInfo) {
        if ((content != null) && !content.equals("null") && NavigateUtil.canNavigate(context)) {
            tv.setText(prefix + content + suffix);
        } else {
            if (isImportantInfo) {
                tv.setText(prefix + " -- " + suffix);
            } else {
                tv.setVisibility(View.GONE);
            }
        }
    }

    public static void setTextView(TextView tv, String content) {
        if (!TextUtils.isEmpty(content) && !content.contains("null") && (!content.trim().equals("")) && content != null) {
            tv.setText(content);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    public static void setTextView(TextView tv, String content, boolean isImportanceInfo) {
        if (!TextUtils.isEmpty(content) && !content.equals("null") && (!content.trim().equals("")) && content != null) {
            tv.setText(content);
        } else {
            if (isImportanceInfo) {
                tv.setText("--");
            } else {
                tv.setVisibility(View.GONE);
            }
        }
    }

    public static void setTextView(TextView tv, SpannableStringBuilder text, String content) {
        if (!TextUtils.isEmpty(content) && !content.equals("null") && (!content.trim().equals(""))) {
            tv.setText(text);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    /*
    根据内容设置提示是否显示
     */
    public static void setShowStatus(View tv, String content) {
        if (!TextUtils.isEmpty(content) && !content.equals("null") && (!content.trim().equals("")) && content != null)
            tv.setVisibility(View.VISIBLE);
        else
            tv.setVisibility(View.GONE);
    }

    public static boolean isEmpty(String str) {
        if (!TextUtils.isEmpty(str) && !str.equals("null") && !str.equals("无数据")) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isEmptyValue(String str) {
        if (!TextUtils.isEmpty(str) && !"".equals(str)) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }
}
