package com.gionee.secretary.presenter;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.Editable;

import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.ui.viewInterface.IPasswordVerifyView;
import com.gionee.secretary.module.settings.PasswordModel;
import com.gionee.secretary.utils.LogUtils;

/**
 * Created by liyy on 16-12-2.
 */
public class PasswordVerifyPresenter {
    private static final String TAG = "PasswordVerifyPresenter";
    private Context mContext;
    private IPasswordVerifyView mPasswordVerifyView;
    private String mPassword;
    private boolean isClosePw;
    private boolean mLockToAppEnabled;
    private boolean isInLockTask;

    public PasswordVerifyPresenter(Context context, IPasswordVerifyView passwordVerifyView) {
        this.mContext = context;
        this.mPasswordVerifyView = passwordVerifyView;

        PasswordModel screenLockLab = PasswordModel.getInstance(mContext);
        mPassword = screenLockLab.getPassword();
    }


    public void changeActionBar() {
        Intent intent = ((Activity) mPasswordVerifyView).getIntent();
        isClosePw = intent.getBooleanExtra(Constants.EXTRA_CLOSE_PASSWORD_SWITCH, false);
        mPasswordVerifyView.initActionBar(isClosePw);

        mLockToAppEnabled = false;//Settings.System.getInt(mContext.getContentResolver(), Settings.System.LOCK_TO_APP_ENABLED, 0) != 0;
        final ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        isInLockTask = am.isInLockTaskMode();
    }

    public void backToUpView() {
        if (mLockToAppEnabled && isInLockTask) {
            return;
        }

        mPasswordVerifyView.hideInputMethod();
        if (!isClosePw) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            mContext.startActivity(home);

        } else {
            ((Activity) mPasswordVerifyView).finish();
        }

    }

    public void verifyPassword(Editable s, String password) {
        LogUtils.i(TAG, "verifyPassword......length:" + s.length() + "mPassword:" + mPassword);
        if (s.length() == Constants.PASSWORD_LENGTH) {
            if (password.equals(mPassword)) {
                mPasswordVerifyView.hideInputMethod();
                if (isClosePw) { //关闭密码保护
                    ((Activity) mPasswordVerifyView).setResult(Activity.RESULT_OK);
                    ((Activity) mPasswordVerifyView).finish();
                } else {//解锁后释放锁
                    LogUtils.i(TAG, "setPWD....LockState....false");
                    PasswordModel screenlockLab = PasswordModel.getInstance(mContext);
                    screenlockLab.updateLockState(false);
                    ((Activity) mPasswordVerifyView).finish();
                }
                mPasswordVerifyView.resetFailCount();
            } else {
                mPasswordVerifyView.passwordAgain();
            }
        }
    }
}
