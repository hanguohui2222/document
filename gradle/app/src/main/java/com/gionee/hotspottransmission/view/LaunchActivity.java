package com.gionee.hotspottransmission.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import com.gionee.hotspottransmission.constants.Constants;
import amigoui.app.AmigoActivity;
import amigoui.app.AmigoAlertDialog;
import amigoui.widget.AmigoCheckBox;
import amigoui.widget.AmigoTextView;
import com.gionee.hotspottransmission.R;

/**
 * Created by luorw on 17/24/16.
 */
public class LaunchActivity extends AmigoActivity {
    private static final int PERMISSIONS_REQUEST = 102;
    private String[] mPermissions = {"android.permission.ACCESS_WIFI_STATE","android.permission.CHANGE_WIFI_STATE","android.permission.CHANGE_NETWORK_STATE",
            "android.permission.INTERNET","android.permission.ACCESS_NETWORK_STATE","android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE","android.permission.READ_CONTACTS","android.permission.BLUETOOTH",
            "android.permission.SYSTEM_ALERT_WINDOW","android.permission.WRITE_SETTINGS","android.permission.MANAGE_DOCUMENTS","android.permission.READ_PHONE_STATE",
            "android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION","android.permission.CHANGE_WIFI_MULTICAST_STATE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
            showPermissionTip();
        }else{
            requestPermission();
        }
    }

    private void showPermissionTip(){
        final SharedPreferences sharedPreferences = getSharedPreferences(Constants.PERMISSION_TIP, Context.MODE_PRIVATE);
        Boolean isShow = sharedPreferences.getBoolean(Constants.SHOW_NET_TIP,true);
        if(isShow){
            final View view = LayoutInflater.from(this).inflate(R.layout.dialog_permission_tip, null);
            ((AmigoTextView) view.findViewById(R.id.tv_tip)).setText(R.string.welcome);
            AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(this);
            builder.setTitle(getString(R.string.welcome_title));
            builder.setView(view);
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AmigoCheckBox checkBox = (AmigoCheckBox) view.findViewById(R.id.checkBox);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(Constants.SHOW_NET_TIP,!checkBox.isChecked());
                    editor.commit();
                    sendToMainActivity();
                }

            });
            builder.setCancelable(false);
            AmigoAlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            if(null != dialog && !dialog.isShowing()){
                dialog.show();
            }
        } else {
            sendToMainActivity();
        }
    }

    private void sendToMainActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(LaunchActivity.this,MainActivity.class));
                finish();
            }
        },500);
    }


    private void requestPermission() {
        if (checkPermission()) {
            ActivityCompat.requestPermissions(this, mPermissions, PERMISSIONS_REQUEST);
        } else {
            sendToMainActivity();
        }
    }

    private boolean checkPermission(){
        boolean isNotGranted = false;
        for(int i = 0 ; i < mPermissions.length ; i++){
            isNotGranted = ContextCompat.checkSelfPermission(this, mPermissions[i]) != PackageManager.PERMISSION_GRANTED;
            if(isNotGranted){
                break;
            }
        }
        return isNotGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case PERMISSIONS_REQUEST:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//授权成功
                        sendToMainActivity();
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {//点击拒绝授权
                        finish();
                    }
                }
                break;
            default:
                break;
        }
    }
}
