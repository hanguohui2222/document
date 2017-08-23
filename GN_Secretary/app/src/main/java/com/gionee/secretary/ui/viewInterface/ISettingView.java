package com.gionee.secretary.ui.viewInterface;

/**
 * Created by liyy on 16-11-30.
 */
public interface ISettingView {
    void openPasswordSwitch();

    void closePasswordSwitch();

    void openWidgetSwitch();

    void closeWidgetSwitch();

    void updateTravelModePreferenceSummery(String summery);

    void showToast(int duration);

    void enableWidgetSwitch();

    void updateNotifyRingPreferenceSummery(String ringName);
}
