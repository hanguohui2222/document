package com.gionee.hotspottransmission.history.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gionee.hotspottransmission.db.WlanDBMetaData;
import com.gionee.hotspottransmission.db.WlanDBOpenHelper;
import com.gionee.hotspottransmission.db.WlanSQLite;
import com.gionee.hotspottransmission.history.bean.DeviceInfo;
import com.gionee.hotspottransmission.utils.LogUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhuboqin on 3/05/16.
 */
public class DeviceDao {

    private SQLiteDatabase db;
    private final WlanDBOpenHelper helper;

    public DeviceDao(Context context){
        helper = WlanSQLite.getSQLiteHelper(context);
    }

    public List<DeviceInfo> getDevices(){
        LogUtil.i("设备dao DeviceDao getDevices ");

        db = helper.getWritableDatabase();
        DeviceInfo info = null;
        List<DeviceInfo> infos = new ArrayList<>();
        Cursor cursor = null;
        if(db.isOpen()){
            
            try{
            	cursor = db.rawQuery(
                        "SELECT * FROM " + WlanDBMetaData.T_DEVICE_INFO_MeteData.TABLE_NAME + " order by "
                                + WlanDBMetaData.T_DEVICE_INFO_MeteData.CONNECTTIME+" desc", null);

                infos.clear();
                
                while (cursor.moveToNext()){
                    info = new DeviceInfo();
                    info.deviceName = cursor.getString(cursor.getColumnIndex(WlanDBMetaData.T_DEVICE_INFO_MeteData.DEVICENAME));
                    long dateLong = cursor.getLong(cursor.getColumnIndex(WlanDBMetaData.T_DEVICE_INFO_MeteData.CONNECTTIME));
                    String address = cursor.getString(cursor.getColumnIndex(WlanDBMetaData.T_DEVICE_INFO_MeteData.ADDRESS));
                    String status = cursor.getString(cursor.getColumnIndex(WlanDBMetaData.T_DEVICE_INFO_MeteData.STATUS));
                    String ip = cursor.getString(cursor.getColumnIndex(WlanDBMetaData.T_DEVICE_INFO_MeteData.IP));
                    info.connectTime = new Date(dateLong);
                    info.deviceAddress = address;
                    info.status = status;
                    info.ip = ip;
                    infos.add(info);
                }
                LogUtil.i("设备dao DeviceDao devices 个数" + infos.size() + " 所有设备 " + infos.toString());
            }catch (Exception exception){
            	LogUtil.i("设备dao DeviceDao devices infos.size = " + infos.size() + " 所有设备 " + infos.toString());
            	LogUtil.i("-----------exception---------");
            	if(db.isOpen()){
            		LogUtil.i("-----------exception-----open----");
            		if(cursor != null){
            			LogUtil.i("-----------exception-----cursor != null----");
            			cursor.close();
                    	db.close();
                    }
                }
                LogUtil.i(exception.toString());
            }

        }
        if(db.isOpen()){
            cursor.close();
            db.close();
        }
        return infos;
    }

    public void addDevice(DeviceInfo info){
        db = helper.getWritableDatabase();
        if(db.isOpen()){
            long dateLong = info.connectTime.getTime();
            db.execSQL("INSERT INTO " + WlanDBMetaData.T_DEVICE_INFO_MeteData.TABLE_NAME + " values( null, '"
                    + info.deviceName + "'," + dateLong + ",'"+info.deviceAddress + "','"+info.status + "','" + info.ip+"')");
            db.close();
        }
    }

    public boolean deleteAllDevices(){
        db = helper.getWritableDatabase();
        int result = db.delete(WlanDBMetaData.T_DEVICE_INFO_MeteData.TABLE_NAME,null,null);
        db.close();
        if(result != 0){
            return true;
        }else
            return false;
    }

    public void updateDeviceName(String address, String newName) {
        LogUtil.i("update, address: " + address + ", newName: " + newName);
        if (address == null || newName == null) {
            return;
        }
        db = helper.getWritableDatabase();
        String sql = "update " + WlanDBMetaData.T_DEVICE_INFO_MeteData.TABLE_NAME + " set "
                + WlanDBMetaData.T_DEVICE_INFO_MeteData.DEVICENAME + "='" + newName + "'"
                + " where " + WlanDBMetaData.T_DEVICE_INFO_MeteData.ADDRESS + "='" + address + "';";
        if (db.isOpen()) {
            db.execSQL(sql);
            db.close();
        }
    }

    public void updateDeviceStatus(String address, int status) {
        LogUtil.i("update, address: " + address + ", status: " + status);
        if (address == null) {
            return;
        }
        db = helper.getWritableDatabase();
        String sql = "update " + WlanDBMetaData.T_DEVICE_INFO_MeteData.TABLE_NAME + " set "
                + WlanDBMetaData.T_DEVICE_INFO_MeteData.STATUS + "='" + status
                + "' where " + WlanDBMetaData.T_DEVICE_INFO_MeteData.ADDRESS + "='" + address + "';";
        if (db.isOpen()) {
            db.execSQL(sql);
            db.close();
        }
    }

}
