package com.gionee.secretary.ui.viewInterface;

import com.gionee.secretary.bean.SelfCreateSchedule;

/**
 * Created by luorw on 4/27/16.
 */
public interface ISelfCreateScheduleView {
    void saveSuccess();

    void cancel();

    void EditSchedule(SelfCreateSchedule schedule);

    void UpdateScheduleId(int id);
}
