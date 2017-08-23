package com.gionee.secretary;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import com.gionee.secretary.calendar.CalendarManager;
import com.gionee.secretary.R;
import com.gionee.secretary.bean.BaseSchedule;
import com.gionee.secretary.dao.ScheduleInfoDao;
import com.gionee.secretary.module.settings.PasswordModel;
import com.gionee.secretary.utils.DateUtils;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.view.SplashActivity;
import com.gionee.theme.export.ThemeAppBgReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by liyu on 16/5/20.
 */
public class WidgetProvider extends AppWidgetProvider {
	private static final String TAG = "WidgetProvider";
	private List<BaseSchedule> schedules;
	private static final String SP_NAME = "WidgetProvider";
	private static final String SP_KEY_ISFIRST = "isFirst";
	private static final String SP_KEY_HIDESCHEDULE = "hideSchedule";
	private ThemeAppBgReader mThemeAppBgReader;
	private Bitmap bm;

	private void updateView(Context context) {
		ComponentName thisWidget = new ComponentName(context,
				WidgetProvider.class);
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_ui);
		AppWidgetManager appmanager = AppWidgetManager.getInstance(context);
		updateDate(context, views);
		// Gionee sunyang 2017-01-16 modify for GNSPR #64900 begin
//		Intent intent = new Intent(context, CalendarActivity.class);
		Intent intent = new Intent(context, SplashActivity.class);
		// Gionee sunyang 2017-01-16 modify for GNSPR #64900 end
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		views.setOnClickPendingIntent(R.id.ll, pendingIntent);
		ThemeAppBgReader.getInstance().create(context);
		Drawable bgDrawable = ThemeAppBgReader.getInstance().getAppBackground(ThemeAppBgReader.App.SECRETARY);
		if (bgDrawable == null) {
			LogUtils.i(TAG,"SM"+"bgDrawable is null");
			if("GIONEE GN5007".equalsIgnoreCase(Build.MODEL)){
				//大金刚2项目
				views.setImageViewResource(R.id.iv_bg, R.drawable.widget_default_bg_by);
			}else {
				//其他项目
				views.setImageViewResource(R.id.iv_bg, R.drawable.widget_default_bg);
			}
			appmanager.updateAppWidget(thisWidget, views);
		} else {
			synchronized (this){
				if("GIONEE GN5007".equals(Build.MODEL)){
					//大金刚2项目
					if(bm != null){
						bm.recycle();
					}
					BitmapDrawable bd = (BitmapDrawable) bgDrawable;
					bm = bd.getBitmap();
					views.setImageViewBitmap(R.id.iv_bg,bm);
				}else {
					//其他项目
					views.setImageViewResource(R.id.iv_bg, R.drawable.widget_default_bg);
				}
				appmanager.updateAppWidget(thisWidget, views);
			}
		}
	}
	
	private boolean isHideSchedule(Context context){
		return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).getBoolean(SP_KEY_HIDESCHEDULE, false);
	}
	
	private void setHideSchedule(Context context, boolean hide){
		context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit().putBoolean(SP_KEY_HIDESCHEDULE, hide).commit();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent.getAction();
		LogUtils.i(TAG,"SM"+"Widget onReceive and action=" + action);
		if ("android.intent.action.TIME_SET".equals(action) || "gn_calendar_datechange".equals(action)){
			CalendarManager cm = CalendarManager.getInstance();
			if (cm != null){
				cm.onDateChanged();
			}
		}
		if ("com.gionee.secretary.HIDESCHEDLE".equals(action)){
			if (intent.getBooleanExtra("hide", false)){
				hideSchedule(context);
				setHideSchedule(context,true);
			}else{
				setHideSchedule(context,false);
				updateView(context);
			}
		}
		else{
			updateView(context);
		}
		
		if("amigo.intent.action.chameleon.POWER_SAVING_MODE".equals(action)){
				PasswordModel.getInstance(context).updateLockState(true);
		}
	}
	
	private void hideSchedule(Context context){
		ComponentName thisWidget = new ComponentName(context,
				WidgetProvider.class);
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_ui);
		AppWidgetManager appmanager = AppWidgetManager.getInstance(context);
		views.setTextViewText(R.id.tv_date_1, "全天");
		views.setTextViewText(R.id.tv_title_1, "暂无日程");
		views.setTextViewText(R.id.tv_date_2, "");
		views.setTextViewText(R.id.tv_title_2, "");
		//Fixed #75858 by liyu begin
		Intent intent = new Intent(context, SplashActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		views.setOnClickPendingIntent(R.id.ll, pendingIntent);
		//Fixed #75858 by liyu end
		appmanager.updateAppWidget(thisWidget, views);
	}


	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		LogUtils.d(TAG,"SM"+"T110 onEnabled");
	}

	private void updateDate(Context context, RemoteViews views) {
		ScheduleInfoDao scheduleInfoDao = ScheduleInfoDao.getInstance(context);
		try {
			schedules = scheduleInfoDao.getSchedulesForWidget();
		}catch(Exception e){
			Log.e("liyu", "WidgetProvider updateDate e  = "+e.getMessage());
		}
		LogUtils.d(TAG,"liyu"+"schedules.size() = " + schedules.size());
		SharedPreferences sp = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		if (sp.getBoolean(SP_KEY_ISFIRST, true)) {
			if (schedules.size() > 0) {
				sp.edit().putBoolean(SP_KEY_ISFIRST, false).commit();
			} else {
				return;
			}
		}
		views.setViewVisibility(R.id.ll_1, View.VISIBLE);
		views.setViewVisibility(R.id.ll_2, View.VISIBLE);
		views.setViewVisibility(R.id.tv_empty, View.INVISIBLE);
		
		if (isHideSchedule(context)){
			views.setTextViewText(R.id.tv_date_1, "全天");
			views.setTextViewText(R.id.tv_title_1, "暂无日程");
			views.setTextViewText(R.id.tv_date_2, "");
			views.setTextViewText(R.id.tv_title_2, "");
			return;
		}
		
		if (schedules.size() == 0) {
			views.setTextViewText(R.id.tv_date_1, "全天");
			views.setTextViewText(R.id.tv_title_1, "暂无日程");
			views.setTextViewText(R.id.tv_date_2, "");
			views.setTextViewText(R.id.tv_title_2, "");
		} else {
			BaseSchedule event = schedules.get(0);
			if (!event.isAllDay) {
				views.setTextViewText(R.id.tv_date_1,
						DateUtils.time2String(event.date));
				setTimeTask(event, views, context);
				views.setTextViewText(R.id.tv_title_1, event.getTitle());
			} else {
				views.setTextViewText(R.id.tv_date_1, "全天");
				views.setTextViewText(R.id.tv_title_1, event.getTitle());
			}

			if (schedules.size() > 1) {
				event = schedules.get(1);
				if (!event.isAllDay) {
					views.setTextViewText(R.id.tv_date_2,
							DateUtils.time2String(event.date));
					if (schedules.get(0).isAllDay) {
						setTimeTask(event, views, context);
					}
				} else {
					views.setTextViewText(R.id.tv_date_2, "全天");
				}
				views.setTextViewText(R.id.tv_title_2, event.getTitle());
			} else {
				views.setTextViewText(R.id.tv_date_2, "");
				views.setTextViewText(R.id.tv_title_2, "");
			}
		}
	}

	private void setTimeTask(BaseSchedule event, RemoteViews views,
			Context context) {
		Timer timer = new Timer();
		try {
			timer.schedule(new UpdateTime(event, views, context), event.date);
		} catch (Exception e) {

		}
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
	}

	public class UpdateTime extends TimerTask {
		BaseSchedule event;
		RemoteViews views;
		Context context;

		protected UpdateTime(BaseSchedule event, RemoteViews views,
				Context context) {
			this.event = event;
			this.views = views;
			this.context = context;
		}

		@Override
		public void run() {
			updateView(context);
		}
	}
}

