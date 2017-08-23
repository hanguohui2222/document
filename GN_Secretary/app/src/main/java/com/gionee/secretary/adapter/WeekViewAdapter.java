package com.gionee.secretary.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gionee.secretary.R;
import com.gionee.secretary.calendar.CalendarManager;
import com.gionee.secretary.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by liyu on 16/5/12.
 */
public class WeekViewAdapter extends BaseAdapter {
    private Calendar calStartDate;
    private CalendarManager mCalendarManager;
    private List<Integer> list;
    private ScaleAnimation sa;
    private boolean hasAnimation;

    private ArrayList<Calendar> datas;

    private ArrayList<Calendar> getDates() {
        int day = calStartDate.get(Calendar.DAY_OF_WEEK);
        calStartDate.add(calStartDate.DAY_OF_MONTH, 1 - day);
        ArrayList<Calendar> dates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(calStartDate.getTime());
            dates.add(calendar);
            calStartDate.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dates;
    }

    private Context mActivity;

    public WeekViewAdapter(Context a, Calendar cal) {
        mCalendarManager = CalendarManager.getInstance();
        calStartDate = cal;
        mActivity = a;
        datas = getDates();
        sa = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(300);
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Calendar getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(mActivity, R.layout.item_calendar, null);
        TextView tv_number = (TextView) view.findViewById(R.id.tv_number);
//        TextView tv_remarks = (TextView)view.findViewById(R.id.tv_remarks);
        ImageView iv = (ImageView) view.findViewById(R.id.iv);
        Calendar calCalendar = datas.get(position);

        final int iDay = calCalendar.get(Calendar.DAY_OF_WEEK);

        if (iDay == 7 || iDay == 1) {
            tv_number.setTextColor(mActivity.getResources().getColor(R.color.calender_item_number_first_or_end));
//        	tv_remarks.setTextColor(mActivity.getResources().getColor(R.color.calender_item_number_remarks_first_or_end));
        }
//        tv_remarks.setText(mCalendarManager.getChineseDay(calCalendar));
        int dayOfMonth = calCalendar.get(Calendar.DAY_OF_MONTH);
        tv_number.setText(dayOfMonth + "");

        if (mCalendarManager.hasSchedule(DateUtils.formatDate2StringByMonth(calCalendar.getTime()), dayOfMonth)) {
            iv.setVisibility(View.VISIBLE);
        } else {
            iv.setVisibility(View.INVISIBLE);
        }

//        if (dayOfMonth == 1){
//        	tv_number.setText(calCalendar.get(Calendar.MONTH)+1 +"月");
//        	tv_number.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
//        	RelativeLayout.LayoutParams layoutParams = 
//        			new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        	layoutParams.setMargins(0,6,0,0);//4个参数按顺序分别是左上右下
//        	layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE); 
//        	tv_number.setLayoutParams(layoutParams); 
//        }else{
//        	tv_number.setText(dayOfMonth +"");
//        }
//        if (position == mSelection){
//        	RelativeLayout rl = (RelativeLayout)view.findViewById(R.id.rl);
//    		rl.setBackgroundResource(R.drawable.bg_calendar_selected);	
//        }

        Calendar cal = Calendar.getInstance();
        ImageView bg = (ImageView) view.findViewById(R.id.bg);
        if (mCalendarManager.getCalendar().get(Calendar.YEAR) == calCalendar.get(Calendar.YEAR)
                && mCalendarManager.getCalendar().get(Calendar.DAY_OF_YEAR) == calCalendar.get(Calendar.DAY_OF_YEAR)) {
            bg.setBackgroundResource(R.drawable.bg_calendar_selected);
            if (hasAnimation) {
                bg.startAnimation(sa);
                new AnimThread().start();//周视图每点周日会多次刷新，而log显示notifyDataSetChanged()只走了一次，不知到为什么，所以暂时先用线程关闭动画标记
            }
        } else if (cal.get(Calendar.YEAR) == calCalendar.get(Calendar.YEAR)
                && cal.get(Calendar.DAY_OF_YEAR) == calCalendar.get(Calendar.DAY_OF_YEAR)) {
            bg.setBackgroundResource(R.drawable.bg_calendar_today);
            tv_number.setTextColor(mActivity.getResources().getColor(R.color.month_item_number_selected));
//            tv_remarks.setTextColor(mActivity.getResources().getColor(R.color.month_item_remarks_selected));
        }
        return view;
    }

    public void notifyDataSetChangedWithAnimation() {
        hasAnimation = true;
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    class AnimThread extends Thread {

        @Override
        public void run() {
            SystemClock.sleep(300);
            hasAnimation = false;
        }

    }

}
