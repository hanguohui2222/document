package com.gionee.hotspottransmission.runnable;

import android.content.Context;

import com.gionee.hotspottransmission.callback.IMultiDeviceCallBack;
import com.gionee.hotspottransmission.callback.IMultiTransferService;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.sharepreference.DeviceSp;
import com.gionee.hotspottransmission.utils.LogUtil;
import com.gionee.hotspottransmission.R;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by luorw on 8/1/17.
 */
public class PingRunnable implements Runnable {
    private IMultiDeviceCallBack multiDeviceCallBack;
    private Context mContext;

    public PingRunnable(Context context , IMultiDeviceCallBack multiDeviceCallBack) {
        this.mContext = context.getApplicationContext();
        this.multiDeviceCallBack = multiDeviceCallBack;
    }

    @Override
    public void run() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LogUtil.i("luorw , PingRunnable , pingNetWork-------------");
                if(!pingNetWork()){
                    LogUtil.i("luorw , PingRunnable , pingNetWork--------false-----");
                    String msg = Constants.MSG_OFFLINE +","+ DeviceSp.getInstance().getHostIp(mContext) +","+DeviceSp.getInstance().getDeviceName(mContext)+","+DeviceSp.getInstance().getDeviceAddress(mContext);
//                    multiDeviceCallBack.offline(msg);
//                    timer.cancel();
                }
            }
        }, 0 , 3000);
    }

    private boolean pingNetWork(){
        try {
            String ipAddress = DeviceSp.getInstance().getHostIp(mContext);
            Process process = Runtime.getRuntime().exec("/system/bin/ping -c 1 -w 100 " + ipAddress);
            int status = process.waitFor();
            if (status == 0) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            LogUtil.i("luorw , PingRunnable , pingNetWork--------IOException-----"+e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            LogUtil.i("luorw , PingRunnable , pingNetWork--------InterruptedException-----"+e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
