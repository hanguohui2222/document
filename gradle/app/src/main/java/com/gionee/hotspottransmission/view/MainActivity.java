package com.gionee.hotspottransmission.view;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.ViewCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.history.view.FileRecordActivity;
import com.gionee.hotspottransmission.manager.HotspotManager;
import java.util.List;
import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;

/**
 * Created by luorw on 17/5/9.
 */
public class MainActivity extends AmigoActivity implements View.OnClickListener {

    private ImageView iv_setting;
    private ImageView iv_history;
    private RelativeLayout rl_group_transfer;
    private RelativeLayout rl_invite_friend;
    private long exitTime = 0;
    private static final int REQUEST_CODE_ASK_WRITE_SETTINGS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor();
        setContentView(R.layout.activity_main);
        initActionBar();
        initViews();
        setListener();
        getWriteSettingPermission();
    }

    private void getWriteSettingPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, REQUEST_CODE_ASK_WRITE_SETTINGS);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new HotspotManager().closeHotspot(this);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void setStatusBarColor() {
        Window window = this.getWindow();
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(this.getResources().getColor(R.color.home_top_layout_color));
        ViewGroup mContentView =
                (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View .预留出系统 View 的空间.
            ViewCompat.setFitsSystemWindows(mChildView, true);
        }
    }

    private void initActionBar() {
        AmigoActionBar actionBar = getAmigoActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void initViews() {
        iv_setting = (ImageView) findViewById(R.id.iv_setting);
        iv_history = (ImageView) findViewById(R.id.iv_history);
        rl_group_transfer = (RelativeLayout) findViewById(R.id.rl_group_transfer);
        rl_invite_friend = (RelativeLayout) findViewById(R.id.rl_invite_friend);
    }

    private void setListener() {
        iv_setting.setOnClickListener(this);
        iv_history.setOnClickListener(this);
        rl_group_transfer.setOnClickListener(this);
        rl_invite_friend.setOnClickListener(this);
    }

    public void sender(View view) {
        Intent intent = new Intent(this, SelectFilesActivity.class);
        intent.putExtra(Constants.IS_GROUP_OWNER,true);
        startActivity(intent);
    }

    public void receiver(View view) {
        startActivity(new Intent(this, ReceiverActivity.class));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000 || isExistActivity(LaunchActivity.class)) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 判断某一个类是否存在任务栈里面
     *
     * @return
     */
    private boolean isExistActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        ComponentName cmpName = intent.resolveActivity(getPackageManager());
        boolean flag = false;
        if (cmpName != null) { // 说明系统中存在这个activity
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(10);
            for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                if (taskInfo.baseActivity.equals(cmpName)) { // 说明它已经启动了
                    flag = true;
                    break;  //跳出循环，优化效率
                }
            }
        }
        return flag;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_setting:
                startActivity(new Intent(this, SettingActivity.class));
                break;
            case R.id.rl_invite_friend:
                startActivity(new Intent(this, InviteFriendActivity.class));
                break;
            case R.id.iv_history:
                startActivity(new Intent(this, FileRecordActivity.class));
                break;
            case R.id.rl_group_transfer:
                startActivity(new Intent(this, GroupTransferActivity.class));
                break;
            default:
                break;
        }
    }

}

