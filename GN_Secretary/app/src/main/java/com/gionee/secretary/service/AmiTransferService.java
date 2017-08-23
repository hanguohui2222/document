package com.gionee.secretary.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.gionee.dataghost.plugin.aidl.IDataGhostService;
import com.gionee.dataghost.plugin.vo.FileInfo;
import com.gionee.dataghost.plugin.vo.PathInfo;
import com.gionee.dataghost.plugin.vo.PluginInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ami换机传输系统应用的数据接口实现
 * Created by luorw on 8/4/16.
 */
public class AmiTransferService extends Service {
    private List<FileInfo> mFileInfoList = new ArrayList<>();
    private long mFileSize;
    private String mLibsFile =
            "/data/data/com.gionee.secretary/databases/secretary_db," +
                    "/data/data/com.gionee.secretary/shared_prefs," +
                    "/data/data/com.gionee.secretary/cache," +
                    "/storage/emulated/0/secretary/noteImage";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    IBinder mBinder = new IDataGhostService.Stub() {

        /**获取插件信息，用于ami换机页面展示使用
         * @return
         * @throws RemoteException
         */
        @Override
        public PluginInfo getPluginInfo() throws RemoteException {
            Log.i("luorw", "getPluginInfo---------------");
            PluginInfo pluginInfo = new PluginInfo();
            pluginInfo.setDescription("");
            pluginInfo.setName("商务秘书");
            pluginInfo.setSize(getAllFileSize());
            return pluginInfo;
        }

        /**准备工作在此处实现，与getFileInfo()方法调用是同步的，所以不要在此方法中另起线程
         * @return
         * @throws RemoteException
         */
        @Override
        public boolean prepare() throws RemoteException {
            return false;
        }

        /**得到文件信息，只能是文件或目录
         * @return
         * @throws RemoteException
         */
        @Override
        public List<FileInfo> getFileInfo() throws RemoteException {
            mFileInfoList.clear();
            String[] filePaths = mLibsFile.split(",");
            addFileInfoList(filePaths[0]);
            File sharedPrefsFile = new File(filePaths[1]);
            if (sharedPrefsFile.exists() && sharedPrefsFile.isDirectory()) {
                File[] files = sharedPrefsFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    addFileInfoList(files[i].getAbsolutePath());
                }
            }
            File cacheFile = new File(filePaths[2]);
            if (cacheFile.exists() && cacheFile.isDirectory()) {
                File[] files = cacheFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    addFileInfoList(files[i].getAbsolutePath());
                }
            }
            File imageFile = new File(filePaths[3]);
            if (imageFile.exists() && imageFile.isDirectory()) {
                File[] files = imageFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    addFileInfoList(files[i].getAbsolutePath());
                }
            }
            return mFileInfoList;
        }

        private void addFileInfoList(String path) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setPath(path);
            fileInfo.setFile(true);
            fileInfo.setSystemData(true);
            mFileInfoList.add(fileInfo);
        }

        /**旧手机端发送完成后调用此方法，可在此方法中删除文件等
         * @throws RemoteException
         */
        @Override
        public void handleSendCompleted() throws RemoteException {
        }

        /**恢复数据，如果有兼容问题根据旧手机端的应用版本号versionCode来实现，跟restoreV2方法两个实现一个即可
         * @param list
         * @param l
         * @return
         * @throws RemoteException
         */
        @Override
        public boolean restore(List<String> list, long l) throws RemoteException {
            return false;
        }

        /**恢复数据，跟restore方法两个实现一个即可
         * @param list
         * @param l
         * @return
         * @throws RemoteException
         */
        @Override
        public boolean restoreV2(List<PathInfo> list, long l) throws RemoteException {
            Log.i("luorw", "restoreV2---------------oldVersion = " + l + " , currentVersion = " + getVersionCode());
//            if(l > getVersionCode()){
//                //暂不支持高版本的数据向低版本的传输
//                return false;
//            }
            for (int i = 0; i < list.size(); i++) {
                PathInfo pathInfo = list.get(i);
                Log.i("luorw", i + " :ReceivePath = " + pathInfo.getReceivePath() + " ,SendPath = " + pathInfo.getSendPath());
                copyFile(pathInfo.getReceivePath(), pathInfo.getSendPath());
            }
            Process.killProcess(Process.myPid());//传输完成需要杀进程
            return false;
        }

        /**新手机恢复完成后调用此方法，可在此方法中删除amihuanji下的数据，但是只能删除自己的数据
         * @throws RemoteException
         */
        @Override
        public void handleRestoreCompleted() throws RemoteException {
        }
    };


    private int getVersionCode() {
        PackageManager packageManager = this.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private long getAllFileSize() {
        String[] files = mLibsFile.split(",");
        mFileSize = 0;
        for (int i = 0; i < files.length; i++) {
            Log.i("luorw", "getAllFileSize--------file-------" + files[i]);
            File file = new File(files[i]);
            if (file.exists()) {
                mFileSize = mFileSize + file.length();
            }
        }
        Log.i("luorw", "getAllFileSize---------------" + mFileSize);
        return mFileSize;
    }

    /**
     * 拷贝文件
     *
     * @param amiReceivePath String 原文件路径
     * @param savePath       String 复制后路径
     */
    private void copyFile(String amiReceivePath, String savePath) {
        Log.i("luorw", "copyFile,amiReceivePath-----" + amiReceivePath + " , savePath = " + savePath);
        File receiveFile = new File(amiReceivePath);
        if (receiveFile.exists()) {
            Log.i("luorw", "copyFile," + amiReceivePath + " exists ");
            if (receiveFile.isDirectory()) {
                File[] files = receiveFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    String filePath = files[i].getAbsolutePath();
                    Log.i("luorw", "filePath = " + filePath);
                    deleteOldFile(getFilePathFromDir(amiReceivePath, filePath));
                    writeFileStream(files[i], filePath, getFilePathFromDir(savePath, filePath));
                }
            } else {
                deleteOldFile(savePath);
                writeFileStream(receiveFile, amiReceivePath, savePath);
            }
        }
    }

    private String getFilePathFromDir(String dir, String path) {
        String[] str = path.split("/");
        int index = str.length - 1;
        String oldPath = dir + "/" + str[index];
        Log.i("luorw", "getFilePathFromDir , " + oldPath);
        return oldPath;
    }

    private void deleteOldFile(String savePath) {
        File oldFile = new File(savePath);
        if (oldFile.exists()) {
            Log.i("luorw", "deleteOldFile," + savePath + " exists ");
            if (savePath.contains("databases")) {
                SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(oldFile, null);
                if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                    sqLiteDatabase.close();
                }
            }
            oldFile.delete();
        } else {
            //一开始没有db的情况下，要先创建databases目录
            if (savePath.contains("databases")) {
                File dir = new File("/data/data/com.gionee.secretary/databases");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            } else if (savePath.contains("shared_prefs")) {
                File dir = new File("/data/data/com.gionee.secretary/shared_prefs");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }
        }
    }

    private void writeFileStream(File receiveFile, String amiReceivePath, String savePath) {
        try {
            Log.i("luorw", "writeFileStream , " + amiReceivePath + " exists ");
            int readIn = 0;
            InputStream inputStream = new FileInputStream(amiReceivePath);
            FileOutputStream fs = new FileOutputStream(savePath);
            byte[] buffer = new byte[1024];
            while ((readIn = inputStream.read(buffer)) != -1) {
                fs.write(buffer, 0, readIn);
            }

            inputStream.close();
        } catch (IOException e) {
            Log.i("luorw", "copyFile,Exception-----" + e.getMessage());
            e.printStackTrace();
        }
        //删掉接收到的数据文件，以防再次传输时，相同的文件会重命名
        receiveFile.delete();
    }
}
