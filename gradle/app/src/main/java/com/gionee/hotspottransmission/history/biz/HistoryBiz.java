package com.gionee.hotspottransmission.history.biz;

import android.content.Context;

import com.gionee.hotspottransmission.history.bean.HistoryInfo;
import com.gionee.hotspottransmission.history.dao.HistoryDao;

import java.util.List;

/**
 * Created by zhuboqin on 4/05/16.
 */
public class HistoryBiz {

    private HistoryDao dao;

    public HistoryBiz(Context context){
        dao = new HistoryDao(context);
    }

    public List<HistoryInfo> getHistory(){
        List<HistoryInfo> list = dao.findAllTitle();
        return list;
    }

    public boolean deleteHistory(){
        return dao.deleteAllHistory();
    }

    public void addHistoryRecord(HistoryInfo historyInfo){
        //先更新T_HISTORY_TITLE_MeteData中设备名
        dao.updateDeviceName(historyInfo.deviceAddress,historyInfo.deviceName);
        dao.insertHistory(historyInfo);
    }

}
