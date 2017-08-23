package com.gionee.secretary.widget;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.utils.LogUtils;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

import amigoui.widget.AmigoEditText;

public class NoteEditText extends AmigoEditText {
    private onSelectionChangedListener listener;

    public NoteEditText(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public NoteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public NoteEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        // Fixed #81703 #81363 begin
        String str = this.getText().toString();
        try {
            str = str.substring(this.getSelectionStart(), this.getSelectionEnd()).trim();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (("png".equals(str) || (str.endsWith("record.amr") && str.length() == 24)) && (id == android.R.id.cut || id == 16908341)) {
            this.clearFocus();
            return false;
        }
        // Fixed #81703 #81363 end
        // Fixed #75346 begin
        if (id == android.R.id.copy || id == android.R.id.cut) {
            super.onTextContextMenuItem(id);
            ClipboardManager clip = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if(clip.getPrimaryClip() == null){
                return false;
            }
            String text = clip.getPrimaryClip().getItemAt(0).getText().toString();
            LogUtils.d("liyu", "before copy text = " + text);
            Pattern p = Pattern.compile(Constants.MATCH_PATTERN);
            Matcher m = p.matcher(text);
            while (m.find()) {
                text = text.replace(text.substring(m.start(), m.end()), "");
                m = p.matcher(text);
            }
            if ((text.endsWith("png") || text.endsWith(".amr"))) {
                text = "";
            }
//			p = Pattern.compile(Constants.CHILD_PATTERN);
//			m = p.matcher(text);
//			while (m.find()) {
//				text = text.replace(text.substring(m.start(), m.end()), "");
//				m = p.matcher(text);
//			}
//			text = text.trim().replace("png", "");
            LogUtils.d("liyu", "after copy text = " + text);
            clip.setPrimaryClip(ClipData.newPlainText("text", text));
            return true;
        }
        // Fixed #75346 end

        if (id == android.R.id.paste) {
//			long start = System.currentTimeMillis();
            int lastCursorPosion = getSelectionStart();
            ClipboardManager clip = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if(clip.getPrimaryClip() == null){
                return false;
            }
            String text = clip.getPrimaryClip().getItemAt(0).getText().toString();
            LogUtils.d("liyu", "paste text = " + text);
//			Pattern p = Pattern.compile(Constants.MATCH_PATTERN,Pattern.CASE_INSENSITIVE);
//			Matcher m = p.matcher(text);
//			while (m.find()) {
//				text = text.replace(text.substring(m.start(), m.end()), "");
//				m = p.matcher(text);
//			}
            //fix #72780 begin
//			p = Pattern.compile(Constants.CHILD_PATTERN);
//			m = p.matcher(text);
//			while (m.find()) {
//				text = text.replace(text.substring(m.start(), m.end()), "");
//				m = p.matcher(text);
//			}
//			text = text.trim().replace("png", "");
            //fix #72780 end
//			if (TextUtils.isEmpty(text)) {
//				text = " ";// TODO 如果没有变化无法关闭剪贴板，解决方法再看
//				this.stopTextSelectionMode();
//			}
//			try {
//				this.getEditableText().insert(lastCursorPosion, text);
//			} catch (Exception e) {
//
//			}
//			LogUtils.d("liyu", "paste = "+(System.currentTimeMillis()-start));
//			return true;
        }
        try {
            return super.onTextContextMenuItem(id);
        } catch (Exception e) {
            return false;
        }

    }

    boolean flag;
    long start = 0;

    // liyu fix #70001 part1 start
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            start = System.currentTimeMillis();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            long end = System.currentTimeMillis();
            if (end - start <= 500) {
                flag = true;
            } else {
                flag = false;
            }
            start = 0;
        }
        return super.onTouchEvent(event);
    }

    // liyu fix #70001 part1 end

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        flag = false; // liyu Fix #71511
        try {
            return super.onKeyDown(keyCode, event);
        } catch (Exception e) {
            // s10 #91235 java.lang.IndexOutOfBoundsException 这个地方也会报错表示理解不能
            return false;
        }

    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        try {
            // Fixed #66842 by liyu begin
            String text = this.getText().toString();
            if (selStart == selEnd && text.length() >= selStart + 71
                    && text.substring(selStart + 68, selStart + 71).equals("amr")) {
                setSelection(selStart + 71);
            }
            // Fixed #66842 by liyu end

            // liyu fix #71397 start
            if (selStart != selEnd) {
                if ((selEnd - selStart == 4 && text.substring(selStart, selEnd).equals("file"))
                        || selEnd - selStart == 17) {  // billUnChecked.png && billISChecked.png
                    Pattern p = Pattern.compile(Constants.MATCH_PATTERN, Pattern.CASE_INSENSITIVE);
                    Matcher m = p.matcher(text);
                    this.setLongClickable(false);
                    if (m.find()) {
                        String str = text.substring(m.start(), m.end());
                        if (str.equals(Constants.PATH_BILL_CHECKED_IMG) || str.equals(Constants.PATH_BILL_UNCHECKED_IMG)) {
                            setSelection(m.end() + 1);
                        } else {
                            setSelection(m.end());
                        }
                    }

                }
                return;
            }
            // liyu fix #71397 end

            // liyu fix #70001 part2 start
            if (selStart >= Constants.PATH_BILL_CHECKED_IMG.length() && this.getText().charAt(selStart) == ' ') {
                String str = text.substring(selStart - (Constants.PATH_BILL_CHECKED_IMG.length()), selStart);
                if (str.trim().equals(Constants.PATH_BILL_CHECKED_IMG) || str.trim().equals(Constants.PATH_BILL_UNCHECKED_IMG)) {
                    setSelection(selStart + 1);
                    this.setLongClickable(false);
                    if (listener != null && flag) {
                        listener.onBillClicked(selStart);
                        flag = false;
                    }
                }
            } else if (text.length() > selStart + Constants.PATH_BILL_CHECKED_IMG.length()) {
                String str = text.substring(selStart, selStart + Constants.PATH_BILL_CHECKED_IMG.length());
                if (str.trim().equals(Constants.PATH_BILL_CHECKED_IMG) || str.trim().equals(Constants.PATH_BILL_UNCHECKED_IMG)) {
                    setSelection(selStart + Constants.PATH_BILL_CHECKED_IMG.length() + 1);
                    this.setLongClickable(false);
                    if (listener != null && flag) {
                        listener.onBillClicked(selStart);
                        flag = false;
                    }
                }
            } else {
                this.setLongClickable(true);
            }
            // liyu fix #70001 part2 end
        } catch (Exception e) {
            // "NoteEditText onSelectionChanged e = "+e.getMessage().toString());
        }
        super.onSelectionChanged(selStart, selEnd);
    }

    public void setListener(onSelectionChangedListener listener) {
        this.listener = listener;
    }

    public interface onSelectionChangedListener {
        void onBillClicked(int sel);
    }

}
