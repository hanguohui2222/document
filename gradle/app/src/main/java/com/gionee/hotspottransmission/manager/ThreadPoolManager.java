package com.gionee.hotspottransmission.manager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by luorw on 8/4/17.
 */
public class ThreadPoolManager {
    private static ThreadPoolManager mThreadPoolManager;
    private ExecutorService mExecutorService;

    private ThreadPoolManager() {

    }

    public synchronized static ThreadPoolManager getInstance() {
        if (mThreadPoolManager == null) {
            mThreadPoolManager = new ThreadPoolManager();
        }
        return mThreadPoolManager;
    }

    public void executeRunnable(Runnable runnable){
        if (mExecutorService == null){
            mExecutorService = Executors.newCachedThreadPool();
        }
        mExecutorService.execute(runnable);
    }
}
