package com.gionee.hotspottransmission.manager;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;

import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.constants.Constants;
import com.gionee.hotspottransmission.dao.MediaDao;
import com.gionee.hotspottransmission.callback.IDataQueryCallBack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luorw on 4/26/16.
 */
public class QueryImageDataManager {
    private Map<String, List<FileInfo>> mDataInfoMap;

    public Map<String, List<FileInfo>> getmDataInfoMap() {
        return mDataInfoMap;
    }

    //modified by luorw for #20370,#23997 begin
    public void queryDataInfo(final Activity activity, final int dataType, final Handler handler) {
        if (mDataInfoMap == null) {
            mDataInfoMap = new ArrayMap<>();
        } else {
            mDataInfoMap.clear();
        }
        final MediaDao dao = new MediaDao(activity, new IDataQueryCallBack() {
            @Override
            public void onQueryEnd(List<FileInfo> imageList) {
                //added by luorw for GNSPR #34374 begin
                if (mDataInfoMap != null) {
                    mDataInfoMap.clear();
                }
                //added by luorw for GNSPR #34374 end
                for (FileInfo info : imageList) {
                    String key = info.getFileDir();
                    Object id = info.getId();
                    if (id == null || key == null || key.isEmpty()) {
                        continue;
                    } else if (!mDataInfoMap.containsKey(key)) {
                        mDataInfoMap.put(key, new ArrayList<FileInfo>());
                    }
                    mDataInfoMap.get(key).add(info);
                }
                Message msg = Message.obtain();
                msg.what = Constants.REFRESH_IMAGE_DRI_GV;
                msg.obj = mDataInfoMap;
                handler.sendMessage(msg);
            }
        });
        dao.queryMediaData(dataType);
    }
//modified by luorw for #20370,#23997 end
}
