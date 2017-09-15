package com.gionee.hotspottransmission.callback;

import com.gionee.hotspottransmission.bean.FileInfo;

/**
 * Created by luorw on 4/27/16.
 */
public interface ISelectSortFiles {
    void selectedItem(boolean isSelected,FileInfo info);
    void selectedAll(boolean isSelected);
}
