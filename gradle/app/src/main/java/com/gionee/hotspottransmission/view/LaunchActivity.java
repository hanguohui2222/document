package com.gionee.hotspottransmission.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showPermissionTip();
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
}
