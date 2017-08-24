package com.amigo.widgetdemol;

import java.util.ArrayList;
import java.util.Calendar;

import amigoui.app.AmigoAlertDialog;
import amigoui.app.AmigoDatePickerDialog;
import amigoui.app.AmigoListActivity;
import amigoui.app.AmigoProgressDialog;
import amigoui.app.AmigoTimePickerDialog;
import amigoui.app.AmigoTimePickerDialog.OnTimeSetListener;
import amigoui.widget.AmigoDateTimePickerDialog;
import amigoui.widget.AmigoListView;
import amigoui.widget.AmigoTimeDayPickerDialog;
import amigoui.widget.AmigoTimePicker;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;

public class GnAlertDialogWindow extends AmigoListActivity {

    ArrayList<String> mMultiViewArray = null;
    String[] mMultiViewStrings = {
            "带Button的对话框", 
            "单选对话框",
            "日期选择对话框",
            "时间选择对话框",
            "多选对话框",
            "Activity对话框",
            "分享对话框",
            "进度对话框",
            "日期时间选择对话框",
            "WebView对话框",
            "日期选择对话框（农历模式）",
            "日期时间选择对话框（农历模式）",
            "新的TimeDayPicker",
            "新的TimeDayPicker 上下午",
            "TimePicker 上下午",
    };
    

    private boolean[] mMultiselectChecked = new boolean[mMultiViewStrings.length]; 
    final int GN_ALERTDIALOG = 0;

    final int GN_SINGLE_CHOICE = 1 ;
    final int GN_DATEPICKER_DIALOG = 2;
    final int GN_TIMEPICKER_DIALOG = 3;

    final int GN_SHARE_DIALOG = 6 ;
    final int GN_PROGRESS_DIALOG = 7;
    final int GN_MULTI_CHOICE = 4 ;

    final int GN_ACTIVITY_DIALOG = 5;
    final int GN_DATETIMEPICKER_DIALOG = 8;
    final int GN_WEBVIEW_DIALOG = 9;
    final int GN_LUNAR_DATE_DIALOG = 10;
    final int GN_LUNAR_DATE_TIME_DIALOG = 11;
    final int GN_TIME_DAY_DIALOG = 12;
    final int GN_TIME_DAY_DIALOG_AMPM = 13;
    final int GN_TIME_DIALOG_AMPM = 14;
    
    private String []mArrayAdapter;
    private int mSelectIdx = 0;
    AmigoAlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArrayList();
        setListAdapter(new SimpleListAdapter(this, mMultiViewArray));
        mArrayAdapter = getResources().getStringArray(R.array.planets);
    }
    
    private void initArrayList() {
        mMultiViewArray = new ArrayList<String>();
        for (int i = 0; i < mMultiViewStrings.length; i++) {
            mMultiViewArray.add(mMultiViewStrings[i]);
        }
    }
    
    @Override
    protected void onListItemClick(AmigoListView l, View v, int position, long id) {
        switch (position) {
            case GN_ALERTDIALOG:
                new AmigoAlertDialog.Builder(this)
                .setTitle("标题")
                .setMessage("有按扭对话框示例，右上角无X")
                .setNegativeButton("取消", null)
                .setPositiveButton(/*Button.GN_BUTTON_RECOM_STYLE, */"确定", null)
                .show();
                break;
            case GN_PROGRESS_DIALOG:
                startProgressDialog();
                break;
            case GN_TIMEPICKER_DIALOG:
				boolean is24Hour = DateFormat.is24HourFormat(this);
//              new AmigoTimePickerDialog(this, null, 23, 23, is24Hour).show();

				OnTimeSetListener listener = new OnTimeSetListener() {
					@Override
					public void onTimeSet(AmigoTimePicker view, int hourOfDay, int minute) {
						Log.d("maxw", "hourOfDay=" + hourOfDay + " ,minute=" + minute);
					}
				};

				AmigoTimePickerDialog amigoTimePickerDialog = new AmigoTimePickerDialog(this, null, 4, 59, true);
				AmigoTimePicker timePicker = amigoTimePickerDialog.getTimePicker();
				timePicker.setMinuteDelta(5);
				timePicker.setMaxHour(5);
				amigoTimePickerDialog.show();
				
				break;
            case GN_DATEPICKER_DIALOG:
                new AmigoDatePickerDialog(this, null, 2013, 05, 25).show();
                break;
            case GN_SINGLE_CHOICE :
                new AmigoAlertDialog.Builder(this)
                .setTitle("休眠")
                .setSingleChoiceItems(mArrayAdapter,mSelectIdx,new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectIdx = which;
                        dialog.dismiss();
                    }
                })
                .show();
                break;
            case GN_ACTIVITY_DIALOG:
                Intent intent = new Intent(this,GnActivityDialog.class);
                startActivity(intent);
                break;
            case GN_SHARE_DIALOG:
                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("text/plain");
                try {
                    startActivity(Intent.createChooser(send, getString(
                            R.string.hello)));
                } catch(android.content.ActivityNotFoundException ex) {
                    // if no app handles it, do nothing
                }
                break;
                
                
            case GN_MULTI_CHOICE:
                new AmigoAlertDialog.Builder(this)
                .setTitle("多选")
 
                .setMultiChoiceItems(mMultiViewStrings, mMultiselectChecked ,new OnMultiChoiceClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						
					}
				})
                .setNegativeButton("取消", new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						 dialog.dismiss();
					}
				})
                .show();
                break;
            case GN_DATETIMEPICKER_DIALOG:
            	is24Hour = DateFormat.is24HourFormat(this);
//            	new AmigoDateTimePickerDialog(this, 2015, 6, 28, 4, 1, is24Hour, null).show();;
            	Calendar c = Calendar.getInstance();
            	c.set(2011, 2, 1, 2, 2);
            	AmigoDateTimePickerDialog d = new AmigoDateTimePickerDialog(GnAlertDialogWindow.this, null, c);
            	d.setCurrentPage(1);
            	d.show();
            	break;
            case GN_WEBVIEW_DIALOG:
                Intent webIntent = new Intent(this, GnWebViewWindow.class);
                startActivity(webIntent);
                break;
            case GN_LUNAR_DATE_DIALOG:
                showLunarDatePickerDiglog();
                break;
            case GN_LUNAR_DATE_TIME_DIALOG:
                showLunarDateTimePickerDialog();
                break;
            case GN_TIME_DAY_DIALOG:
                showTimeDayDialog(true);
                break;
            case GN_TIME_DAY_DIALOG_AMPM:
                showTimeDayDialog(false);
                break;
            case GN_TIME_DIALOG_AMPM:
                AmigoTimePickerDialog amigoTPD = new AmigoTimePickerDialog(this, null, 4, 59, false);
                AmigoTimePicker amigoTP = amigoTPD.getTimePicker();
                amigoTP.setMinuteDelta(5);
                amigoTP.setMaxHour(5);
                amigoTPD.show();
                break;
            default:
                break;
        }
    }
    
    private void showLunarDatePickerDiglog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        AmigoDatePickerDialog lunarDialog = new AmigoDatePickerDialog(this, null, year, month, day);
        lunarDialog.showLunarModeSwitch();
        lunarDialog.show();
    }
    private void showLunarDateTimePickerDialog() {
        Calendar c = Calendar.getInstance();
        c.set(2011, 2, 1, 2, 2);
        AmigoDateTimePickerDialog d = new AmigoDateTimePickerDialog(GnAlertDialogWindow.this, null, c);
        d.showLunarModeSwitch();
        d.show();
    }

    private void startProgressDialog() {
        mProgressDialog = AmigoProgressDialog.show(this, "DemoProgressDialog", "如果不点击将在10s后自动消失", false, true,
                new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mHandler.removeCallbacksAndMessages(null);
                    }
                });
        mHandler.sendMessageDelayed(Message.obtain(mHandler, PROGRESS_MSG), 10000);
    }
    
    private void  showTimeDayDialog(boolean is24hour) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        AmigoTimeDayPickerDialog dialog = new AmigoTimeDayPickerDialog(this, null, null,
                year, month, day, hour, minute, is24hour);
        dialog.showLunarModeSwitch();
        dialog.show();
    }
    
    AmigoProgressDialog mProgressDialog;
    private static final int PROGRESS_MSG = 1;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROGRESS_MSG:
                    mProgressDialog.dismiss();
                    break;

                default:
                    break;
            }
        }
        
    };
    
    @Override
    protected void onDestroy() {
        if(mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if(null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        
        super.onDestroy();
    };
    
}
