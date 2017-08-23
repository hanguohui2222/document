package com.gionee.secretary.module.settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.gionee.account.sdk.GioneeAccount;
import com.gionee.account.sdk.listener.GetLoginInfoListener;
import com.gionee.account.sdk.listener.LoginResultListener;
import com.gionee.account.sdk.listener.VerifyListener;
import com.gionee.account.sdk.vo.LoginInfo;
import com.gionee.secretary.R;
import com.gionee.secretary.constants.Constants;
import com.gionee.secretary.ui.activity.SettingActivity;
import com.gionee.secretary.utils.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by liyy on 16-4-7.
 */
public class LoginModel {
    private static final String TAG = "LoginModel";
    private Context mContext;
    public GioneeAccount mGioneeAccount;
    AmigoAccountLoginLinster loginLinster = null;


    public LoginModel(Context context) {
        this.mContext = context;
        this.mGioneeAccount = GioneeAccount.getInstance(context.getApplicationContext());
    }

    /**
     * 登陆
     */
    public void login() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return mGioneeAccount.isAccountLogin();
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (aBoolean) {
                    toSaveUserPhoto();
                    if(loginLinster != null){
                        loginLinster.loginOK();
                    }
                } else {
                    loginAmigoAccount();
                }
            }
        }.execute();
    }

    public void logout() {
        loginOut(new VerifyListener() {
            @Override
            public void onSucess(Object o) {
                is_set_password_and_login = false;
                //Gionee <gn_by> <zhengyt> <2017-04-28> add for Bug#112927 begin
                Intent intent = new Intent(mContext, SettingActivity.class);
                mContext.startActivity(intent);
                //Gionee <gn_by> <zhengyt> <2017-04-28> add for Bug#112927 end
            }

            @Override
            public void onCancel(Object o) {
            }
        });
    }


    public Bitmap getUserPhoto() {
        Bitmap bitmap;
        File file = new File(mContext.getCacheDir(), mGioneeAccount.getUserId() + Constants.SUFFIX);
        if (file.exists()) {
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                bitmap = null;
            }
        } else {
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.user_icon);
        }
        return bitmap;
    }

    /**
     * 提供用户头像
     */
    public void toSaveUserPhoto() {
        String userId = mGioneeAccount.getUserId();
        boolean found = findUserPhoto(userId);
        if (!found) {
            getAccountLocalInfo(new MyGetLoginInfoListener());
        }

        if (isLoginFromPassWord && mCommonHandler != null) {
            Message msg = mCommonHandler.obtainMessage(RESET_PASSWORD);
            mCommonHandler.sendMessageDelayed(msg, DELAY_MILLIS);
        }
    }

    /**
     * 获取金立帐号信息，如头像 GetUserProfileListener的 onComplete(Object o)回调 Md md = (Md)
     * o LoginInfo.getphoto为头像
     */
    public void getAccountLocalInfo(GetLoginInfoListener getLoginInfoListener) {
        mGioneeAccount.getLoginInfo(getLoginInfoListener);
    }

    private class MyGetLoginInfoListener implements GetLoginInfoListener {
        @Override
        public void onPhoneLogin(LoginInfo loginInfo) {
            LogUtils.i(TAG, "onPhoneLogin:" + loginInfo.getPhoto() + "  xxx.uid:" + loginInfo.getUid() + "  name:" + loginInfo.getName());
            //Gionee zhengyt 2017-3-16 modify for GNSPR#72468
            //saveUserPhoto(loginInfo.getUid(), loginInfo.getPhoto());
        }

        @Override
        public void onQQLogin(LoginInfo loginInfo) {
            saveUserPhoto(loginInfo.getUid(), loginInfo.getPhoto());
        }

        @Override
        public void onSinaWeiBoLogin(LoginInfo loginInfo) {
            saveUserPhoto(loginInfo.getUid(), loginInfo.getPhoto());
        }

        @Override
        public void onEmailLogin(LoginInfo loginInfo) {

        }

        @Override
        public void onUnKownLogin(LoginInfo loginInfo) {

        }

        @Override
        public void onUnLogin() {

        }

        @Override
        public void onError(Exception e) {

        }
    }

    private void saveUserPhoto(final String userId, final Bitmap photo) {
        if (photo == null) {
            LogUtils.i(TAG, "LoginModel not found user photo");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(mContext.getCacheDir(), userId + Constants.SUFFIX);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    photo.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.flush();
                    out.close();
                    LogUtils.i(TAG, mContext.getString(R.string.image_saved));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 判断金立帐号登录状态
     *
     * @return
     */
    public void amigoAccountLogin() {
        new LoginTask().execute();
    }

    private class LoginTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return mGioneeAccount.isAccountLogin();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean && loginLinster != null) {
                loginLinster.loginOK();
            } else if (!aBoolean && loginLinster != null) {
                loginLinster.loginNo();
            }
        }
    }

    public void setAmigoAccountLoginLinster(AmigoAccountLoginLinster linster) {
        loginLinster = linster;
    }

    public interface AmigoAccountLoginLinster {
        void loginOK();//已经登录;

        void loginNo();//未登录;
    }

    /**
     * 调用amigo登陆
     *
     * @param
     */
    private void loginAmigoAccount() {
        mGioneeAccount.login(mContext, Constants.APPID, new AmigoAccountLoginResultListener());
    }


    public boolean findUserPhoto(String uid) {
        File file = new File(mContext.getCacheDir(), uid + Constants.SUFFIX);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    private class AmigoAccountLoginResultListener extends LoginResultListener {

        public AmigoAccountLoginResultListener() {
        }


        @Override
        public void onSucess(Object object) {
            final String uid = ((LoginInfo) object).getUid();
            final LoginInfo loginInfo = (LoginInfo) object;
            String userName = loginInfo.getName();
            LogUtils.i(TAG, "LoginModel login onSuccess userName=" + userName);

            is_set_password_and_login = true;

            if (!findUserPhoto(uid)) {
                Bitmap photo = loginInfo.getPhoto();
                if (photo != null) {
                    saveUserPhoto(uid, photo);
                } else {
                    LogUtils.i(TAG, "LoginModel user photo is null on login success");
                }
            }

            if (isLoginFromPassWord) {
                Message msg = mCommonHandler.obtainMessage(RESET_PASSWORD);
                mCommonHandler.sendMessageDelayed(msg, DELAY_MILLIS);
            }
            Intent intent = new Intent(mContext, SettingActivity.class);
            mContext.startActivity(intent);

        }

        @Override
        public void onCancel(Object o) {
            if (o != null) {
                int code = (Integer) o;
                if (code == GioneeAccount.ERROR_VERSION_NOT_SUPPORT) {
                    Toast.makeText(mContext, R.string.error_version_not_support, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mContext, R.string.login_cancel, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onGetTokenError(Object o) {
            LogUtils.i(TAG, "LoginResultListener onGetTokenError");
        }
    }


    /**
     * 登出帐号
     */
    public void loginOut(VerifyListener loginoutVerifyListener) {
        mGioneeAccount.callLoginOut(mContext.getApplicationContext(), mGioneeAccount.getUserId(), loginoutVerifyListener);
    }


    private static boolean isLoginFromPassWord = false;
    private Handler mCommonHandler;
    private static final int RESET_PASSWORD = 5;
    private static final long DELAY_MILLIS = 300l;

    public void setPassWord(boolean isFromPassWord, Handler handler) {
        isLoginFromPassWord = true;
        mCommonHandler = handler;
    }


    public void checkLogInfo() {

        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setName(mGioneeAccount.getUsername());
        loginInfo.setUid(mGioneeAccount.getUserId());
        mGioneeAccount.verify(mContext.getApplicationContext(), loginInfo, new VerifyListener() {
            @Override
            public void onSucess(Object o) {
                Toast.makeText(mContext, "验证通过", Toast.LENGTH_SHORT).show();
                if (isLoginFromPassWord) {
                    Message msg = mCommonHandler.obtainMessage(RESET_PASSWORD);
                    mCommonHandler.sendMessageDelayed(msg, DELAY_MILLIS);
                }
            }

            @Override
            public void onCancel(Object o) {
                Toast.makeText(mContext, "验证失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean is_set_password_and_login = false;

    public boolean checkLoginAmigoAccount() {
        return is_set_password_and_login;
    }

}