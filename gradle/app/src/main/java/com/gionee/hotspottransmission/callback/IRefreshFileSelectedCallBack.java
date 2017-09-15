package com.gionee.hotspottransmission.callback;

import com.gionee.hotspottransmission.bean.FileInfo;
import java.util.List;

/**
 * Created by luorw on 4/27/16.
 */
public interface IRefreshFileSelectedCallBack {
    void onRefreshCount(List<FileInfo> list);
    void onRefreshSelected(boolean isSelected);
    void onShowSelected(List<FileInfo> list);
}
