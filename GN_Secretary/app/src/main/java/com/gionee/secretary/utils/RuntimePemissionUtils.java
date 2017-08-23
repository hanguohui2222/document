package com.gionee.secretary.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * Created by hangh on 7/7/17.
 */

public class RuntimePemissionUtils {
    public static boolean checkAndRequestForRunntimePermission(Activity activity, String []  permissionsNeeded) {
        // 1 检查权限
        ArrayList<String> permissionsNeedRequest = new ArrayList<String>();
        for (String permission : permissionsNeeded) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    == PackageManager.PERMISSION_GRANTED) {
                continue;
            }
            permissionsNeedRequest.add(permission);
        }
        // 2 请求权限
        if (permissionsNeedRequest.size() == 0) {
            return true;
        } else {
            String[] permissions = new String[permissionsNeedRequest.size()];
            permissions = permissionsNeedRequest.toArray(permissions);
            ActivityCompat.requestPermissions(activity, permissions, 0);
            return false;
        }
    }
}
