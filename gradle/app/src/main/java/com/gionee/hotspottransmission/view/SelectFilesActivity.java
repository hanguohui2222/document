package com.gionee.hotspottransmission.view;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.adapter.ChooseFilePagerAdapter;
import com.gionee.hotspottransmission.adapter.SelectedListAdapter;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileMultiSendData;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.callback.IClearFileSelectedCallBack;
import com.gionee.hotspottransmission.callback.IRefreshFileSelectedCallBack;
import com.gionee.hotspottransmission.callback.ISelectFiles;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.manager.SelectFilesManager;
import com.gionee.hotspottransmission.manager.ThreadPoolManager;
import com.gionee.hotspottransmission.utils.LogUtil;
import com.gionee.hotspottransmission.view.fragment.AppsFragment;
import com.gionee.hotspottransmission.view.fragment.FilesFragment;
import com.gionee.hotspottransmission.view.fragment.ImagesDirFragment;
import com.gionee.hotspottransmission.view.fragment.MusicFragment;
import com.gionee.hotspottransmission.view.fragment.VideoFragment;

import java.util.ArrayList;
import java.util.List;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;
import amigoui.app.AmigoProgressDialog;

public class SelectFilesActivity extends AmigoActivity implements ISelectFiles, View.OnClickListener, AdapterView.OnItemClickListener {
    private ViewPager mViewPager;
    private ChooseFilePagerAdapter mViewPagerAdapter;
    private ArrayList<Fragment> mFragmentPages;
    private Context mContext;
    private SelectFilesManager mSelectFilesManager;
    private ListView mlvSelected;
    private LinearLayout mAllFilesLayout;
    private LinearLayout mSelectedFilesLayout;
    private SelectedListAdapter mSelectedListAdapter;
    private AmigoActionBar mActionBar;
    private int mCurrentTab;
    private Menu mMenu;
    private List<FileInfo> mSelectedList;
    private int mCurrentMode = ALL_FILES_MODE;
    private static final int ALL_FILES_MODE = 0;
    private static final int SELETED_FILES_MODE = 1;
    private AmigoProgressDialog pd;
    private static final int SENDER_IS_WIFI_ENABLE = 1;
    private static final int SENDER_START_SEND = 2;
    public FilesFragment mFilesFragment;
    public AppsFragment mAppsFragment;
    public ImagesDirFragment mImagesDirFragment;
    public MusicFragment mMusicFragment;
    public VideoFragment mVideoFragment;
    private TextView mTvSelectAll;
    private boolean isSelectAll;
    private boolean isReSend;
    private boolean isGroupOwner;
    private boolean isMultiSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.i("SelectFilesActivity----onCreate");
        setContentView(R.layout.activity_select_files);
        parseIntent();
        clearFileSendData();
        initView();
        initTabActionBar();
        initTabs();
    }

    private void clearFileSendData(){
        if(isMultiSend){
            FileMultiSendData.getInstance().clearAllMultiSendData();
        }
        FileSendData.getInstance().clearAllFiles();
    }

    private void initSelectedListActionBar() {
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setNavigationMode(amigoui.app.AmigoActionBar.DISPLAY_HOME_AS_UP);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setTitle(R.string.selected_files);
        mTvSelectAll.setVisibility(View.GONE);
        mActionBar.show();
        mActionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentMode = ALL_FILES_MODE;
                initTabActionBar();
                //20160720 add by weiqun
                mlvSelected.setAdapter(null);
                mSelectedListAdapter = null;
                //20160720 add by weiqun
                mSelectedFilesLayout.setVisibility(View.GONE);
                mAllFilesLayout.setVisibility(View.VISIBLE);
                refreshMenu(FileSendData.getInstance().getFileSendList());
            }
        });
    }

    private void initTabActionBar() {
        mActionBar = getAmigoActionBar();
        mActionBar.setTitle(getResources().getString(R.string.choose_file));
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setNavigationMode(AmigoActionBar.NAVIGATION_MODE_TABS);
        View actionbar = getLayoutInflater().inflate(
                R.layout.actionbar_select_files, null);
        mTvSelectAll = (TextView) actionbar.findViewById(R.id.select_all);
        mTvSelectAll.setVisibility(View.VISIBLE);
        mTvSelectAll.setOnClickListener(this);
        updateSelectAllForFragment();
        AmigoActionBar.LayoutParams param = new AmigoActionBar.LayoutParams(
                AmigoActionBar.LayoutParams.WRAP_CONTENT,
                AmigoActionBar.LayoutParams.MATCH_PARENT, Gravity.RIGHT);
        mActionBar.setCustomView(actionbar, param);
        mActionBar.setNavigationMode(AmigoActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setOnBackClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FileSendData.getInstance().clearAllFiles();
                finish();
            }
        });
        mActionBar.show();
    }

    private void initTabs() {
        if (mActionBar.getTabCount() == 0) {
            String[] tabTitleArray = getResources().getStringArray(R.array.select_files_tab);
            if (tabTitleArray.length > 0) {
                for (int i = 0; i < mViewPagerAdapter.getCount(); i++) {
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

    private void parseIntent() {
        Intent intent = getIntent();
        isGroupOwner = intent.getBooleanExtra(Constants.IS_GROUP_OWNER, false);
        if (Constants.ACTION_RESEND_FILES.equals(intent.getAction())) {
            isReSend = true;
        }else if(Constants.ACTION_MULTI_SEND_FILES.equals(intent.getAction())){
            isMultiSend = true;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initView() {
        mContext = this;
        mSelectFilesManager = new SelectFilesManager();
        mViewPager = (ViewPager) findViewById(R.id.choose_file_viewpager);
        mlvSelected = (ListView) findViewById(R.id.selected_files_listview);
        mAllFilesLayout = (LinearLayout) findViewById(R.id.all_files_layout);
        mSelectedFilesLayout = (LinearLayout) findViewById(R.id.selected_files_layout);
        addFragments();
        FragmentManager manager = getFragmentManager();
        mViewPagerAdapter = new ChooseFilePagerAdapter(getApplicationContext(), manager, mFragmentPages);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setOnPageChangeListener(mPageChangeListener);
        mViewPager.setOnTouchListener(mTouchListener);
        mViewPager.setCurrentItem(0);
    }

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            LogUtil.i(" --- onPageSelected --- position = " + position + " ActionBar = " + mActionBar + "mCurrentTab =" + mCurrentMode);
            mActionBar.setSelectedNavigationItem(position);
            mCurrentTab = position;
            updateSelectAllForFragment();
        }

        @Override
        public void onPageScrolled(int position, float offset, int offsetPixels) {
            mActionBar.onPageScrolled(position, offset, offsetPixels);
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * 按照不同的fragment更新全选的可用状态，以及是否为可选或取消可选
     */
    private void updateSelectAllForFragment() {
        switch (mCurrentTab) {
            case 0:
                setSelectAllEnable(false);
                break;
            case 1:
                setSelectAllEnable(true);
                updateSelectAllText(mAppsFragment.isSelectAll());
                break;
            case 2:
                if (mImagesDirFragment.getmImageMode() == Constants.IMAGE_FIRST_DIR) {
                    setSelectAllEnable(false);
                } else {
                    setSelectAllEnable(true);
                    updateSelectAllText(mImagesDirFragment.isSelectAll());
                }
                break;
            case 3:
                setSelectAllEnable(true);
                updateSelectAllText(mMusicFragment.isSelectAll());
                break;
            case 4:
                setSelectAllEnable(true);
                updateSelectAllText(mVideoFragment.isSelectAll());
                break;
        }
    }

    private void updateSelectAllText(boolean isSelectAll) {
        if (isSelectAll) {
            mTvSelectAll.setText(getResources().getString(R.string.cancel_select_all_files));
        } else {
            mTvSelectAll.setText(getResources().getString(R.string.select_all_files));
        }
    }


    private void setSelectAllEnable(boolean flag) {
        if (flag && mCurrentMode == ALL_FILES_MODE) {
            mTvSelectAll.setEnabled(true);
            mTvSelectAll.setVisibility(View.VISIBLE);
            mTvSelectAll.setTextColor(getResources().getColor(R.color.actionbar_title_color));
        } else {
            mTvSelectAll.setEnabled(false);
            mTvSelectAll.setVisibility(View.GONE);
        }
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
    @Override
    public void onBackPressed() {
        IBackPressedListener backPressedListener = (IBackPressedListener) mViewPagerAdapter.getItem(mViewPager.getCurrentItem());
        if (!backPressedListener.onBack()) {
            if (mSelectedFilesLayout.getVisibility() == View.VISIBLE) {
                mCurrentMode = ALL_FILES_MODE;
                initTabActionBar();
                //20160720 add by weiqun
                mlvSelected.setAdapter(null);
                mSelectedListAdapter = null;
                //20160720 add by weiqun
                mSelectedFilesLayout.setVisibility(View.GONE);
                mAllFilesLayout.setVisibility(View.VISIBLE);
                refreshMenu(FileSendData.getInstance().getFileSendList());
            } else {
                FileSendData.getInstance().clearAllFiles();
                super.onBackPressed();
            }
        } else {
            if (mSelectedFilesLayout.getVisibility() == View.VISIBLE) {
                mCurrentMode = ALL_FILES_MODE;
                initTabActionBar();
                //20160720 add by weiqun
                mlvSelected.setAdapter(null);
                mSelectedListAdapter = null;
                //20160720 add by weiqun
                mSelectedFilesLayout.setVisibility(View.GONE);
                mAllFilesLayout.setVisibility(View.VISIBLE);
                refreshMenu(FileSendData.getInstance().getFileSendList());
            }
        }
    }

    @Override
    public void showSelectedList() {
        mSelectFilesManager.showFileSendList(new IRefreshFileSelectedCallBack() {

            @Override
            public void onRefreshCount(List<FileInfo> list) {

            }

            @Override
            public void onRefreshSelected(boolean isSelected) {

            }

            @Override
            public void onShowSelected(List<FileInfo> list) {
                mSelectedListAdapter = new SelectedListAdapter(mContext, SelectFilesActivity.this, imageHandler);
                mlvSelected.setAdapter(mSelectedListAdapter);
                mAllFilesLayout.setVisibility(View.GONE);
                mSelectedFilesLayout.setVisibility(View.VISIBLE);
                mCurrentMode = SELETED_FILES_MODE;
                initSelectedListActionBar();
                refreshMenu(list);
            }

        });
    }

    Handler imageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.SET_IMAGE_BITMAP:
                    ImageView imageView = (ImageView) mlvSelected.findViewWithTag(msg.arg1);
                    LogUtil.i("msg.arg1" + msg.arg1);
                    LogUtil.i("imageHandler findViewWithTag " + imageView);
                    LogUtil.i("-------------");
                    if (imageView != null) {
                        imageView.setImageBitmap((Bitmap) msg.obj);
                    }
                    break;
            }
        }
    };

    @Override
    public void send() {
        if(isMultiSend){
            multiSend();
        }else{
            oneByOneSend();
        }
    }


    private void multiSend(){
        FileSendData.getInstance().calculateAllFileSize();
        ThreadPoolManager.getInstance().executeRunnable(new Runnable() {
            @Override
            public void run() {
                FileSendData.getInstance().setAllFileIcon(mContext);
                LogUtil.i("startSend,ps = " + pd);
                handler.sendEmptyMessage(SENDER_START_SEND);
            }
        });
        if (pd == null) {
            pd = new AmigoProgressDialog(SelectFilesActivity.this);
        }
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage(getResources().getString(R.string.file_is_ready_transfer));
        pd.setCancelable(false);
        pd.show();
    }

    private void oneByOneSend(){
        FileSendData.getInstance().calculateAllFileSize();
        if (isReSend && FileSendData.getInstance().isConnected()) {
            ThreadPoolManager.getInstance().executeRunnable(new Runnable() {
                @Override
                public void run() {
                    FileSendData.getInstance().setAllFileIcon(mContext);
                    LogUtil.i("startSend,ps = " + pd);
                    handler.sendEmptyMessage(SENDER_START_SEND);
                }
            });
            if (pd == null) {
                pd = new AmigoProgressDialog(SelectFilesActivity.this);
            }
            pd.setCanceledOnTouchOutside(false);
            pd.setMessage(getResources().getString(R.string.file_is_ready_transfer));
            pd.setCancelable(false);
            pd.show();
        } else if (isGroupOwner) {
            ThreadPoolManager.getInstance().executeRunnable(new Runnable() {
                @Override
                public void run() {
                    FileSendData.getInstance().setAllFileIcon(mContext);
                }
            });
            Intent intent = new Intent(this, SenderActivity.class);
            startActivity(intent);
            finish();
        } else {
            ThreadPoolManager.getInstance().executeRunnable(new Runnable() {
                @Override
                public void run() {
                    FileSendData.getInstance().setAllFileIcon(mContext);
                }
            });
            Intent intent = new Intent();
            intent.setAction(Constants.ACTION_GC_SEND_FILES);
            intent.setClass(SelectFilesActivity.this, GcTransferActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void enterTransfer() {
        Intent intent = new Intent();
        if(isMultiSend){
            if(isGroupOwner){
                intent.setClass(SelectFilesActivity.this, GoMultiTransferActivity.class);
            }else{
                intent.setClass(SelectFilesActivity.this, GcMultiTransferActivity.class);
            }
        }else{
            if (isGroupOwner) {
                intent.setClass(SelectFilesActivity.this, GoTransferActivity.class);
            } else {
                intent.setClass(SelectFilesActivity.this, GcTransferActivity.class);
            }
            intent.setAction(Constants.ACTION_RESEND_FILES);
        }
        startActivity(intent);
        finish();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SENDER_START_SEND:
                    LogUtil.i("SENDER_START_SEND------------");
                    if (pd != null) {
                        pd.dismiss();
                    }
                    enterTransfer();
                    break;
            }
        }
    };


    @Override
    public void clearSelectedList() {
        mSelectFilesManager.clearFileTransferList(new IClearFileSelectedCallBack() {
            @Override
            public void clearSelectedFiles() {
                //added by luorw for GNSPR #42800 test begin
                if (mSelectedListAdapter == null) {
                    return;
                }
                mSelectedListAdapter.getmFileList().clear();
                //added by luorw for GNSPR #42800 test end
                mFilesFragment.refreshAllAdapter();
                if (mAppsFragment.getmFileListadapter() != null) {
                    mAppsFragment.getmFileListadapter().notifyDataSetChanged();
                    mAppsFragment.setIsSelectAll(false);
                }
                if (mImagesDirFragment.getmMediaListAdapter() != null) {
                    mImagesDirFragment.getmMediaListAdapter().notifyDataSetChanged();
                    mImagesDirFragment.setIsSelectAll(false);
                }
                if (mMusicFragment.getmAdapter() != null) {
                    mMusicFragment.getmAdapter().notifyDataSetChanged();
                    mMusicFragment.setSelectAll(false);
                }
                if (mVideoFragment.getmAdapter() != null) {
                    mVideoFragment.getmAdapter().notifyDataSetChanged();
                    mVideoFragment.setSelectAll(false);
                }
            }

            @Override
            public void refreshUI(List<FileInfo> list) {
                mSelectedListAdapter.notifyDataSetChanged();
                LogUtil.i("refreshUI  清除已选");
                //20160720 add by weiqun
                mlvSelected.setAdapter(null);
                mSelectedListAdapter = null;
                //20160720 add by weiqun
                mSelectedFilesLayout.setVisibility(View.GONE);
                mAllFilesLayout.setVisibility(View.VISIBLE);
                mCurrentMode = ALL_FILES_MODE;
                initTabActionBar();
                refreshMenu(list);
            }

            @Override
            public void refreshSelectAllUI() {
                refreshSelectAllText();
            }

        });
    }

    @Override
    public void clearSelectedItem(final FileInfo info) {
        if(info.getFileType() == Constants.TYPE_IMAGE){
            mSelectFilesManager.clearImgTransferItem(info,info.getFileDir(),new IClearFileSelectedCallBack(){

                @Override
                public void clearSelectedFiles() {
                    //added by luorw for GNSPR #42800 test begin
                    mSelectedListAdapter.getmFileList().remove(info);
                    //added by luorw for GNSPR #42800 test end
                    mSelectedListAdapter.notifyDataSetChanged();
                    if (mImagesDirFragment.getmMediaListAdapter() != null) {
                        mImagesDirFragment.getmMediaListAdapter().notifyDataSetChanged();
                    }
                }

                @Override
                public void refreshUI(List<FileInfo> list) {
                    refreshMenu(list);
                }

                @Override
                public void refreshSelectAllUI() {
                    String key = info.getFileDir();
                    List<FileInfo> list = FileSendData.getInstance().mImageDatas.get(key);
                    if (mImagesDirFragment.mImageList.size() != list.size()) {
                        mImagesDirFragment.setIsSelectAll(false);
                    }
                    refreshSelectAllText();
                }
            });
        }
        mSelectFilesManager.clearFileTransferItem(info, new IClearFileSelectedCallBack() {
            @Override
            public void clearSelectedFiles() {
                //added by luorw for GNSPR #42800 test begin
                mSelectedListAdapter.getmFileList().remove(info);
                //added by luorw for GNSPR #42800 test end
                mSelectedListAdapter.notifyDataSetChanged();
                switch (info.getFileType()) {
                    case Constants.TYPE_FILE:
                        mFilesFragment.refreshSelectedItemAdapter(info);
                        break;
                    case Constants.TYPE_APPS:
                        if (info.getLoaderId() == Constants.TYPE_APK) {
                            mFilesFragment.refreshSelectedItemAdapter(info);
                        } else {
                            if (mAppsFragment.getmFileListadapter() != null) {
                                mAppsFragment.getmFileListadapter().notifyDataSetChanged();
                            }
                        }
                        break;
                    case Constants.TYPE_MUSIC:
                        if (mMusicFragment.getmAdapter() != null) {
                            mMusicFragment.getmAdapter().notifyDataSetChanged();
                        }
                        break;
                    case Constants.TYPE_VIDEO:
                        if (mVideoFragment.getmAdapter() != null) {
                            mVideoFragment.getmAdapter().notifyDataSetChanged();
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void refreshUI(List<FileInfo> list) {
                refreshMenu(list);
            }

            @Override
            public void refreshSelectAllUI() {
                switch (info.getFileType()) {
                    case Constants.TYPE_APPS:
                        if (info.getLoaderId() != Constants.TYPE_APK) {
                            if (mAppsFragment.mListAppInfo.size() != FileSendData.getInstance().mAppDatas.size()) {
                                mAppsFragment.setIsSelectAll(false);
                            }
                        }
                        break;
                    case Constants.TYPE_MUSIC:
                        if (mMusicFragment.mMusicList.size() != FileSendData.getInstance().mMusicDatas.size()) {
                            mMusicFragment.setSelectAll(false);
                        }
                        break;
                    case Constants.TYPE_VIDEO:
                        if (mVideoFragment.mVideoList.size() != FileSendData.getInstance().mVideoDatas.size()) {
                            mVideoFragment.setSelectAll(false);
                        }
                        break;
                    default:
                        break;
                }
                refreshSelectAllText();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_file_remove:
                clearSelectedItem((FileInfo) v.getTag());
                break;
            case R.id.select_all:
                selectAll();
                break;
            default:
                break;
        }
    }

    private void selectAll() {
        if (mTvSelectAll.getText().equals(getResources().getString(R.string.select_all_files))) {
            isSelectAll = false;
        } else {
            isSelectAll = true;
        }
        isSelectAll = !isSelectAll;
        switch (mCurrentTab) {
            case 1:
                mAppsFragment.selectedAll(isSelectAll);
                break;
            case 2:
                mImagesDirFragment.selectedAll(isSelectAll);
                break;
            case 3:
                mMusicFragment.selectedAll(isSelectAll);
                break;
            case 4:
                mVideoFragment.selectedAll(isSelectAll);
                break;
            default:
                break;
        }
        if (isSelectAll) {
            mTvSelectAll.setText(getResources().getString(R.string.cancel_select_all_files));
        } else {
            mTvSelectAll.setText(getResources().getString(R.string.select_all_files));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public interface IBackPressedListener {
        /**
         * 处理back事件。
         *
         * @return True: 表示已经处理; False: 没有处理，让基类处理。
         */
        boolean onBack();
    }

    private void addFragments() {
        mFragmentPages = new ArrayList<Fragment>();
        mFilesFragment = FilesFragment.newInstance();
        mAppsFragment = AppsFragment.newInstance();
        mImagesDirFragment = ImagesDirFragment.newInstance();
        mMusicFragment = MusicFragment.newInstance();
        mVideoFragment = VideoFragment.newInstance();
        mFragmentPages.add(mFilesFragment);
        mFragmentPages.add(mAppsFragment);
        mFragmentPages.add(mImagesDirFragment);
        mFragmentPages.add(mMusicFragment);
        mFragmentPages.add(mVideoFragment);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.select_files_menu, menu);
        MenuItem menuSelected = menu.findItem(R.id.selected_files);
        MenuItem menuSend = menu.findItem(R.id.send_files);
        MenuItem menuClear = menu.findItem(R.id.clear_selected_files);

        if (mCurrentMode == ALL_FILES_MODE) {
            menuSend.setVisible(true);
            menuSelected.setVisible(true);
            menuClear.setVisible(false);
            if (null != mSelectedList && mSelectedList.size() != 0) {
                menuSend.setEnabled(true);
                menuSelected.setEnabled(true);
                menuSelected.setTitle(getResources().getString(R.string.selected_files) + "(" + mSelectedList.size() + ")");
            } else {
                menuSend.setEnabled(false);
                menuSelected.setEnabled(false);
                menuSelected.setTitle(getResources().getString(R.string.selected_files));
            }
        } else if (mCurrentMode == SELETED_FILES_MODE) {
            menuSend.setVisible(false);
            menuSelected.setVisible(false);
            menuClear.setVisible(true);
            if (null != mSelectedList && mSelectedList.size() != 0) {
                menuClear.setEnabled(true);
            } else {
                menuClear.setEnabled(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.selected_files:
                showSelectedList();
                break;
            case R.id.send_files:
                send();
                break;
            case R.id.clear_selected_files:
                clearSelectedList();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void refreshMenu(List<FileInfo> list) {
        mSelectedList = list;
        if (mMenu != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onPrepareOptionsMenu(mMenu);
                }
            }, 100);
        }
    }

    @Override
    public void refreshSelectAllText() {
        updateSelectAllForFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

