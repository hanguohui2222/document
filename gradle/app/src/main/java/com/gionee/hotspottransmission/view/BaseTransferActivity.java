package com.gionee.hotspottransmission.view;

import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.adapter.TransferPagerAdapter;
import com.gionee.hotspottransmission.bean.SocketChannel;
import com.gionee.hotspottransmission.service.BaseService;
import com.gionee.hotspottransmission.service.MultiBaseService;
import com.gionee.hotspottransmission.utils.LogUtil;

import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;
import amigoui.app.AmigoAlertDialog;

public abstract class BaseTransferActivity extends AmigoActivity{

    protected AmigoActionBar mActionBar;
    protected ViewPager mViewPager;
    protected TransferPagerAdapter mViewPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_transfer);
        initActionBar();
        addFragments();
        initTabs();
    }

    public void initActionBar(){
        mActionBar = getAmigoActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(true);
        mActionBar.setNavigationMode(AmigoActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setTitle(R.string.transfer_action_title);
        mActionBar.show();
        mActionBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeExitDialog();
            }
        });
    }
    public abstract void addFragments();

    public abstract void refreshFragmentMenu(int position);

    public void initTabs() {
        if (mActionBar.getTabCount() == 0) {
            String[] tabTitleArray = getResources().getStringArray(R.array.transfer_tab);
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

    protected ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
            refreshFragmentMenu(position);
        }

        @Override
        public void onPageScrolled(int position, float offset, int offsetPixels) {
            mActionBar.onPageScrolled(position, offset, offsetPixels);
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    protected View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mActionBar.onScrollToEnd(v, event);
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        makeExitDialog();
    }

    /**
     * 退出提示
     */
    public void makeExitDialog() {
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this, R.style.AmigoDialogTheme);
        builder.setMessage(this.getResources().getString(R.string.exit_transfer));
        builder.setNegativeButton(getResources().getString(R.string.cancel), null);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LogUtil.i("确认退出传输");
                exit(true);
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    public void  exit(boolean isFinishActivity){

    }

    public BaseService getService(){
        return null;
    }

    public MultiBaseService getMultiService(){
        return null;
    }

    public void removeAssociation(String key){
        SocketChannel.getInstance().removeAddress(key);
        SocketChannel.getInstance().removeName(key);
        SocketChannel.getInstance().removeCommendSocket(key);
        getMultiService().mCommandRunnables.remove(key);
//        getMultiService().unRegisterSendListener(key);
//        getMultiService().unRegisterReceiveListener(key);
    }
}
