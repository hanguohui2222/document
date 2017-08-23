package com.gionee.secretary.ui.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoAlertDialog;
import amigoui.app.AmigoProgressDialog;
import amigoui.widget.AmigoEditText;
import amigoui.widget.AmigoTextView;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gionee.secretary.R;
import com.gionee.secretary.bean.VoiceNoteBean;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.dao.VoiceNoteDao;
import com.gionee.secretary.listener.NoDoubleClickListener;
import com.gionee.secretary.utils.DensityUtils;
import com.gionee.secretary.utils.DisplayUtils;
import com.gionee.secretary.utils.ImageLoader;
import com.gionee.secretary.utils.LogUtils;
import com.gionee.secretary.utils.RemindUtils;
import com.gionee.secretary.utils.RuntimePemissionUtils;
import com.gionee.secretary.utils.ShareUtil;
import com.gionee.secretary.widget.BillImageSpan;
import com.gionee.secretary.widget.PhotoImageSpan;
import com.gionee.secretary.widget.PictureClickableSpan;
import com.gionee.secretary.widget.SoundImageSpan;
import com.gionee.secretary.widget.SoundsClickableSpan;
import com.youju.statistics.YouJuAgent;

public class NoteDetailActivity extends PasswordBaseActivity {
    private int noteid;
    private AmigoEditText etDate;
    private AmigoEditText etContent;
    private long createtime;
    private TextView mNoteRemindTimeTv;
//    private AmigoTextView mDeleteTv;
//    private AmigoTextView mEditTv;
    private LinearLayout mNoteRemindTimeLayout;
    private String createTime = "";
    private VoiceNoteBean voiceNoteBean;
    private final String PATH = Environment.getExternalStorageDirectory() + "/secretary/实时转写/";
    private static final String PATH_BILL_CHECKED_IMG = "file:///data/user/0/secretary/files/billIsChecked.png";
    private static final String PATH_BILL_UNCHECKED_IMG = "file:///data/user/0/secretary/files/billUnChecked.png";
    private Bitmap bm_bill_unchecked;
    private Bitmap bm_bill_checked;
    private ScrollView mScrollView;
    private ProgressBar mProgressBar;
    private AmigoProgressDialog pd;
    private List<Bitmap> bitmapList = new ArrayList<>();
    private List<ImageSpan> imageSpanList = new ArrayList<>();

    private boolean reflushData = true;

    /*modify by zhengjl at 2017-2-14 优化备忘图片加载 not begin*/
    //private ImageLoader imageLoader;
    //added by luorw for #75707 2017-03-24 begin
    private ImageView mImageViewShare;
    ImageLoader loader;
    //added by luorw for #75707 2017-03-24 end
    private MyHandler mHandler = new MyHandler(this);
    private boolean isResumeRefresh;
    OnActivityStateListener listener;
    public interface OnActivityStateListener {
         void onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
        noteid = getIntent().getIntExtra("noteid", 0);
        // Gionee sunyang 2017-01-18 modify for GNSPR #65546 begin
        isFromNotification();
        // Gionee sunyang 2017-01-18 modify for GNSPR #65546 end
        initActionBar();
        checkBillImageSrc();
        initView();
        initListener();
        registerRefreshReceiver();
    }

    public void setOnActivityStateListener(OnActivityStateListener listener){
        this.listener = listener;
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("zjl", "reflushData:" + reflushData);
        if (reflushData && checkAndRequestPermission()){
            isResumeRefresh = true;
            loadData();
            isResumeRefresh = false;
        }
        reflushData = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);
        MenuItem menuDelete = menu.findItem(R.id.detail_delete);
        MenuItem menuEdit = menu.findItem(R.id.detail_edit);
        if(DisplayUtils.isFullScreen()){
            //全面屏
            menuDelete.setIcon(R.drawable.gn_private_delete_enable_full);
            menuEdit.setIcon(R.drawable.edit_enable_full);
        }else {
            menuDelete.setIcon(R.drawable.gn_private_delete_enable);
            menuEdit.setIcon(R.drawable.edit_enable);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.detail_delete:
                clickDelete();
                break;
            case R.id.detail_edit:
                clickEdit();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkAndRequestPermission() {
        return RuntimePemissionUtils.checkAndRequestForRunntimePermission(
                this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(permissions[0])
                && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            finish();
        }else {
            loadData();
        }
    }

    private BroadcastReceiver mRefreshListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.i("mRefreshListReceiver", "REFRESH_FOR_NOTE_DETAIL_UI");
            if(checkAndRequestPermission()){
                loadData();
            }
        }
    };

    private void registerRefreshReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.REFRESH_FOR_NOTE_DETAIL_UI);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        this.registerReceiver(mRefreshListReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listener != null){
            listener.onDestroy();
        }
        unRegisterRefreshListReceiver();
        if (loader != null) {
            loader.recycle();
        }
        if (mDataList != null) {
            mDataList.clear();
        }
        for (Bitmap b : bitmapList) {
            if (!b.isRecycled()) {
                b.recycle();
                b = null;
            }
        }
        for (ImageSpan span : imageSpanList) {
            if (span instanceof PhotoImageSpan) {
                PhotoImageSpan photoImageSpan = (PhotoImageSpan) span;
                photoImageSpan.recycle();
            } else if (span instanceof SoundImageSpan) {
                SoundImageSpan soundImageSpan = (SoundImageSpan) span;
                soundImageSpan.recycle();
            }
        }
        imageSpanList.clear();
        bitmapList.clear();
        Runtime.getRuntime().gc();
    }

    private void unRegisterRefreshListReceiver() {
        if (mRefreshListReceiver != null) {
            this.unregisterReceiver(mRefreshListReceiver);
        }
    }

    private void checkBillImageSrc() {
        bm_bill_unchecked = BitmapFactory.decodeResource(getResources(), R.drawable.voice_note_item_normal);
        bm_bill_checked = BitmapFactory.decodeResource(getResources(), R.drawable.voice_note_item_selected);
    }

    private void initView() {
        etDate = (AmigoEditText) findViewById(R.id.create_time_title);
        etContent = (AmigoEditText) findViewById(R.id.note_edit);
        mNoteRemindTimeTv = (TextView) findViewById(R.id.note_remind_time_tv);
        mNoteRemindTimeLayout = (LinearLayout) findViewById(R.id.note_remind_time_layout);
        mScrollView = (ScrollView) findViewById(R.id.content_scroll_view);
        mProgressBar = (ProgressBar) findViewById(R.id.content_progress_bar);

    }

    private void initListener(){

    }

    private void initActionBar() {
        AmigoActionBar mActionBar = getAmigoActionBar();
        mActionBar.setDisplayOptions(AmigoActionBar.DISPLAY_SHOW_CUSTOM, AmigoActionBar.DISPLAY_SHOW_CUSTOM);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        //mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gionee sunyang 2017-01-18 modify for GNSPR #65546 begin
                if (isNotifation) {
                    onBackPressed();
                } else {
                    NoteDetailActivity.this.finish();
                }
                // Gionee sunyang 2017-01-18 modify for GNSPR #65546 end
            }
        });
        View view = getLayoutInflater().inflate(R.layout.actionbar_note_detail, null);
        ImageView back_btn = (ImageView) view.findViewById(R.id.back_btn);
        DisplayUtils.setBackIcon(back_btn);
        //added by luorw for #75707 2017-03-24 begin
        mImageViewShare = (ImageView) view.findViewById(R.id.img_share);
        mImageViewShare.setVisibility(View.GONE);
        if(DisplayUtils.isFullScreen()){
            mImageViewShare.setImageResource(R.drawable.share_btn_full);
        }else {
            mImageViewShare.setImageResource(R.drawable.share_btn);
        }
        mImageViewShare.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                YouJuAgent.onEvent(NoteDetailActivity.this, NoteDetailActivity.this.getResources().getString(R.string.click_share));
                //modified by luorw for GNSPR #70215 20170303 begin
                //ShareUtil shareUtil = new ShareUtil();
                //shareUtil.shareScrollViewDetail(mScrollView, NoteDetailActivity.this);
                showProgressDialog();
                TakeBitmapThread thread = new TakeBitmapThread(mScrollView,NoteDetailActivity.this);
                thread.start();
                //modified by luorw for GNSPR #70215 20170303 end
            }
        });
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gionee sunyang 2017-01-18 modify for GNSPR #65546 begin
                if (isNotifation) {
                    onBackPressed();
                } else {
                    NoteDetailActivity.this.finish();
                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                }
                // Gionee sunyang 2017-01-18 modify for GNSPR #65546 end
            }
        });
        mActionBar.setCustomView(view);
        mActionBar.show();

    }

    private static class TakeBitmapThread extends Thread{
        private WeakReference<NoteDetailActivity> mActivity;
        ScrollView mView;
        Bitmap bitmap = null;
        public TakeBitmapThread(ScrollView view,NoteDetailActivity activity) {
            this.mActivity = new WeakReference<NoteDetailActivity>(activity);
            mView = view;
            bitmap = ShareUtil.getBitmapByView(mView);
        }

        @Override
        public void run() {
            synchronized (NoteDetailActivity.class){
                final NoteDetailActivity activity = mActivity.get();
                if(activity != null){
                    SystemClock.sleep(500);//不让闪太快，显示进度过程
                    if (bitmap != null) {
                        String path = "/sdcard/Android/data/" + activity.getApplicationInfo().packageName + "/";
                        String fileName = System.currentTimeMillis() + ".png";
                        File mFile = new File(path, fileName);
                        File parentFile = mFile.getParentFile();
                        if (!parentFile.exists()) {
                            parentFile.mkdirs();
                        }
                        Bitmap compressBp = ShareUtil.compressImage(bitmap);
                        bitmap.recycle();
                        bitmap = null;
                        ShareUtil.savePic(compressBp, mFile, path);
                        activity.mHandler.sendMessage(activity.mHandler.obtainMessage(SHARE_SUCCESS,mFile));
                    }else {
                        activity.mHandler.sendMessage(activity.mHandler.obtainMessage(SHARE_FAIL));
                    }
                }
            }
        }
    }

    private void showProgressDialog() {
        pd = new AmigoProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        pd.setMessage("正在生成图片...");
        pd.show();
    }

    //added by luorw for GNSPR #71388 20170313 begin
    private List<Object> mDataList = null;
    private static final int ADD_CONTENT_LIST = 0;
    private static final int SHARE_SUCCESS = 1;
    private static final int SHARE_FAIL = 2;

    private static class GetDataThread extends Thread{
        private WeakReference<NoteDetailActivity> mActivity;
        private String content;
        public GetDataThread(NoteDetailActivity noteDetailActivity,String content){
            mActivity = new WeakReference<NoteDetailActivity>(noteDetailActivity);
            this.content = content;
        }

        @Override
        public void run() {
            final NoteDetailActivity activity = mActivity.get();
            if(activity != null){
                activity.mDataList = new ArrayList<Object>();
                activity.loader = ImageLoader.build();
                activity.mDataList.clear();
                activity.bitmapList.clear();
                activity.imageSpanList.clear();
                //定义正则表达式，用于匹配路径
                Pattern p = Pattern.compile(Constants.MATCH_PATTERN, Pattern.CASE_INSENSITIVE);
                Matcher m = p.matcher(content);
                int startIndex = 0;
                Bitmap bm = null;
                Bitmap rbm = null;
                Bitmap bitmap = null;
                while (m.find()) {
                    //取出路径前的文字
                    if (m.start() > 0) {
                        activity.mDataList.add(content.substring(startIndex, m.start()));
                    }
                    SpannableString ss = new SpannableString(m.group().toString());
                    //modified by luorw for GNSPR #101691 20170822 begin
                    // 取出路径
                    String path = m.group().toString();
                    String realPath = path.substring(0,path.length() - Constants.URI_END_TAG_LENGTH);
                    // 取出路径的后缀
                    String type = realPath.substring(realPath.length() - 3, realPath.length());
                    //modified by luorw for GNSPR #101691 20170822 end
                    //判断附件的类型，如果是录音文件，则从资源文件中加载图片
                    if (type.equals("amr")) {
                        bm = BitmapFactory.decodeResource(activity.getResources(), R.drawable.record_bg);
                        if (bm == null)
                            continue;
                        bm = activity.zoomImg(bm, DisplayUtils.getDisplayWidth(activity));
                        activity.bitmapList.add(bm);
                        //缩放图片
                        SoundImageSpan span = new SoundImageSpan(activity, bm, realPath);
                        ss.setSpan(span, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        System.out.println(m.start() + "-------" + m.end());
                        SoundsClickableSpan cs = new SoundsClickableSpan(activity, realPath);
                        ss.setSpan(cs, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        activity.mDataList.add(ss);
                        activity.imageSpanList.add(span);
                        startIndex = m.end();
                    } else {
                        if (realPath.equals(PATH_BILL_UNCHECKED_IMG)) {
                            rbm = activity.bm_bill_unchecked;
                            BillImageSpan span = new BillImageSpan(activity, rbm);
                            ss.setSpan(span, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            System.out.println(m.start() + "-------" + m.end());
                            activity.bitmapList.add(rbm);
                            activity.mDataList.add(ss);
                            activity.imageSpanList.add(span);
                            startIndex = m.end();
                        } else if (realPath.equals(PATH_BILL_CHECKED_IMG)) {
                            rbm = activity.bm_bill_checked;
                            BillImageSpan span = new BillImageSpan(activity, rbm);
                            ss.setSpan(span, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            System.out.println(m.start() + "-------" + m.end());
                            activity.bitmapList.add(rbm);
                            activity.mDataList.add(ss);
                            activity.imageSpanList.add(span);
                            startIndex = m.end();
                        } else {
                            String imagePathuri = m.group().substring(0,path.length() - Constants.URI_END_TAG_LENGTH);
                            LogUtils.i("loadData", "imagePath = " + imagePathuri);
                            //modified by luorw for  GNSPR #71389 begin
                            //Gionee zhengyt 2017-3-13 add for GNSRP #71049 BEGIN
                            bitmap = activity.loader.getBitmapFromMemCache(imagePathuri);
                            try {
                                if (bitmap == null) {
                                    if (imagePathuri.startsWith(Constants.FILE_SCHEME)) {
                                        String imagepath = imagePathuri.substring(Constants.FILE_SCHEME.length());
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inSampleSize = 1;
                                        options.inPreferredConfig = Bitmap.Config.RGB_565;
                                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(imagepath));
                                        bitmap = BitmapFactory.decodeStream(bis, null, options);
                                    }
                                    if (bitmap != null) {
                                        activity.loader.addBitmapToMemoryCache(imagePathuri, bitmap);
                                    }
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            //Gionee zhengyt 2017-3-13 add for GNSRP #71049 BND
                            //added by luorw for GNSPR #73315 2017-03-17 begin
                            if (bitmap == null) {
                                bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.image_unavailble);
                                PhotoImageSpan span = new PhotoImageSpan(activity, bitmap);
                                ss.setSpan(span, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                PictureClickableSpan pcs = new PictureClickableSpan(activity, imagePathuri);
                                ss.setSpan(pcs, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                System.out.println(m.start() + "-------" + m.end());
                                activity.bitmapList.add(bitmap);
                                activity.mDataList.add(ss);
                                activity.imageSpanList.add(span);
                            }
                            //added by luorw for GNSPR #73315 2017-03-17 end
                            else {
                                bm = activity.zoomImg(bitmap, DisplayUtils.getDisplayWidth(activity));
                                PhotoImageSpan span = new PhotoImageSpan(activity, bm);
                                //modified by luorw for  GNSPR #71389 end
                                ss.setSpan(span, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                PictureClickableSpan pcs = new PictureClickableSpan(activity, imagePathuri);
                                ss.setSpan(pcs, 0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                System.out.println(m.start() + "-------" + m.end());
                                activity.bitmapList.add(bm);
                                activity.mDataList.add(ss);
                                activity.imageSpanList.add(span);
                                /*modify by zhengjl at 2017-2-14 优化备忘图片加载 not begin*/
                            }
                            startIndex = m.end();
                        }
                    }
                }
                if (bitmap != null && bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
                if (bm != null && bm.isRecycled()) {
                    bm.recycle();
                    bm = null;
                }

                //将最后一个图片之后的文字添加在TextView中
                activity.mDataList.add(content.substring(startIndex, content.length()));
                activity.mHandler.sendMessage(activity.mHandler.obtainMessage(ADD_CONTENT_LIST));
            }
        }
    }

    private void getDataList(final String content) {
        etContent.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        new GetDataThread(this,content).start();
    }


    private void appendDateList() {
        if (mDataList == null || mDataList.size() == 0) {
            return;
        }
        for (int i = 0; i < mDataList.size(); i++) {
            Object date = mDataList.get(i);
            if (date instanceof SpannableString) {
                etContent.append((SpannableString) date);
            } else {
                etContent.append(date.toString());
            }
        }
        etContent.setHighlightColor(Color.TRANSPARENT);//消除点击时的背景色
        etContent.setMovementMethod(LinkMovementMethod.getInstance());
    }


    private static class MyHandler extends Handler {
        private final WeakReference<NoteDetailActivity> mActivity;

        public MyHandler(NoteDetailActivity activity) {
            mActivity = new WeakReference<NoteDetailActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            NoteDetailActivity noteDetailActivity = mActivity.get();
            if (noteDetailActivity == null) {
                return;
            }
            if (noteDetailActivity.pd != null && noteDetailActivity.pd.isShowing()) {
                if (noteDetailActivity != null && !noteDetailActivity.isDestroyed()) {
                    noteDetailActivity.pd.dismiss();
                }
            }
            switch (msg.what) {
                case ADD_CONTENT_LIST:
                    noteDetailActivity.appendDateList();
                    noteDetailActivity.voiceNoteBean.setContent(noteDetailActivity.etContent.getText().toString());
                    noteDetailActivity.mProgressBar.setVisibility(View.GONE);
                    noteDetailActivity.etContent.setVisibility(View.VISIBLE);
                    //added by luorw for #75707 2017-03-24 begin
                    noteDetailActivity.mImageViewShare.setVisibility(View.VISIBLE);
                    //added by luorw for #75707 2017-03-24 end
                    break;
                case SHARE_SUCCESS:
                    ComponentName componentName = noteDetailActivity.getComponentName();
                    if (componentName != null && componentName.getClassName().equals(ShareUtil.getLauncherTopApp(noteDetailActivity))) {
                        File file = (File)msg.obj;
                        ShareUtil.shareIntent(file,noteDetailActivity);
                    }
                    break;
                case SHARE_FAIL:
                    Toast.makeText(noteDetailActivity, "分享的内容太长，分享失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
    //added by luorw for GNSPR #71388 20170313 end

    private void loadData() {
        etContent.getText().clear();
        voiceNoteBean = new VoiceNoteBean();
        if (noteid > 0) {
            VoiceNoteBean noteBean = VoiceNoteDao.getInstance(this).getVoiceNote(noteid);
            /*
			modify by zhengjl at 2017-1-18
			显示时间
			 */
//			etDate.setText(noteBean.getTitle());
            Date creatDate = new Date(noteBean.getCreateTime());
            String creatTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(creatDate);
            if(noteBean.getCreateTime() == 0 && isResumeRefresh){
                etDate.setText("");
            }else{
                etDate.setText(creatTime);
            }
            createtime = noteBean.getCreateTime();
            LogUtils.e("zhengyt", "-----createTime=---2222-" + noteBean.getCreateTime());
            String content = noteBean.getContent();
            if (content != null) {
                getDataList(content);
            } else {
                etContent.setText("");
            }
            if (noteBean.getRemindDate() != 0) {
                mNoteRemindTimeLayout.setVisibility(View.VISIBLE);
                if(Build.VERSION.SDK_INT >= 24 && !"GIONEE W919".equalsIgnoreCase(Build.MODEL)){
                    //android 7.0及以上
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)mNoteRemindTimeLayout.getLayoutParams();
                    lp.bottomMargin = DensityUtils.dip2px(this,60);
                    mNoteRemindTimeLayout.setLayoutParams(lp);
                }
                Date date = new Date(noteBean.getRemindDate());
                String remindTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(date);
                mNoteRemindTimeTv.setText(remindTime);
            } else {
                mNoteRemindTimeLayout.setVisibility(View.GONE);
            }
            voiceNoteBean = noteBean;
        } else {
            createtime = System.currentTimeMillis();
            Date date = new Date(createtime);
            createTime = new SimpleDateFormat("yyyy.MM.dd HH:mm").format(date);
            etDate.setText(createTime);
            etContent.setText("");
        }

        if (Build.VERSION.SDK_INT >= 24 && !"GIONEE W919".equalsIgnoreCase(Build.MODEL) && mNoteRemindTimeLayout.getVisibility() == View.GONE) {
            //android 7.0 且不是w919项目,且时间提醒不可见
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mScrollView.getLayoutParams();
            int buttomMargin = DisplayUtils.dip2px(this,50);
            layoutParams.bottomMargin = buttomMargin;
            mScrollView.setLayoutParams(layoutParams);
        }
    }

    /**
     * added by luorw for 71389 20170311
     *
     * @param uri：图片的本地url地址
     * @return Bitmap
     */
    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            LogUtils.i("decodeUriAsBitmap", "e = " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    private void clickDelete() {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.delete));
        builder.setMessage(this.getString(R.string.delete_voice_note));
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.sec_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (noteid > 0) {
                    // 编辑模式
                    VoiceNoteBean bean = VoiceNoteDao.getInstance(NoteDetailActivity.this).getVoiceNote(noteid);
                    //added by luorw for GNSPR #70074 begin
                    RemindUtils.noteAlarmCancel(NoteDetailActivity.this, bean);
                    //added by luorw for GNSPR #70074 end
                    List<VoiceNoteBean> beanList = new ArrayList<VoiceNoteBean>();
                    beanList.add(bean);
                    VoiceNoteDao.getInstance(NoteDetailActivity.this).deleteVoiceNotes(beanList);
                }

                dialog.dismiss();
                NoteDetailActivity.this.finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            }
        });
        builder.show();
    }

    private void clickEdit() {
        Intent intent = new Intent();
        intent.setClass(this, AddVoiceNoteActivity.class);
        intent.putExtra("noteid", noteid);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        //modify by zhengjl at 2017-2-22 for 详情也显示位置错乱
        //Gionee zhengyt 2017-3-10 modify for GNSPR#71108 Begin
        //finish();
        //Gionee zhengyt 2017-3-10 modify for GNSPR#71108 Begin
        startActivity(intent);
        //modify by zhengjl at 2017-2-22 for 详情也显示位置错乱
    }


    // 等比例缩放图片
    private Bitmap resize(Bitmap bitmap, int S) {
        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();
        double partion = imgWidth * 1.0 / imgHeight;
        double sqrtLength = Math.sqrt(partion * partion + 1);
        // 新的缩略图大小
        double newImgW = S * (partion / sqrtLength);
        double newImgH = S * (1 / sqrtLength);
        float scaleW = (float) (newImgW / imgWidth);
        float scaleH = (float) (newImgH / imgHeight);

        Matrix mx = new Matrix();
        // 对原图片进行缩放
        mx.postScale(scaleW, scaleH);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx, true);
        return bitmap;
    }

    // 缩放图片
    private Bitmap zoomImg(Bitmap bm, int newWidth) {
        // 获得图片的宽高
        if (bm == null) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.amigo);
            return bitmap;
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        int newHeight = height * newWidth / width;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    // Gionee sunyang 2017-01-18 modify for GNSPR #65546 begin
    private boolean isNotifation;

    private void isFromNotification() {
        isNotifation = getIntent().getBooleanExtra(Constants.NOTIFACATION_FLAG, false);
        if (isNotifation) {
            YouJuAgent.onEvent(this, this.getResources().getString(R.string.click_notification));
        }
    }

    @Override
    public void onBackPressed() {
        if (isNotifation) {
            super.onBackPressed();
        } else {
            //Gionee zhengyt 2017-3-10 modify for GNSPR#71108 Begin
            //super.onBackPressed();
            NoteDetailActivity.this.finish();
            //Gionee zhengyt 2017-3-10 modify for GNSPR#71108 Begin
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
    }

    private void setStartCalendarActivity() {
        Intent intent = new Intent();
        intent.setClass(this, CalendarActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
    // Gionee sunyang 2017-01-18 modify for GNSPR #65546 end


}
