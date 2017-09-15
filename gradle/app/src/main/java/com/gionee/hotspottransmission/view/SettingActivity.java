package com.gionee.hotspottransmission.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.LogUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import amigoui.app.AmigoActionBar;
import amigoui.app.AmigoActivity;
import amigoui.app.AmigoAlertDialog;
import amigoui.widget.AmigoButton;
import com.gionee.hotspottransmission.R;

/**
 * Created by luorw on 17/5/9.
 */
public class SettingActivity extends AmigoActivity implements View.OnClickListener,TextWatcher{
    private static final int MSG_SHOW_INPUT_METHOD = 1;
    private static final int DELAY_MILLIS = 100;
    private Context mContext;
    private EditText et_rename;
    private AmigoButton positiveButton;
    private TextView mTv_DeviceName;
    private RelativeLayout mLayoutRename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mContext = this;
        initActionBar();
        initViews();
    }

    private void initViews() {
        mLayoutRename = (RelativeLayout)findViewById(R.id.rl_rename);
        mTv_DeviceName = (TextView)findViewById(R.id.rename_sub_title);
        String oldName = DeviceSp.getInstance().getDeviceName(mContext);
        mTv_DeviceName.setText(oldName);
        mLayoutRename.setOnClickListener(this);
    }

    private void initActionBar() {
        AmigoActionBar actionBar = getAmigoActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(amigoui.app.AmigoActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle(R.string.setting_action_title);
        actionBar.show();
    }

    private void showRenameDialog(){
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(mContext,R.style.AmigoDialogTheme);
        builder.setTitle(mContext.getResources().getString(R.string.dialog_title_modify_device_nickname));
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_rename_device, null);
        et_rename = (EditText) view.findViewById(R.id.phone_name);
        String oldName = mTv_DeviceName.getText().toString();
        et_rename.setText(oldName);
        et_rename.setSelection(oldName.length());
        ImageView clearButton = (ImageView) view.findViewById(R.id.clear_button);
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        final AmigoAlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Message msg = mCommonHandler.obtainMessage(MSG_SHOW_INPUT_METHOD);
        mCommonHandler.sendMessageDelayed(msg, DELAY_MILLIS);
        clearButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                et_rename.setText("");
            }
        });
        positiveButton = ((AmigoAlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String newName = et_rename.getText().toString();
                //点击确认时，如果包含特殊表情，则弹出toast提示，不关闭pop框
                if (containsEmoji(newName)) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_device_nickname_no_emoji), Toast.LENGTH_SHORT).show();
                } else {
                    renameDevice(newName);
                    dialog.dismiss();
                }
            }
        });
        et_rename.addTextChangedListener(this);
    }

    private Handler mCommonHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_INPUT_METHOD:
                    showInputMethod();
                    break;
                default:
                    break;
            }
        }
    };

    private void showInputMethod() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private String mBeforeText;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        mBeforeText = s.toString();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        LogUtil.i("onTextChanged-----s = " + s + " ,start = " + start + " ,before = " + before + " ,count = " + count);
        //不是在减少字符
        if (s != null && (count-before) > 0) {
            String tmp = s.toString();
            int bytesLength = tmp.getBytes().length;
            int strLength = s.toString().length();
            LogUtil.i("onTextChanged-----bytesLength = "+bytesLength);
            //如果输入框中的字节数大于30，则取前30个字节显示到输入框内
            if(bytesLength > 27){
                String changedStr = s.toString().substring(start, start + strLength - mBeforeText.length());
                LogUtil.i("onTextChanged-----changeStr = "+ changedStr);

                if(containsEmoji(changedStr)){
                    if(start == 0){
                        et_rename.setText("");
                    }else{
                        StringBuffer buffer = new StringBuffer(tmp);
                        buffer.delete(start , start + strLength - mBeforeText.length());
                        et_rename.setText(buffer.toString());
                        et_rename.setSelection(buffer.toString().length());
                        LogUtil.i("onTextChanged---大于30个字节--beforeStr = " + buffer.toString());
                    }
                }else{
                    String afterStr = getSubString(tmp);
                    LogUtil.i("onTextChanged---大于30个字节--afterStr = "+afterStr);
                    et_rename.setText(afterStr);
                    et_rename.setSelection(afterStr.length());
                }
            }
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        String s = "";
        if(editable.toString().getBytes().length > 27){
            s = getSubString(editable.toString());
        }else{
            s = editable.toString();
        }

        if(s.toString() == null || "".equals(s.toString()) || s.toString().trim().length() == 0){
            positiveButton.setEnabled(false);
        }else{
            positiveButton.setEnabled(true);
        }
        LogUtil.i("afterTextChanged-----s = "+s+" ,s.length() = "+s.toString().length());
    }

    private void renameDevice(String name){
        DeviceSp.getInstance().saveDeviceName(mContext,name);
        mTv_DeviceName.setText(name);
        String imei = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        DeviceSp.getInstance().saveDeviceAddress(mContext,imei);
    }

    /**
     * 检测是否有emoji表情
     * @param source
     * @return
     */
    private boolean containsEmoji(String source) {
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (!isEmojiCharacter(codePoint)) { // 如果不能匹配,则该字符是Emoji表情
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是Emoji
     * @param codePoint
     *            比较的单个字符
     * @return
     */
    private boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) || (codePoint == 0xD)
                || ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
                || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
    }

    private String getSubString(String content){
        try{
            int maxByte = 27;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int index = 0; //字符串的字符位置
            int count = 0; //目前的字节数
            while(count < maxByte){
                //当前的字节数目
                if(index < content.length()-1){
                    count += content.substring(index, index+1).getBytes("utf-8").length;
                    if(count <= maxByte){
                        out.write(content.substring(index, index+1).getBytes("utf-8"));
                    }
                    index++;
                }else{
                    break;
                }
            }
            String result = new String(out.toByteArray());
            out.close();
            return result;
        }catch(IOException e){
            LogUtil.e("onTextChanged---IOException = "+e.getMessage());
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        showRenameDialog();
    }
}
