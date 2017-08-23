package com.gionee.secretary.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gionee.secretary.R;

/**
 * Created by liyy on 16-5-30.
 */
public class TextUtilTools {
    private static final String TAG = "TextUtilTools";
    private static String sEllipsis = "\u2026";
    private static int sTypefaceHighlight = Typeface.BOLD;

    public static SpannableStringBuilder hightLightText(String matcher, String compile, int color) {
//        SpannableStringBuilder spannable = new SpannableStringBuilder("我是谁a用户a谁用户");
//        Pattern pp = Pattern.compile("用户");
//        Matcher m = pp.matcher("我是谁a用户a谁用户");
        if (CardDetailsUtils.isEmpty(compile)) return null;
        LogUtils.i(TAG, "match:" + matcher + "   compile:" + compile);
        SpannableStringBuilder spannable = new SpannableStringBuilder(matcher);
        try {
            Pattern pp = Pattern.compile(compile);
            Matcher m = pp.matcher(matcher);
            CharacterStyle span = null;


            while (m.find()) {
                span = new ForegroundColorSpan(color);// 需要重复！
                spannable.setSpan(span, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            return spannable;
        } catch (java.util.regex.PatternSyntaxException e) {
            LogUtils.i(TAG, "TextUtils......PatternSyntaxException....contains...special...string");
            return spannable;
        }
    }

    public static String[] splitText(String str, String chars) {
        if (str != null && chars != null) {
            return str.trim().split(chars);
        }
        return null;
    }

    public static SpannableStringBuilder hightBlueLightText(String hightLight) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(hightLight);
        CharacterStyle spanPre = new ForegroundColorSpan(0x66000000);
        CharacterStyle spanAfter = new ForegroundColorSpan(0x4567a0);

        String aa[] = hightLight.split("\\ ");
        spannable.setSpan(spanPre, 0, aa[0].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(spanAfter, aa[0].length(), aa[1].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    private final static String prefix_en = "[";
    private final static String suffix_en = "]";
    private final static String prefix_cn = "【";
    private final static String suffix_cn = "】";

    public static String getMessageSign(String body) {
        if (TextUtils.isEmpty(body)) {
            return body;
        }
        String prefix = "";
        String suffix = "";
        int prefixPos, suffixPos;
        String s = body.trim();
        if (s.startsWith(prefix_en)) {
            suffixPos = s.indexOf(suffix_en);
            if (suffixPos > 0)
                prefix = s.substring(1, suffixPos);
        } else if (s.startsWith(prefix_cn)) {
            suffixPos = s.indexOf(suffix_cn);
            if (suffixPos > 0)
                prefix = s.substring(1, suffixPos);
        }

        if (s.endsWith(suffix_en)) {
            prefixPos = s.lastIndexOf(prefix_en);
            if (prefixPos > 0)
                suffix = s.substring(prefixPos + 1, s.length() - 1);
        } else if (s.endsWith(suffix_cn)) {
            prefixPos = s.lastIndexOf(prefix_cn);
            if (prefixPos > 0)
                suffix = s.substring(prefixPos + 1, s.length() - 1);
        }

        //JSONArray result = new JSONArray();
        if (prefix.length() > 0 && prefix.length() < 9) {
            //result.put(prefix);
            return prefix;
        }
        if (suffix.length() > 0 && suffix.length() < 9) {
            //result.put(suffix);
            return suffix;
        }
        return "";

        //return result.toString();
    }


    public static void setHighLightText(String mFullText, String mTargetString, TextViewSnippet textView, Context context) {
        if (mFullText == null || mTargetString == null) {
            return;
        }
        String patternString = Pattern.quote(mTargetString);//"\\b" + Pattern.quote(target);
        Pattern mPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        int mHighLightColor = context.getResources().getColor(R.color.search_highlight_color);


        String fullTextLower = mFullText.toLowerCase();
        String targetStringLower = mTargetString.toLowerCase();

        LogUtils.i("liyy", "fullTextLower:" + fullTextLower + "  ,targetStringLower:" + targetStringLower);

        int startPos = 0;
        int searchStringLength = targetStringLower.length();
        int bodyLength = fullTextLower.length();

        Matcher m = mPattern.matcher(mFullText);
        if (m.find(0)) {
            startPos = m.start();
        }

        TextPaint tp = textView.getPaint();

        float textFieldWidth = 758;//应该是显示控件的宽度，最大可以是屏幕宽度
//        LogUtils.i("liyy", "textFieldWidth:" + textFieldWidth + ",  searchStringWidth:" + searchStringWidth);

        //30 108
        //662 108

        String snippetString = null;
        float ellipsisWidth = tp.measureText(sEllipsis);
        textFieldWidth -= (2F * ellipsisWidth); // assume we'll need one on both ends

        int offset = -1;
        int start = -1;
        int end = -1;
                /* TODO: this code could be made more efficient by only measuring the additional
                 * characters as we widen the string rather than measuring the whole new
                 * string each time.
                 */
        while (true) {
            offset += 1;

            int newstart = Math.max(0, startPos - offset);
            int newend = Math.min(bodyLength, startPos + searchStringLength + offset);

            if (newstart == start && newend == end) {
                // if we couldn't expand out any further then we're done
                break;
            }
            start = newstart;
            end = newend;

            // pull the candidate string out of the full text rather than body
            // because body has been toLower()'ed
            String candidate = mFullText.substring(start, end);
            if (tp.measureText(candidate) > textFieldWidth) {
                // if the newly computed width would exceed our bounds then we're done
                // do not use this "candidate"
                break;
            }

            snippetString = String.format(
                    "%s%s%s",
                    start == 0 ? "" : sEllipsis,
                    candidate,
                    end == bodyLength ? "" : sEllipsis);
        }


        if (snippetString == null) {
            snippetString = mFullText.substring(startPos, startPos + searchStringLength);
        }
        SpannableString spannable = new SpannableString(snippetString);
        int findStart = 0;
        m = mPattern.matcher(snippetString);
        while (m.find(findStart) && m.start() != m.end()) {
            spannable.setSpan(new StyleSpan(sTypefaceHighlight),
                    m.start(), m.end(), 0);
            spannable.setSpan(new ForegroundColorSpan(mHighLightColor),
                    m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            findStart = m.end();
        }

        textView.setText(spannable);

    }

    public static void setHighLightText(final String mFullText, final String mTargetString, final TextView textView, final Context context) {
        if (mFullText == null || mTargetString == null) {
            return;
        }

        //add by zhengjl at 2017-1-22 for  GNSPR #65759

        if (TextUtils.isEmpty(mTargetString) || !mFullText.contains(mTargetString)) {
            textView.setText(mFullText);
            return;
        }

        /*modify by zhengjl at 2017-2-5 for GNSPR #66336 begin
        之前用textView.post方法，获取控件的测量宽度，效果不好，导致刷新不及时，改为ViewTreeObserver好一些
        */
//        ViewTreeObserver vto2 = textView.getViewTreeObserver();
//        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {

                /*modify by zhengjl at 2017-2-7 for search*/
        float textFieldWidth = 600;
        String patternString = Pattern.quote(mTargetString);//"\\b" + Pattern.quote(target);
        Pattern mPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        int mHighLightColor = context.getResources().getColor(R.color.search_highlight_color);


        String fullTextLower = mFullText.toLowerCase();
        String targetStringLower = mTargetString.toLowerCase();

        LogUtils.i("liyy", "fullTextLower:" + fullTextLower + "  ,targetStringLower:" + targetStringLower);

        int startPos = 0;
        int searchStringLength = targetStringLower.length();
        int bodyLength = fullTextLower.length();

        Matcher m = mPattern.matcher(mFullText);
        if (m.find(0)) {
            startPos = m.start();
        }

        TextPaint tp = textView.getPaint();

//                float textFieldWidth = 758;//应该是显示控件的宽度，最大可以是屏幕宽度
//        LogUtils.i("liyy", "textFieldWidth:" + textFieldWidth + ",  searchStringWidth:" + searchStringWidth);

        //30 108
        //662 108

        String snippetString = null;
        float ellipsisWidth = tp.measureText(sEllipsis);
        textFieldWidth -= (2F * ellipsisWidth); // assume we'll need one on both ends

        int offset = -1;
        int start = -1;
        int end = -1;
                /* TODO: this code could be made more efficient by only measuring the additional
                 * characters as we widen the string rather than measuring the whole new
                 * string each time.
                 */
        while (true) {
            offset += 1;

            int newstart = Math.max(0, startPos - offset);
            int newend = Math.min(bodyLength, startPos + searchStringLength + offset);

            if (newstart == start && newend == end) {
                // if we couldn't expand out any further then we're done
                break;
            }
            start = newstart;
            end = newend;

            // pull the candidate string out of the full text rather than body
            // because body has been toLower()'ed
            String candidate = mFullText.substring(start, end);
            //LogUtils.e("zjl","candidate-------------" + candidate);
            if (tp.measureText(candidate) > textFieldWidth) {
                // if the newly computed width would exceed our bounds then we're done
                // do not use this "candidate"
                break;
            }

            snippetString = String.format(
                    "%s%s%s",
                    start == 0 ? "" : sEllipsis,
                    candidate,
                    end == bodyLength ? "" : sEllipsis);
        }


        if (snippetString == null) {
            snippetString = mFullText.substring(startPos, startPos + searchStringLength);
        }
        //     LogUtils.e("zjl","snippetString:" + snippetString);
        SpannableString spannable = new SpannableString(snippetString);
        int findStart = 0;
        m = mPattern.matcher(snippetString);
        while (m.find(findStart) && m.start() != m.end()) {
            spannable.setSpan(new StyleSpan(sTypefaceHighlight),
                    m.start(), m.end(), 0);
            spannable.setSpan(new ForegroundColorSpan(mHighLightColor),
                    m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            findStart = m.end();
        }

        textView.setText(spannable);

//
//            }
//        });

//        textView.post(new Runnable()
//        {
//            @Override
//            public void run() {
//                //add by zhengjl 获取实际的测量值
//                float textFieldWidth = textView.getMeasuredWidth();
//            }
//        });

        /*modify by zhengjl at 2017-2-5 for GNSPR #66336 end*/


    }

    public static String formatTime(int paramInt, String param) {
        int i = paramInt / 3600;
        int j = paramInt % 3600 / 60;
        int k = paramInt % 60;
        StringBuilder localStringBuilder = new StringBuilder();
        appendFormat(localStringBuilder, i, true, param);
        appendFormat(localStringBuilder, j, true, param);
        appendFormat(localStringBuilder, k, false, param);
        return localStringBuilder.toString();
    }

    private static void appendFormat(StringBuilder paramStringBuilder, int paramInt, boolean paramBoolean, String paramString) {
        if (paramInt < 10)
            paramStringBuilder.append("0");
        paramStringBuilder.append(paramInt);
        if (paramBoolean)
            paramStringBuilder.append(paramString);
    }

}
