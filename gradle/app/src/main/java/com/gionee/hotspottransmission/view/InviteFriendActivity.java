package com.gionee.hotspottransmission.view;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.gionee.hotspottransmission.R;
import java.io.File;
import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;
/**
 * Created by luorw on 5/12/17.
 */
public class InviteFriendActivity extends AmigoActivity implements View.OnClickListener{
    private Button bt_blue;
    // 蓝牙功能包名
    private final String mBluetoothPackageName = "com.android.bluetooth";
    //蓝牙功能操作类名
    private final String mBluetoothClassName = "com.android.bluetooth.opp.BluetoothOppLauncherActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friend);
        initActionBar();
        findViews();
        setListener();
    }

    private void initActionBar() {
        AmigoActionBar actionBar = getAmigoActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(amigoui.app.AmigoActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.invite_friend_install));
        actionBar.show();
    }

    private void findViews(){
        bt_blue = (Button)findViewById(R.id.bt_blue);
    }

    private void setListener(){
        bt_blue.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_blue:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                PackageManager packageManager = getPackageManager();
                PackageInfo packageInfo = null;
                try {
                    packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                File file = new File(packageInfo.applicationInfo.publicSourceDir);
                Uri uri = Uri.fromFile(file);
                sendIntent.putExtra(Intent.EXTRA_STREAM,uri);
                sendIntent.setType("application/vnd.android.package-archive");
                sendIntent.setClassName(mBluetoothPackageName, mBluetoothClassName);
                startActivity(sendIntent);
                break;
        }
    }
}
