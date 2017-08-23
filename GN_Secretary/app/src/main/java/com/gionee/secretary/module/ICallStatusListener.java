package com.gionee.secretary.module;

public interface ICallStatusListener {

    void onIdle();

    void onOffHook();

    void onRinging();
}
