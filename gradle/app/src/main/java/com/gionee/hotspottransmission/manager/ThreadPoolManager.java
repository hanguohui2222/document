package com.gionee.hotspottransmission.manager;

import com.gionee.hotspottransmission.utils.LogUtil;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

//        Future future = mExecutorService.submit(runnable);
//        //如果任务结束执行则返回 null
//        try {
//            LogUtil.i("luorw ,mExecutorService, future.get() = " + future.get());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        mExecutorService.shutdown();
    }

    public void shutdown(){
        mExecutorService.shutdownNow();
    }
}
