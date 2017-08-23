/*
package com.gionee.secretary.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gionee.secretary.R;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.db.TipsPointSP;

import java.util.List;

*/
/**
 * Created by luorw on 12/20/16.
 *//*

public class ShowTipsUtil {

    private static final String LOG_TAG = ShowTipsUtil.class.getSimpleName();

    public static final int GESTURE_TIPS_LANDSCAPE = 1;
    public static final int GESTURE_TIPS_VERTICAL = 2;

//    public static void showTips(final Context context, final ImageView ivTips, final ImageButton tipsPoint, final View gestrueTips, final int page) {
//        if (!TipsPointSP.isTipsShow(context, page)) {
//            ivTips.setVisibility(View.VISIBLE);
//        }
//        ivTips.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ivTips.setVisibility(View.GONE);
//                if (!TipsPointSP.isTipsShow(context, page)) {
//                    TipsPointSP.setTipsShow(context, true, page);
//                    showTipsPoint(context, tipsPoint, ivTips, page);
//                }
//                if(page == Constants.TIPS_FOR_HOME_PAGE){
//                    showLanscapeGesture(context,gestrueTips);
//                }
//            }
//        });
//        if (TipsPointSP.isTipsPointShow(context, page)) {
//            showTipsPoint(context, tipsPoint, ivTips, page);
//        }
//    }

    */
/**
     * Show Tips Utility  显示提示标签
     *
     * @param context     Context
     * @param tipsGeneral View of General Tips
     * @param tipsGesture View of Gesture Tips
     * @param tipsPoint   View of TipsPoint
     * @param page        Caller's page
     *//*

    public static void showTips(final Context context, final View tipsGeneral, final View tipsGesture, final ImageButton tipsPoint, final int page) {
        //Tips如果没有显示过，就开始显示Tips
        if (!TipsPointSP.isTipsShow(context, page)) {
            tipsGeneral.setVisibility(View.VISIBLE);
        }

        //Tips层按下消失，小黄点出现
        tipsGeneral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.d("liyh", "tipsOnClick()");
                Boolean isTipsShow = TipsPointSP.isTipsShow(context, page);
                LogUtils.d("liyh", "isTipsShow=" + isTipsShow);
                tipsGeneral.setVisibility(View.GONE);
                if (!isTipsShow) {
                    TipsPointSP.setTipsShow(context, true, page);
                    showTipsPoint(context, tipsPoint, tipsGeneral, page);
                    if (page == Constants.TIPS_FOR_HOME_PAGE) {
                        showLanscapeGesture(context, tipsGesture);
                    }
                }
            }
        });

        if (TipsPointSP.isTipsPointShow(context, page)) {
            showTipsPoint(context, tipsPoint, tipsGeneral, page);
        }
    }

//    public static void showTips(final Context context, final List<ImageView> ivTips, final ImageButton tipsPoint, final RelativeLayout tipsTouchLayer, final ImageView gestrueTips, final int page) {
//        //Tips没显示过，就开始显示Tips
//        if (!TipsPointSP.isTipsShow(context, page)) {
//            tipsTouchLayer.setVisibility(View.VISIBLE);
//            for(ImageView ivTip:ivTips) {
//                ivTip.setVisibility(View.VISIBLE);
//            }
//        }
//        tipsTouchLayer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Boolean isTipsShow = TipsPointSP.isTipsShow(context, page);
//                tipsTouchLayer.setVisibility(View.GONE);
//                for(ImageView ivTip:ivTips) {
//                    ivTip.setVisibility(View.GONE);
//                }
//                if(!isTipsShow) {
//                    TipsPointSP.setTipsShow(context, true, page);
//                    showTipsPoint(context, tipsPoint, ivTips, tipsTouchLayer,page);
//                }
//                if(page == Constants.TIPS_FOR_HOME_PAGE){
//                    showLanscapeGesture(context,gestrueTips);
//                }
//            }
//        });
//
//        //小黄点是否已显示
//        if (TipsPointSP.isTipsPointShow(context, page)) {
//            tipsTouchLayer.setVisibility(View.GONE);
//            showTipsPoint(context, tipsPoint, ivTips, tipsTouchLayer,page);
//        }
//    }

//Deprecated
//    public static void showTips(final Context context, final List<View> ivTips, final ImageButton tipsPoint, final View tipsTouchLayer, final ImageView gestrueTips, final int page) {
//        //Tips没显示过，就开始显示Tips
//        if (!TipsPointSP.isTipsShow(context, page)) {
//            tipsTouchLayer.setVisibility(View.VISIBLE);
//            for(View ivTip:ivTips) {
//                ivTip.setVisibility(View.VISIBLE);
//            }
//        }
//        tipsTouchLayer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Boolean isTipsShow = TipsPointSP.isTipsShow(context, page);
//                tipsTouchLayer.setVisibility(View.GONE);
//                for(View ivTip:ivTips) {
//                    ivTip.setVisibility(View.GONE);
//                }
//                if(!isTipsShow) {
//                    TipsPointSP.setTipsShow(context, true, page);
//                    showTipsPoint(context, tipsPoint, ivTips, tipsTouchLayer,page);
//                    if(page == Constants.TIPS_FOR_HOME_PAGE){
//                        showLanscapeGesture(context,gestrueTips);
//                    }
//                }
//            }
//        });
//
//        //小黄点是否已显示
//        if (TipsPointSP.isTipsPointShow(context, page)) {
//            tipsTouchLayer.setVisibility(View.GONE);
//            showTipsPoint(context, tipsPoint, ivTips, tipsTouchLayer,page);
//        }
//    }

//    public static void showLanscapeGesture(final Context context, ImageView imageView){
//        if(!TipsPointSP.isLandscapeGestureShow(context)){
//            imageView.setVisibility(View.VISIBLE);
//            imageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    TipsPointSP.setLandscapeGestureShow(context,true);
//                    if(!TipsPointSP.isVerticallyGestureShow(context)){
//                        v.setBackgroundResource(R.drawable.gesture_down);
////                        TipsPointSP.setVerticallyGestureShow(context,true);
//                    }else{
//                        v.setVisibility(View.GONE);
//                    }
//                }
//            });
//        }
//        else if (!TipsPointSP.isVerticallyGestureShow(context)) {
//            imageView.setBackgroundResource(R.drawable.gesture_down);
//            imageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    TipsPointSP.setVerticallyGestureShow(context,true);
//                    if(!TipsPointSP.isLandscapeGestureShow(context)) {
//                        v.setBackgroundResource(R.drawable.gesture_left_and_right);
//                    }
//                    v.setVisibility(View.GONE);
//                }
//            });
//
//        }
//    }

    */
/**
     * Utility to show Gesture Tips in Calendar Activity  显示手势提示
     *
     * @param context     Context  上下文
     * @param tipsGesture View of Gesture Tips  手势提示层
     *//*

    public static void showLanscapeGesture(final Context context, final View tipsGesture) {
        tipsGesture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(!TipsPointSP.isVerticallyGestureShow(context)){
//                    //Show Vertical Gesture Tips  当前为显示垂直手势提示
//                    LogUtils.d("liyh","Gesture Tips: Show Vertical Tips");
//                    ImageView gestureTipsImage = (ImageView) v.findViewById(R.id.gesture_tips);
//                    gestureTipsImage.setBackgroundResource(R.drawable.gesture_down);
//                    TipsPointSP.setVerticallyGestureShow(context,true);
//                }
//                else {
//                    //全部手势提示消失
//                    LogUtils.d("liyh","Gesture Tips: All tips gone");
//                    tipsGesture.setVisibility(View.GONE);
//                }
                tipsGesture.setVisibility(View.GONE);
            }
        });

        if (!TipsPointSP.isLandscapeGestureShow(context)) {
            ImageView gestureTipsImage = (ImageView) tipsGesture.findViewById(R.id.gesture_tips);
            gestureTipsImage.setVisibility(View.VISIBLE);
            tipsGesture.setVisibility(View.VISIBLE);
            TipsPointSP.setLandscapeGestureShow(context, true);
        }
//        else if (!TipsPointSP.isVerticallyGestureShow(context)) {
//            LogUtils.d("liyh","Gesture Tips: Show Vertical Tips");
//            ImageView gestureTipsImage = (ImageView) tipsGesture.findViewById(R.id.gesture_tips);
//            gestureTipsImage.setVisibility(View.VISIBLE);
//            gestureTipsImage.setBackgroundResource(R.drawable.gesture_down);
//            tipsGesture.setVisibility(View.VISIBLE);
//            TipsPointSP.setVerticallyGestureShow(context,false);
//        }
    }

    public static void setGestureTipsState(Context context, ImageView imageView, int state) {
        if (!TipsPointSP.isLandscapeGestureShow(context) || !TipsPointSP.isVerticallyGestureShow(context)) {     //两种手势提示其中一种没显示过
            //先显示水平滑动手势
            switch (state) {
                case GESTURE_TIPS_LANDSCAPE:
                    TipsPointSP.setLandscapeGestureShow(context, true);
                    imageView.setVisibility(View.GONE);
                    showLanscapeGesture(context, imageView);
                    break;
                case GESTURE_TIPS_VERTICAL:
                    if (TipsPointSP.isLandscapeGestureShow(context)) {
                        TipsPointSP.setVerticallyGestureShow(context, true);
                        imageView.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    }

    */
/**
     * Show TipsPoint  显示小黄点
     *
     * @param context     Context  上下文
     * @param tipsPoint   View of TipsPoint  小黄点控件
     * @param tipsGeneral General Tips View  提示显示层
     * @param page        The caller's page  调用者页面
     *//*

    private static void showTipsPoint(final Context context, final ImageButton tipsPoint, final View tipsGeneral, final int page) {
        tipsPoint.setVisibility(View.VISIBLE);
        TipsPointSP.setTipsPointShow(context, true, page);
        tipsPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.d("liyh", "TipsPoint OnClick()");
                tipsPoint.setVisibility(View.GONE);
                tipsGeneral.setVisibility(View.VISIBLE);
                TipsPointSP.setTipsPointShow(context, false, page);
            }
        });
    }

//    private static void showTipsPoint(final Context context, final ImageButton tipsPoint, final ImageView ivTips, final int page) {
//        tipsPoint.setVisibility(View.VISIBLE);
//        TipsPointSP.setTipsPointShow(context, true, page);
//        tipsPoint.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tipsPoint.setVisibility(View.GONE);
//                ivTips.setVisibility(View.VISIBLE);
//                TipsPointSP.setTipsPointShow(context, false, page);
//            }
//        });
//    }
//
//    private static void showTipsPoint(final Context context, final ImageButton tipsPoint, final List<ImageView> ivTips, final int page) {
//        tipsPoint.setVisibility(View.VISIBLE);
//        TipsPointSP.setTipsPointShow(context, true, page);
//        View.OnClickListener tipsPointClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tipsPoint.setVisibility(View.GONE);
//                for(ImageView ivTip:ivTips) {
//                    ivTip.setVisibility(View.VISIBLE);
//                }
//                TipsPointSP.setTipsPointShow(context, false, page);
//            }
//        };
//        tipsPoint.setOnClickListener(tipsPointClickListener);
//    }
//
//    private static void showTipsPoint(final Context context, final ImageButton tipsPoint, final List<View> ivTips, final View tipsTouchLayer, final int page) {
//        tipsPoint.setVisibility(View.VISIBLE);
//        TipsPointSP.setTipsPointShow(context, true, page);
//        View.OnClickListener tipsPointClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tipsPoint.setVisibility(View.GONE);
//                tipsTouchLayer.setVisibility(View.VISIBLE);
//                for(View ivTip:ivTips) {
//                    ivTip.setVisibility(View.VISIBLE);
//                }
//                TipsPointSP.setTipsPointShow(context, false, page);
//            }
//        };
//        tipsPoint.setOnClickListener(tipsPointClickListener);
//
//    }

}
*/
