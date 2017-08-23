package com.gionee.secretary.module;

import java.util.ArrayList;
import java.util.List;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class TeleListener extends PhoneStateListener {
    private List<ICallStatusListener> mListeners = new ArrayList<ICallStatusListener>();

    private static TeleListener mTeleListener;

    private TeleListener() {
    }

    public static TeleListener getInstance() {
        if (mTeleListener == null) {
            mTeleListener = new TeleListener();
        }
        return mTeleListener;
    }

    public void registerCallStatusListener(ICallStatusListener listener) {
        mListeners.add(listener);
    }

    public void unRegisterCallStatusListener(ICallStatusListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        switch (state) {
            // 当处于待机状态中
            case TelephonyManager.CALL_STATE_IDLE: {
                for (ICallStatusListener listener : mListeners) {
                    listener.onIdle();
                }
                break;
            }
            // 当处于通话中
            case TelephonyManager.CALL_STATE_OFFHOOK: {
                for (ICallStatusListener listener : mListeners) {
                    listener.onOffHook();
                }
                break;
            }
            // 当处于拨号状态中..
            case TelephonyManager.CALL_STATE_RINGING: {
                for (ICallStatusListener listener : mListeners) {
                    listener.onRinging();
                }
                break;
            }
            default:
                break;
        }
    }
}
