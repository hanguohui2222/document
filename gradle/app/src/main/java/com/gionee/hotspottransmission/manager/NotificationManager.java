package com.gionee.hotspottransmission.manager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.bean.FileReceiveData;
import com.gionee.hotspottransmission.bean.FileSendData;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.utils.LogUtil;
import com.gionee.hotspottransmission.view.BaseTransferActivity;
import com.gionee.hotspottransmission.view.GcTransferActivity;
import com.gionee.hotspottransmission.view.GoTransferActivity;

import java.util.ArrayList;

/**
 * Created by luorw on 4/27/16.
 */
public class NotificationManager {
    public static final int NOTIFY_ID_FILE_SENDER = 1;
    public static final int NOTIFY_ID_FILE_RECEIVER = 2;
    public static final int NOTIFY_SUCCESS = 3;
    public static final int NOTIFY_FAILED = 4;
    public static final int NOTIFY_CANCEL = 5;
    public static final int NOTIFY_TRANSFERING = 6;
    public static final int NOTIFY_COUNT = 7;

    public static Notification generNotification(Context context, int notifyId, int state,boolean isGroupOwner) {
        String notifyHint = getTransferNotificationHint(context, getNotificationInfos(notifyId));
        Notification notification = sendNotification(context, notifyId, notifyHint, true, state,isGroupOwner);
        return notification;
    }
    public static Notification updateNotification(Context context, int notifyId,int state,boolean isGroupOwner) {
        String notifyHint = getTransferNotificationHint(context, getNotificationInfos(notifyId));
        Notification notification = sendNotification(context, notifyId, notifyHint, false, state,isGroupOwner);
        return notification;
    }

    private static Notification sendNotification(Context context, int notifyId, String notifyHint, boolean isNewNotify, int state,boolean isGroupOwner) {
        Notification.Builder b = new Notification.Builder(context);
        b.setSmallIcon(R.drawable.notifycation_icon);
        b.setContentTitle(getTransferNotTitleId(context,state,notifyId));
        LogUtil.i("state = "+state+" ,notifyHint = "+notifyHint);
        b.setContentText(notifyHint);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0,
                getTransferNotificationIntent(context,notifyId,isGroupOwner), PendingIntent.FLAG_UPDATE_CURRENT);
        b.setContentIntent(pIntent);
        Notification noti = b.getNotification();
        if(state == Constants.FILE_TRANSFER_ALL_COMPLETE || state == Constants.FILE_TRANSFER_BREAK){
            noti.flags |= Notification.FLAG_AUTO_CANCEL;
        }else{
            noti.flags |= Notification.FLAG_NO_CLEAR;
        }
        if (isNewNotify) {
        	noti.defaults |= Notification.DEFAULT_SOUND;
        }
        return noti;
    }

    public static void startNotification(Context context, int notifyId, Notification noti) {
        android.app.NotificationManager notificationManager = (android.app.NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifyId, noti);
    }

    public static void cancelNotification(Context context, int notifyId) {
        android.app.NotificationManager notificationManager = (android.app.NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notifyId);
    }
    
    private static Intent getTransferNotificationIntent(Context context,int notifyId,boolean isGroupOwner) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_ENTER_FROM_NOTIFICATION);
        if(isGroupOwner){
            intent.setClass(context, GoTransferActivity.class);
        }else{
            intent.setClass(context, GcTransferActivity.class);
        }
        if(notifyId == NOTIFY_ID_FILE_SENDER){
            intent.putExtra(Constants.CURRENT_TAB,0);
        }else{
            intent.putExtra(Constants.CURRENT_TAB,1);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }
    
    private static String getTransferNotTitleId(Context context, int state,int notifyId) {
        String title = null;
        if (state == Constants.FILE_TRANSFER_ALL_COMPLETE) {
            title = context.getResources().getString(R.string.file_send_all_success_notification_tilte);
        }else if(state == Constants.FILE_TRANSFER_BREAK){
            title = context.getResources().getString(R.string.file_notification_disconnect_hint);
        }else{
            title = context.getResources().getString(R.string.file_send_notification_tilte,getTransferCountNotification(notifyId));
        }
        return title;
    }
    
    private static int getTransferCountNotification(int notifyId) {
        int transferCount = 0;
        if(NOTIFY_ID_FILE_SENDER == notifyId){
            transferCount = FileSendData.getInstance().getFileSendList().size();
        }else{
            transferCount = FileReceiveData.getInstance().getFileReceiveList().size();
        }
        return transferCount;
    }
    
    private static String getTransferNotificationHint(Context context, int[] transferInfo) {
        String notifyHint = "";
        if (transferInfo != null) {
            String successNotifyHint = context.getResources().getString(R.string.file_notification_success_hint, transferInfo[NOTIFY_SUCCESS]);
            String failedNotifyHint = context.getResources().getString(R.string.file_notification_failed_hint, transferInfo[NOTIFY_FAILED]);
            notifyHint = successNotifyHint + "," + failedNotifyHint;
        }
        return notifyHint;
    }

    public static int[] getNotificationInfos(int notifyId) {
        int[] notificationInfos = new int[NotificationManager.NOTIFY_COUNT];
        ArrayList<FileInfo> fileDatas = null;
        if(NOTIFY_ID_FILE_SENDER == notifyId){
            fileDatas =  FileSendData.getInstance().getFileSendList();
        }else{
            fileDatas =  FileReceiveData.getInstance().getFileReceiveList();
        }
        for (int i = 0; i < fileDatas.size(); i++) {
            FileInfo fileTransferData = fileDatas.get(i);
            switch(fileTransferData.getState()) {
                case Constants.FILE_TRANSFER_SUCCESS:
                    notificationInfos[NotificationManager.NOTIFY_SUCCESS]++;
                    break;
                case Constants.FILE_TRANSFER_FAILURE:
                    notificationInfos[NotificationManager.NOTIFY_FAILED]++;
                    break;
                case Constants.FILE_TRANSFER_CANCEL:
                    notificationInfos[NotificationManager.NOTIFY_CANCEL]++;
                    break;
                default:
                    notificationInfos[NotificationManager.NOTIFY_TRANSFERING]++;
                    break;
            }
        }
        return notificationInfos;
    }
}
