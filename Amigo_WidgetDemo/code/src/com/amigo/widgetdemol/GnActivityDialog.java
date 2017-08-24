package com.amigo.widgetdemol;

import amigoui.app.AmigoActivity;
import amigoui.app.AmigoAlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;

public class GnActivityDialog extends AmigoActivity {
    
    AmigoAlertDialog dialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gn_activity_dialog);
        createDialog();
    }
    
    
    private void createDialog() {
        AmigoAlertDialog.Builder b = new AmigoAlertDialog.Builder(this);
        b.setTitle("标题");
        b.setMessage("有按扭对话框示例，右上角无X");
        b.setNegativeButton("取消", new AmigoAlertDialog.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
                finish();
            }
        });
        b.setPositiveButton(/*Button.GN_BUTTON_RECOM_STYLE, */"确定", new AmigoAlertDialog.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
                finish();
            }
        });
        dialog = b.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
        dialog.setOnCancelListener(new AmigoAlertDialog.OnCancelListener() {
            
            @Override
            public void onCancel(DialogInterface arg0) {
                finish();                
            }
        });
    }
}
