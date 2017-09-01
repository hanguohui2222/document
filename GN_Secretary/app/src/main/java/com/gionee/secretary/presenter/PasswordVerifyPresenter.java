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

import static com.gionee.secretary.constants.Constants.LOCK_TO_APP_ENABLED;

/**
 * Created by liyy on 16-12-2.
 */
public class PasswordVerifyPresenter extends BasePresenterImpl<IPasswordVerifyView> {
    private static final String TAG = "PasswordVerifyPresenter";
    private Context mContext;
    private String mPassword;
    private boolean isClosePw;
    private boolean mLockToAppEnabled;
    private boolean isInLockTask;

    public PasswordVerifyPresenter(Context context, IPasswordVerifyView passwordVerifyView) {
        this.mContext = context;
        attachView(passwordVerifyView);

        PasswordModel screenLockLab = PasswordModel.getInstance(mContext);
        mPassword = screenLockLab.getPassword();
    }


    public void changeActionBar() {
        Intent intent = ((Activity) mView).getIntent();
        isClosePw = intent.getBooleanExtra(Constants.EXTRA_CLOSE_PASSWORD_SWITCH, false);
        mView.initActionBar(isClosePw);

        mLockToAppEnabled = Settings.System.getInt(mContext.getContentResolver(), LOCK_TO_APP_ENABLED, 0) != 0;
        final ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        isInLockTask = am.isInLockTaskMode();
    }

    public void backToUpView() {
        if (mLockToAppEnabled && isInLockTask) {
            return;
        }

        mView.hideInputMethod();
        if (!isClosePw) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            mContext.startActivity(home);

        } else {
            ((Activity) mView).finish();
        }

    }

    public void verifyPassword(Editable s, String password) {
        LogUtils.i(TAG, "verifyPassword......length:" + s.length() + "mPassword:" + mPassword);
        if (s.length() == Constants.PASSWORD_LENGTH) {
            if (password.equals(mPassword)) {
                mView.hideInputMethod();
                if (isClosePw) { //关闭密码保护
                    ((Activity) mView).setResult(Activity.RESULT_OK);
                    ((Activity) mView).finish();
                } else {//解锁后释放锁
                    LogUtils.i(TAG, "setPWD....LockState....false");
                    PasswordModel screenlockLab = PasswordModel.getInstance(mContext);
                    screenlockLab.updateLockState(false);
                    ((Activity) mView).finish();
                }
                mView.resetFailCount();
            } else {
                mView.passwordAgain();
            }
        }
    }
}
