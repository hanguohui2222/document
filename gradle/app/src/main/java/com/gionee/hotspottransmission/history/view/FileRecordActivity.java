package com.gionee.hotspottransmission.history.view;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.history.adapter.RecordFilePagerAdapter;
import com.gionee.hotspottransmission.history.bean.DeviceInfo;
import com.gionee.hotspottransmission.history.bean.HistoryFileInfo;
import com.gionee.hotspottransmission.history.bean.HistoryInfo;
import com.gionee.hotspottransmission.history.fragment.DeviceFragment;
import com.gionee.hotspottransmission.history.fragment.ReceiveFileFragment;
import com.gionee.hotspottransmission.history.fragment.RecordFragment;
import com.gionee.hotspottransmission.history.presenter.FileRecordPresenter;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;

/**
 * Created by rongdd on 16-4-27.
 */
public class FileRecordActivity extends AmigoActivity implements IFileRecordView {
    private ViewPager mViewPager;
    private RecordFilePagerAdapter mViewPagerAdapter;
    private ArrayList<Fragment> mFragmentPages;
    private FileRecordPresenter mPresenter;
    private static final int CLEAR_DEVICE = 0;
    private static final int CLEAR_HISTORY = 2;
    private static final int REQUEAT_DATA = 3;
    private AmigoActionBar mActionBar;
    private int mCurrentTab;
    private Menu mMenu;
    private List<DeviceInfo> mDeviceInfos;
    private List<HistoryInfo> mHistoryList;
    private DeviceFragment mDeviceFragment;
    private RecordFragment mHistoryFragment;

    private DeviceInfoListener deviceInfoListener;
    public interface DeviceInfoListener{
        void setDevice(List<DeviceInfo> deviceInfos);
    }
    public void setDeviceInfoListener(com.gionee.hotspottransmission.history.view.FileRecordActivity.DeviceInfoListener deviceInfoListener) {
        this.deviceInfoListener = deviceInfoListener;
    }

    private HistoryListener historyListener;
    public interface HistoryListener{
        void setHistoryRecord(List<HistoryFileInfo> historyFileInfoList);
    }
    public void setHistoryListener(com.gionee.hotspottransmission.history.view.FileRecordActivity.HistoryListener historyListener) {
        this.historyListener = historyListener;
    }

    private FileReceiveListener fileReceiveListener;
    public interface FileReceiveListener{
        void setFileCount(List<Map<String, List<FileInfo>>> list);
    }
    public void setFileReceiveListener(com.gionee.hotspottransmission.history.view.FileRecordActivity.FileReceiveListener fileReceiveListener) {
        this.fileReceiveListener = fileReceiveListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i("设备bug  FileRecordActivity onCreate");
        setContentView(R.layout.activity_record_file);
        initViews();
        mPresenter = new FileRecordPresenter(this,this);
        initTabActionBar();
        initFragment();

        registImageObserver();
    }

    ContentObserver mImageObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            mPresenter.initData(handler);
        }
    };

    public void registImageObserver(){
        Uri uri = Images.Media.getContentUri("external");
        getContentResolver().registerContentObserver(uri, false, mImageObserver);
    }

    private void unregisterObserver(ContentObserver contentObserver) {
        getContentResolver().unregisterContentObserver(contentObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterObserver(mImageObserver);
    }

    private void initTabActionBar() {
        mActionBar = getAmigoActionBar();
        mActionBar.setTitle(getResources().getString(R.string.history_record_title));
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setNavigationMode(AmigoActionBar.NAVIGATION_MODE_TABS);
        mActionBar.show();
    }

    private void initTabs() {
        if(mActionBar.getTabCount() == 0){
            String[] tabTitleArray = getResources().getStringArray(R.array.records_tab);
            if (tabTitleArray.length > 0) {
                for (int i = 0; i < tabTitleArray.length; i++) {
                    AmigoActionBar.Tab tab = mActionBar.newTab();
                    tab.setText(tabTitleArray[i]);
                    tab.setTabListener(mTabListener);
                    mActionBar.addTab(tab);
                }
            }
        }
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mActionBar.onScrollToEnd(v, event);
            return false;
        }
    };

    private AmigoActionBar.TabListener mTabListener = new AmigoActionBar.TabListener() {

        @Override
        public void onTabUnselected(AmigoActionBar.Tab arg0, FragmentTransaction arg1) {
        }

        @TargetApi(Build.VERSION_CODES.DONUT)
        @Override
        public void onTabSelected(AmigoActionBar.Tab tab, FragmentTransaction arg1) {
            int currentTabPosition = tab.getPosition();
            mViewPager.setCurrentItem(currentTabPosition);
        }

        @Override
        public void onTabReselected(AmigoActionBar.Tab arg0, FragmentTransaction arg1) {

        }
    };

    private void initViews() {
        mViewPager = (ViewPager) findViewById(R.id.record_file_viewpager);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private static final int RESPONSE_DEVICE = 11;
    private static final int RESPONSE_HISTORY_RECORD = 12;
    private static final int RESPONSE_FILE = 13;

    public Handler handler = new Handler(){
      public void handleMessage(Message msg){
          switch (msg.what){
              case RESPONSE_DEVICE:
                  List<DeviceInfo> deviceInfos = (List<DeviceInfo>)msg.obj;
                  LogUtil.i("设备 activity handler");
                  if(deviceInfoListener != null){
                      LogUtil.i("设备 activity handler deviceInfoListener != null");
                      deviceInfoListener.setDevice(deviceInfos);
                      mDeviceInfos = deviceInfos;
                  }
                break;
              case RESPONSE_HISTORY_RECORD:
                  List<HistoryFileInfo> historyInfos = (List<HistoryFileInfo>)msg.obj;
                  LogUtil.i("全部历史记录 activity handler");
                  if(historyListener != null) {
                      LogUtil.i("activity handler historyListener != null");
                      historyListener.setHistoryRecord(historyInfos);
                  }
                  break;
              case RESPONSE_FILE:
                  List<Map<String,List<FileInfo>>> list = (List<Map<String,List<FileInfo>>>)msg.obj;
                  LogUtil.i("收到的文件 activity handler");
                  if (fileReceiveListener != null) {
                      LogUtil.i("activity handler fileReceiveListener != null");
                      fileReceiveListener.setFileCount(list);
                  }
                  break;
              case REQUEAT_DATA:
                  mPresenter.initData(handler);
                  break;
          }
      }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void initFragment() {
        LogUtil.i("设备bug  FileRecordActivity initFragment");
        mFragmentPages = new ArrayList<Fragment>();
        mDeviceFragment = new DeviceFragment();
        mHistoryFragment = new RecordFragment();
        mFragmentPages.add(mDeviceFragment);
        mFragmentPages.add(new ReceiveFileFragment());
        mFragmentPages.add(mHistoryFragment);

        FragmentManager manager = getFragmentManager();
        mViewPagerAdapter = new RecordFilePagerAdapter(this,manager,mFragmentPages);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setOnPageChangeListener(mPageChangeListener);
        mViewPager.setOnTouchListener(mTouchListener);
        mViewPager.setCurrentItem(CLEAR_DEVICE);
        initTabs();

        handler.sendEmptyMessageDelayed(REQUEAT_DATA, 500);
    }

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
            mCurrentTab = position;
        }

        @Override
        public void onPageScrolled(int position, float offset, int offsetPixels) {
            mActionBar.onPageScrolled(position, offset, offsetPixels);
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    @Override
    public void clearAll() {
        if(mCurrentTab == CLEAR_DEVICE){
            mPresenter.clearDevice();
            //刷新列表
            mDeviceFragment.refreshListView();
            //modified by luorw for 53416 20161108 begin
        }else if(mCurrentTab == CLEAR_HISTORY && mHistoryFragment != null){
            mPresenter.clearHistory();
            //刷新列表
            mHistoryFragment.refreshListView();
        }
            //modified by luorw for 53416 20161108 end
    }

    @Override
    public void deleteSucc() {
//        Toast.makeText(this,"删除成功!",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deleteFaile() {
//        Toast.makeText(this,"删除失败!",Toast.LENGTH_SHORT).show();
    }

}
