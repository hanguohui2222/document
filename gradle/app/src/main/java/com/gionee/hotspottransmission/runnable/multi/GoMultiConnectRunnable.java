package com.gionee.hotspottransmission.runnable.multi;

import android.content.Context;
import com.gionee.hotspottransmission.callback.IMultiDeviceCallBack;
import com.gionee.hotspottransmission.runnable.GoConnectRunnable;

/**
 * Created by luorw on 7/7/17.
 */
public class GoMultiConnectRunnable extends GoConnectRunnable {

    public GoMultiConnectRunnable(Context context) {
        super(context);
    }

    @Override
    public void run() {
        isGroupTransfer = true;
        super.run();
    }
}
