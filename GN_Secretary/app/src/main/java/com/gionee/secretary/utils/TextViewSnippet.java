package com.gionee.secretary.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import amigoui.widget.AmigoTextView;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.gionee.secretary.R;

/**
 * Created by liyy 2016-06-24
 */
public class TextViewSnippet extends AmigoTextView {
    private static String sEllipsis = "\u2026";

    private static int sTypefaceHighlight = Typeface.BOLD;

    private String mFullText;
    private String mTargetString;
    private Pattern mPattern;
    private int mHighLightColor;

    public TextViewSnippet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextViewSnippet(Context context) {
        this(context, null);
    }

    public TextViewSnippet(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHighLightColor = getContext().getResources().getColor(R.color.search_highlight_color);
        // 用于变色
//            ChangeViewColorWithChameleon();
    }

    /**
     * We have to know our width before we can compute the snippet string.  Do that
     * here and then defer to super for whatever work is normally done.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mFullText == null) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        String fullTextLower = mFullText.toLowerCase();
        String targetStringLower = mTargetString.toLowerCase();

        int startPos = 0;
        int searchStringLength = targetStringLower.length();
        int bodyLength = fullTextLower.length();

        Matcher m = mPattern.matcher(mFullText);
        if (m.find(0)) {
            startPos = m.start();
        }

        TextPaint tp = getPaint();

        float searchStringWidth = tp.measureText(mTargetString);
        float textFieldWidth = getWidth();

        String snippetString = null;
        if (searchStringWidth > textFieldWidth) {
            int end = startPos + searchStringLength <= mFullText.length() ? startPos + searchStringLength :
                    mFullText.length();
            snippetString = mFullText.substring(startPos, end);
        } else {
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
        }

        if (snippetString == null) {
            snippetString = mFullText.substring(startPos, startPos + searchStringLength);
        }
        SpannableString spannable = new SpannableString(snippetString);
        int start = 0;
        m = mPattern.matcher(snippetString);
        while (m.find(start) && m.start() != m.end()) {
            spannable.setSpan(new StyleSpan(sTypefaceHighlight),
                    m.start(), m.end(), 0);
            spannable.setSpan(new ForegroundColorSpan(mHighLightColor),
                    m.start(), m.end(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            start = m.end();
        }
        setText(spannable);

        // do this after the call to setText() above
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setText(String fullText, String target) {
        // Use a regular expression to locate the target string
        // within the full text.  The target string must be
        // found as a word start so we use \b which matches
        // word boundaries.
        String patternString = Pattern.quote(target);//"\\b" + Pattern.quote(target);
        mPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);

        mFullText = fullText;
        mTargetString = target;
        requestLayout();
    }
        
       /* // 用于变色
        private void ChangeViewColorWithChameleon() {
            if (!ChangeColorManager.isNeedChangeColor()) {
                return;
            }
            mHighLightColor = ChangeColorManager.getColorByType(ChangeColorManager.ACCENT_COLOR);
        }*/
}
