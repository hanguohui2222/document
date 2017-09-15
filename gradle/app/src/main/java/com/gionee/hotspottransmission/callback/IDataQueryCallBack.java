package com.gionee.hotspottransmission.callback;

import com.gionee.hotspottransmission.bean.FileInfo;
import java.util.List;

/**
 * Created by luorw on 4/26/16.
 */
public interface IDataQueryCallBack {
    void onQueryEnd(List<FileInfo> fileList);
}
