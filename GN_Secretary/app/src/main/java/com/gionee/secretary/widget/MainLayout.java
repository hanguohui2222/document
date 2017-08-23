package com.gionee.secretary.widget;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.gionee.secretary.R;
import com.gionee.secretary.utils.DensityUtils;

public class MainLayout extends RelativeLayout {
    private Context context;
    private int orignalTop;//schedule初始高度
    private int windowHeight;
    private ViewDragHelper childViewDragHelper;
    private RecyclerView scheduleView;
    private RelativeLayout dateLayout;

    public MainLayout(Context context) {
        super(context);
        init(context);
    }

    public MainLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MainLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
//		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
//		windowHeight = wm.getDefaultDisplay().getHeight() ;//+ DensityUtils.dip2px(context, 100);//加上顶部下部的额外宽度估计乱写一个近似值
//		initChildViewDragHelper();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        scheduleView = (RecyclerView) findViewById(R.id.rv);
        dateLayout = (RelativeLayout) findViewById(R.id.date_layout);
        orignalTop = DensityUtils.dip2px(context, ScrollLayout.WEEK_HEIGHT_DIP * 6);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, orignalTop, 0, 0);
        dateLayout.setLayoutParams(params);
    }
//	
//	@Override
//	public void computeScroll() {
//		if (childViewDragHelper.continueSettling(true)) {
//			postInvalidate();
//		}
//	}
//	
//	@Override
//	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		return childViewDragHelper.shouldInterceptTouchEvent(ev);
//	}
//
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		try{
//			childViewDragHelper.processTouchEvent(event);
//		}catch(Exception e){
//			LogUtils.e("liyu", "axiba!!!!");
//		}
//		return true;
//	}
//	
//	private void initChildViewDragHelper(){
//		childViewDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
//
//			int recodY = 0;
//			
//			@Override
//			public boolean tryCaptureView(View arg0, int arg1) {
//				return arg0 == contentLayout;
//			}
//
//			@Override
//			public int clampViewPositionVertical(View child, int top, int dy) {
////				LogUtils.d("liyu1", "top = "+top);
////				LogUtils.d("liyu", "child.getBottom() = "+child.getBottom());
////				LogUtils.d("liyu1", "child.getHeight() = "+child.getHeight());
////				LogUtils.d("liyu", "windowHeight = "+windowHeight);
////				LogUtils.d("liyu", "-------------------------------------------------------------------");
//				if (top >= orignalTop || child.getHeight() < windowHeight ) {
//					return orignalTop;
//				} else {
////					LogUtils.d("liyu", "orignalTop - child.getTop() = "+(orignalTop - child.getTop()) );
////					LogUtils.d("liyu", "child.getHeight() - windowHeight = "+(child.getHeight() - windowHeight) );
////					if (orignalTop - child.getTop() < child.getHeight() - windowHeight){//可以滑
////						LogUtils.d("liyu1", "if");
////						return top;//top;//child.getHeight()-windowHeight;
////					}else{//不能滑
////						LogUtils.d("liyu", "caluate top = "+ (-child.getMeasuredHeight()+windowHeight+orignalTop-DensityUtils.dip2px(context, 54)));
////						LogUtils.d("liyu", "orignal top = "+ top);
////						return Math.max(top, -child.getMeasuredHeight()+windowHeight+orignalTop-DensityUtils.dip2px(context, 54));//orignalTop - child.getHeight() + windowHeight
////					}
//					int count = scheduleView.getAdapter().getItemCount();
////					LogUtils.d("liyu", "caluate top = "+ (-child.getMeasuredHeight()+windowHeight+orignalTop-DensityUtils.dip2px(context, 54)));
////					LogUtils.d("liyu", "orignal top = "+ top);
//					LogUtils.d("liyu", "count = "+ count);
//					LogUtils.d("liyu", "child.getMeasuredHeight() = "+ child.getMeasuredHeight());
//					return Math.max(top, -child.getMeasuredHeight()+windowHeight+orignalTop-DensityUtils.dip2px(context, 280-count*45)); 
//				}
//				
//				
//			}
//			
//			@Override
//			public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
//				
//			}
//
//			@Override
//			public int getViewVerticalDragRange(View child) {
//				return contentLayout.getMeasuredHeight();
//			}
//		});
//	}

}
