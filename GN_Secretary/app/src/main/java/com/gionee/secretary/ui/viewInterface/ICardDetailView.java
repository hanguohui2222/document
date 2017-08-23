package com.gionee.secretary.ui.viewInterface;

import android.content.Intent;

/**
 * Created by luorw on 4/27/16.
 */
public interface ICardDetailView {
    void deleteSuccess(boolean isRepeatSchedule);

    void showCardDetail(Intent data);

    void editCard();
}
