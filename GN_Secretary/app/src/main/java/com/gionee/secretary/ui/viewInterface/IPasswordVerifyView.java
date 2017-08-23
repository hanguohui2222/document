package com.gionee.secretary.ui.viewInterface;

/**
 * Created by liyy on 16-12-2.
 */
public interface IPasswordVerifyView {
    void initActionBar(boolean isClosePw);

    void hideInputMethod();

    void passwordAgain();

    void resetFailCount();

    /*modify by zhengjl at 2017-2-9 for GNSPR #66730 not end*/
    void inputAgain();
}
