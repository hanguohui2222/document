package com.amigo.widgetdemol;

import amigoui.app.AmigoActivity;
import amigoui.forcetouch.AmigoForceTouchClickCallback;
import amigoui.forcetouch.AmigoForceTouchConstant;
import amigoui.forcetouch.AmigoForceTouchController;
import amigoui.forcetouch.AmigoForceTouchControllerCallback;
import amigoui.forcetouch.AmigoForceTouchMenuCallback;
import amigoui.forcetouch.AmigoForceTouchPreviewCallback;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.graphics.Point;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GnForceTouchWindow extends AmigoActivity implements AmigoForceTouchClickCallback {

    private final static String LOGTAG = "GnForceTouchWindow";
    private TextView mForceView;
    private AmigoForceTouchController mController;
    AmigoForceTouchMenuCallback mCreater;
    private Button mBtn1;
    private Button mBtn2;
    private Button mBtn3;
    private Button mBtn4;
    private Button mBtn5;
    private Button mBtn6;
//    private ImageView mImgView;
    private TextView mTxtView1;
    private ImageView mBtn7;
    private ImageView mBtn8;
    private ImageView mBtn9;
    private ImageView mBtn10;
    private ImageView mBtn11;
    private ImageView mBtn12;
    
    private Bitmap mBmpType1;
    private Bitmap mBmpType2;
    
//    private void startHidePopMenuAnimations(final View view) {
//        Log.e(LOGTAG, "startHidePopMenuAnimations start");
//        final ScaleAnimation anim = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
//                Animation.ABSOLUTE, 400.0f, Animation.ABSOLUTE, 400.0f);
//        anim.setDuration(500);
//        mImgView.setAnimation(anim);
//        anim.startNow();
//        mImgView.setVisibility(View.INVISIBLE);
//        anim.setAnimationListener(new AnimationListener() {
//
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//            }
//        });
//    }
    
    Handler mHandler = new Handler() {
        
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_force_touch_demo);
//        mForceView = (TextView) findViewById(R.id.forceTxt);
        mBtn1 = (Button) findViewById(R.id.button1);
        mBtn2 = (Button) findViewById(R.id.button2);
        mBtn3 = (Button) findViewById(R.id.button3);
        mBtn4 = (Button) findViewById(R.id.button4);
        mBtn7 = (ImageView) findViewById(R.id.button7);
        mBtn8 = (ImageView) findViewById(R.id.button8);
        mBtn9 = (ImageView) findViewById(R.id.button9);
        mBtn10 = (ImageView) findViewById(R.id.button10);
        mBtn11 = (ImageView) findViewById(R.id.button11);
        mBtn12 = (ImageView) findViewById(R.id.button12);
//        mBtn5 = (Button) findViewById(R.id.button5);
//        mBtn6 = (Button) findViewById(R.id.button6);
//        mTextView1 = (TextView) findViewById(R.id.text1);
//        mImgView = (ImageView) findViewById(R.id.button7);
        mBtn2.setOnLongClickListener(new View.OnLongClickListener() {
            
            @Override
            public boolean onLongClick(final View v) {
                Toast.makeText(GnForceTouchWindow.this, "onLongClick", Toast.LENGTH_SHORT).show();
//                startHidePopMenuAnimations(v);
//                mBtn5.setScaleX(1.5f);
//                mBtn5.setScaleY(1.5f);
                
                return false;
            }
        });
        mBtn2.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                ObjectAnimator obj = ObjectAnimator.ofFloat(mBtn5, "scaleX", 1f, 0.2f);
                obj.setDuration(1000);
                obj.start();
            }
        });
        initForceTouchBitmap();
        mController = new AmigoForceTouchController(this);
        initForceTouchMenu();
        mController.setAmigoForceTouchMenuCallback(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, mCreater);
        mController.setAmigoForceTouchMenuCallback(AmigoForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW, mCreater);
        mController.setAmigoForceTouchClickCallback(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, this);
        mController.setAmigoForceTouchClickCallback(AmigoForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW, this);
        
        mController
                .setAmigoForceTouchControllerCallback(new AmigoForceTouchControllerCallback() {

                    @Override
                    public Bitmap getForceTouchView(View touchView, Point p,
                            Point marginP) {
                        Bitmap bmp = null;
                        int id = touchView.getId();
                        switch (id) {
                        case R.id.button7: {
                            getViewInfo(mBtn7, p, marginP);
                            bmp = mBmpType1;
                        }
                            break;
                        case R.id.button9: {
                            getViewInfo(mBtn9, p, marginP);
                            bmp = mBmpType1;
                        }
                            break;
                        case R.id.button10: {
                            getViewInfo(mBtn10, p, marginP);
                            bmp = mBmpType1;
                        }
                            break;
                        case R.id.button8: {
                            getViewInfo(mBtn8,p,marginP);
                            bmp = mBmpType2;
                        }
                            break;
                        case R.id.button11: {
                            getViewInfo(mBtn11,p,marginP);
                            bmp = mBmpType2;
                        }
                            break;
                        case R.id.button12: {
                            getViewInfo(mBtn12,p,marginP);
                            bmp = mBmpType2;
                        }
                            break;
                        default:
                            break;
                        }

                        return bmp;
                    }

                    private void getViewInfo(View view, Point p, Point marginP) {
                        Rect rect = getViewRect(view);
                        p.x = rect.left;
                        p.y = rect.top;
                        marginP.x = 25;
                        marginP.y = 25;
                    }

                    @Override
                    public Bitmap getBlurBitmap() {
                        return null;
                    }
                });
        mController.setForceTouchPreviewCallback(new AmigoForceTouchPreviewCallback() {
            
            @Override
            public View onCreatePreviewView(View view) {
                // TODO Auto-generated method stub
                Button txtView = new Button(GnForceTouchWindow.this);
                txtView.setText("TestPostion3View");
                txtView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Toast.makeText(GnForceTouchWindow.this,
                                "Button#",
                                Toast.LENGTH_SHORT).show();
                        ;
                    }
                });
                return txtView;
            }
            
            @Override
            public void onClickPreviewView(View view) {
                // TODO Auto-generated method stub
                Toast.makeText(GnForceTouchWindow.this, "onClickPreviewView click Button3", Toast.LENGTH_SHORT).show();
            }
        });
        
        mController.registerForceTouchView(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, mBtn1);
        mController.registerForceTouchView(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, mBtn2);
        mController.registerForceTouchView(AmigoForceTouchConstant.MENU_TYPE_CONTENT_PREVIEW, mBtn3);
        mController.registerForceTouchView(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, mBtn4);
//        mController.registerForceTouchView(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, mBtn5);
//        mController.registerForceTouchView(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, mBtn6);
//        mController.registerForceTouchView(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, mTxtView1);
//        mController.registerForceTouchView(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, mImgView);
        mController.registerForceTouchView(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, mBtn7);
        mController.registerForceTouchView(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, mBtn8);
        mController.registerForceTouchView(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, mBtn9);
        mController.registerForceTouchView(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, mBtn10);
        mController.registerForceTouchView(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, mBtn11);
        mController.registerForceTouchView(AmigoForceTouchConstant.MENU_TYPE_QUICK_MENU, mBtn12);
    }

    private void initForceTouchBitmap() {
        mBmpType1 = BitmapFactory.decodeResource(getResources(), R.drawable.icon1);
        mBmpType2 = BitmapFactory.decodeResource(getResources(), R.drawable.icon2);
    }

    public static Rect getViewRect(View view) {
        Rect rect = new Rect();
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        rect.left = location[0];
        rect.top = location[1];
        rect.bottom = rect.top + view.getHeight();
        rect.right = rect.left + view.getWidth();

        return rect;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mController.dismissForceTouchWindow();
    }
    void initForceTouchMenu() {
        mCreater = new AmigoForceTouchMenuCallback() {

            @Override
            public void onCreateForceTouchMenu(View view, Menu menu) {
                getMenuInflater().inflate(R.menu.gn_forcetouch_common_menu,
                        menu);
            }

            @Override
            public void onPrepareForceTouchMenu(View view, Menu menu) {
                int id = view.getId();
                switch(id) {
                case R.id.button2:
                    menu.getItem(1).setEnabled(false);
                    break;
                default:
                    break;
                }
            }

            @Override
            public void onForceTouchMenuItemClick(View view,
                    MenuItem menuItem) {
                Log.e(LOGTAG,"onForceTouchMenuItemClick item="+menuItem.getItemId()+";text="+menuItem.getTitle());
            }
        };
    }

    @Override
    public boolean onLightTouchClick(View view, float pressure) {
        return false;
    }

    @Override
    public boolean onForceTouchClick(View view) {
        return false;
    }

    @Override
    public void onForceTouchClickView(View view) {
        Log.e(LOGTAG,"onTouchClick view="+view);
    }
}
