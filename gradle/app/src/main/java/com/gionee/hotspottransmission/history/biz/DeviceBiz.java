package com.gionee.hotspottransmission.history.biz;

import android.content.Context;
import com.gionee.hotspottransmission.history.bean.DeviceInfo;
import com.gionee.hotspottransmission.history.dao.DeviceDao;
import com.gionee.hotspottransmission.utils.LogUtil;
import java.util.List;

/**
 * Created by zhuboqin on 3/05/16.
 */
public class DeviceBiz {

    private DeviceDao dao;

    public DeviceBiz(Context context){
        dao = new DeviceDao(context);
    }

    public List<DeviceInfo> findDevices(){
        List<DeviceInfo> infos = dao.getDevices();
        return infos;
    }

    /**
     * 添加连接设备到数据库
     * @param info
     */
    public void addDevice(DeviceInfo info){
        //先更新历史设备中与当前设备address相同的设备名
        dao.updateDeviceName(info.deviceAddress,info.deviceName);
        //暂时先把所有的设备信息都插入进去，在查询历史设备时再通过过滤来查询
//        List<DeviceInfo> devices = dao.getDevices();
//        //判断如果是同一天连接的设备则不插入到表内
//        for(DeviceInfo device : devices){
//            if(device.equals(info)){
//                LogUtil.i("---------db已经保存该设备" + info.toString());
//                return;
//            }
//        }
        dao.addDevice(info);
    }

    public boolean deleteAllDevice(){
        return dao.deleteAllDevices();
    }

}
