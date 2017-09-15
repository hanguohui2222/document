package com.gionee.hotspottransmission.callback;

/**
 * Created by luorw on 2016/5/9 0009.
 */
public interface ITransferListener {
    void onCancelByIndex(int index);
    void onCancelAll(int currentTransferIndex);
    void onTransferCompleteByIndex(int index);
    void onTransferAllComplete();
    void onReadFileListSuccess();
    void onUpdateTransferProgress(int index);
}
