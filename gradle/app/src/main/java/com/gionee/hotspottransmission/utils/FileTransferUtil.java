package com.gionee.hotspottransmission.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.format.DateFormat;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Patterns;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.gionee.hotspottransmission.R;
import com.gionee.hotspottransmission.bean.FileInfo;
import com.gionee.hotspottransmission.callback.IDeviceCallBack;
import com.gionee.hotspottransmission.callback.IMultiDeviceCallBack;
import com.gionee.hotspottransmission.constants.Constants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import amigoui.app.AmigoAlertDialog;

/**
 * Created by luorw on 4/28/16.
 */
public class FileTransferUtil {
    private static final long UNAVAILABLE = -1L;
    public static String[] getFileNamesFromURI(Context context, ArrayList<Uri> contentUris, String type) {
        LogUtil.i("getFileNamesFromURI : contentUris = " + contentUris);
        if (contentUris == null || contentUris.size() <= 0) {
            return null;
        }

        String[] fileNames = new String[contentUris.size()];
        for (int i = 0; i < contentUris.size(); i++) {
            if(contentUris.get(i) == null){
                return null;
            }
            String uriStr = Uri.decode(contentUris.get(i).toString());
            String filePath = null;
            if (uriStr.startsWith("content:")) {
                if (type != null && type.equalsIgnoreCase("text/x-vcard")) {
                    filePath = getVCardFileName();
                }
                else {
                    filePath = getRealPathFromURI(context, contentUris.get(i));
                    LogUtil.i("getFileNamesFromURI : filePath = "+filePath);
                }
            } else if (uriStr.startsWith("file:")) {
                filePath = uriStr;
            }
            if(filePath != null){
                fileNames[i] = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
            }
        }
        return fileNames;
    }

    public static String[] downLoadFileName;
    public static long[] downLoadFileSize;
    /**获取下载管理的文件名和文件大小
     * @param context
     * @param uris
     */
    public static void getFileDataFromDownLoad(Context context,ArrayList<Uri> uris){
        downLoadFileName = new String[uris.size()];
        downLoadFileSize = new long[uris.size()];
        for(int i=0 ; i<uris.size() ; i++){
            Uri uri = uris.get(i);
            Cursor metadataCursor;
            try {
                metadataCursor = context.getContentResolver().query(uri, new String[] {
                        OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
                }, null, null, null);
            } catch (SQLiteException e) {
                // some content providers don't support the DISPLAY_NAME or SIZE columns
                metadataCursor = null;
            }
            if (metadataCursor != null) {
                try {
                    if (metadataCursor.moveToFirst()) {
                        downLoadFileName[i] = metadataCursor.getString(0);
                        downLoadFileSize[i] = metadataCursor.getLong(1);
                    }
                } finally {
                    metadataCursor.close();
                }
            }
            if (downLoadFileName[i] == null) {
                // use last segment of URI if DISPLAY_NAME query fails
                downLoadFileName[i] = uri.getLastPathSegment();
            }
            LogUtil.i("getFileDataFromDownLoad : fileName = "+downLoadFileName[i] + ",size = "+downLoadFileSize[i]);
        }
    }

    private static String getVCardFileName() {
        final String fileExtension = ".vcf";
        // base on time stamp
        String name = DateFormat.format("yyyyMMdd_hhmmss", new Date(System.currentTimeMillis())).toString();
        name = name.trim();
        return name + fileExtension;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        // can post image
        String[] proj = new String[1];
        proj[0] = MediaStore.Images.Media.DATA;
        Cursor cursor = null;
        String fileRealPath = "";
        try {
            cursor = context.getContentResolver().query(contentUri, proj, // Which columns to return
                    null, // WHERE clause; which rows to return (all rows)
                    null, // WHERE clause selection arguments (none)
                    null); // Order-by clause (ascending by name)
            if (cursor == null) {
                return "unknow name";
            }
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            fileRealPath = cursor.getString(column_index);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("Exception: " + e.toString());
        } finally {
            if(cursor != null)
                cursor.close();
        }
        return fileRealPath;
    }

    public static Uri creatFileForSharedContent(Context context, CharSequence shareContent) {

        if (shareContent == null) {
            return null;
        }

        Uri fileUri = null;
        FileOutputStream outStream = null;
        try {
            String fileName = context.getString(R.string.wifi_share_file_name) + ".html";
            context.deleteFile(fileName);

            /*
             * Convert the plain text to HTML
             */
            StringBuffer sb = new StringBuffer("<html><head><meta http-equiv=\"Content-Type\""
                    + " content=\"text/html; charset=UTF-8\"/></head><body>");
            // Escape any inadvertent HTML in the text message
            String text = escapeCharacterToDisplay(shareContent.toString());

            // Regex that matches Web URL protocol part as case insensitive.
            Pattern webUrlProtocol = Pattern.compile("(?i)(http|https)://");

            Pattern pattern = Pattern.compile("("
                    + Patterns.WEB_URL.pattern() + ")|("
                    + Patterns.EMAIL_ADDRESS.pattern() + ")|("
                    + Patterns.PHONE.pattern() + ")");
            // Find any embedded URL's and linkify
            Matcher m = pattern.matcher(text);
            while (m.find()) {
                String matchStr = m.group();
                String link = null;

                // Find any embedded URL's and linkify
                if (Patterns.WEB_URL.matcher(matchStr).matches()) {
                    Matcher proto = webUrlProtocol.matcher(matchStr);
                    if (proto.find()) {
                        // This is workForScan around to force URL protocol part be lower case,
                        // because WebView could follow only lower case protocol link.
                        link = proto.group().toLowerCase(Locale.US) +
                                matchStr.substring(proto.end());
                    } else {
                        // Patterns.WEB_URL matches URL without protocol part,
                        // so added default protocol to link.
                        link = "http://" + matchStr;
                    }

                    // Find any embedded email address
                } else if (Patterns.EMAIL_ADDRESS.matcher(matchStr).matches()) {
                    link = "mailto:" + matchStr;

                    // Find any embedded phone numbers and linkify
                } else if (Patterns.PHONE.matcher(matchStr).matches()) {
                    link = "tel:" + matchStr;
                }
                if (link != null) {
                    String href = String.format("<a href=\"%s\">%s</a>", link, matchStr);
                    m.appendReplacement(sb, href);
                }
            }
            m.appendTail(sb);
            sb.append("</body></html>");

            byte[] byteBuff = sb.toString().getBytes();

            outStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            if (outStream != null) {
                outStream.write(byteBuff, 0, byteBuff.length);
                fileUri = Uri.fromFile(new File(context.getFilesDir(), fileName));
                if (fileUri != null) {
                    LogUtil.d("Created one file for shared content: " + fileUri.toString());
                }
            }
        } catch (FileNotFoundException e) {
            LogUtil.e("FileNotFoundException: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            LogUtil.e("IOException: " + e.toString());
        } catch (Exception e) {
            LogUtil.e("Exception: " + e.toString());
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileUri;
    }
    private static String escapeCharacterToDisplay(String text) {

        Pattern pattern = Pattern.compile("[<>&]| {2,}|\r?\n");
        Matcher match = pattern.matcher(text);

        if (match.find()) {
            StringBuilder out = new StringBuilder();
            int end = 0;
            do {
                int start = match.start();
                out.append(text.substring(end, start));
                end = match.end();
                int c = text.codePointAt(start);
                if (c == ' ') {
                    // Escape successive spaces into series of "&nbsp;".
                    for (int i = 1, n = end - start; i < n; ++i) {
                        out.append("&nbsp;");
                    }
                    out.append(' ');
                } else if (c == '\r' || c == '\n') {
                    out.append("<br>");
                } else if (c == '<') {
                    out.append("&lt;");
                } else if (c == '>') {
                    out.append("&gt;");
                } else if (c == '&') {
                    out.append("&amp;");
                }
            } while (match.find());
            out.append(text.substring(end));
            text = out.toString();
        }
        return text;
    }

    public static String[] getFilePathsFromURI(Context context, ArrayList<Uri> contentUris, String type) {
        if (contentUris == null || contentUris.size() <= 0) {
            return null;
        }
        String[] filePaths = new String[contentUris.size()];
        for (int i = 0; i < contentUris.size(); i++) {
            String uriStr = Uri.decode(contentUris.get(i).toString());
            if (uriStr.startsWith("content:")) {
                //modified by luorw for GNSPR #23587 begin
                if (type != null && type.equalsIgnoreCase("text/x-vcard")) {
                    filePaths[i] = getVCardFileName();
                }else{
                    filePaths[i] = getRealPathFromURI(context, contentUris.get(i));
                }
                //modified by luorw for GNSPR #23587 end
            } else if (uriStr.startsWith("file:")) {
                filePaths[i] = uriStr.substring("file://".length(), uriStr.length());
            }
        }

        return filePaths;
    }

    public static long[] getFileSizesFromURI(Context context, ArrayList<Uri> contentUris, String type) {
        if (contentUris == null || contentUris.size() <= 0) {
            return null;
        }

        long[] fileSizes = new long[contentUris.size()];
        for (int i = 0; i < contentUris.size(); i++) {
            String uriStr = Uri.decode(contentUris.get(i).toString());
            LogUtil.i("getFileSizesFromURI------uriStr = "+uriStr+" ,type = "+type);
            if (uriStr.startsWith("content:")) {
                long fileSize = 0;
                if (type != null && type.equalsIgnoreCase("text/x-vcard")) {
                    fileSize = getVardFileSize(context, contentUris.get(i));
                }else{
                    fileSize = getRealSizeFromURI(context, contentUris.get(i));
                }
                fileSizes[i] = fileSize;
            } else if (uriStr.startsWith("file:")) {
                File file = new File(uriStr.substring("file://".length()));
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    fileSizes[i] = fileInputStream.available();
                    fileInputStream.close();
                    LogUtil.i("uriStr.startsWith file------fileSizes = "+fileSizes[i]+" ,file.length() = "+file.length());
                    //Gionee added by luorw for GNSPR #7853 20160304 begin
                    if(fileSizes[i] == 0){
                        fileSizes[i] = file.length();
                        LogUtil.i("fileSizes[i] = "+fileSizes[i]);
                    }
                    //Gionee added by luorw for GNSPR #7853 20160304 end
                } catch (FileNotFoundException e) {
                    LogUtil.i("getFileSizesFromURI------FileNotFoundException = "+e.getMessage());
                    e.printStackTrace();
                } catch (IOException e) {
                    LogUtil.i("getFileSizesFromURI------IOException = "+e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return fileSizes;
    }

    private static long getVardFileSize(Context context, Uri uri) {
        if (uri == null) {
            return 0;
        }

        long fileSize = 0;
        final String filename = getVCardFileName();
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = context.getContentResolver().openInputStream(uri);
                out = context.openFileOutput(filename, Context.MODE_PRIVATE);
                byte[] buf = new byte[8096];
                int size = 0;
                while ((size = in.read(buf)) != -1) {
                    out.write(buf, 0, size);
                    fileSize += size;
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileSize;
    }

    private static int getRealSizeFromURI(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.SIZE };
        Cursor cursor = null;
        int fileRealSize = 0;
        try {
            cursor = context.getContentResolver().query(contentUri, proj, // Which columns to return
                    null, // WHERE clause; which rows to return (all rows)
                    null, // WHERE clause selection arguments (none)
                    null); // Order-by clause (ascending by name)

            if (cursor == null) {
                return 0;
            }
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            cursor.moveToFirst();
            fileRealSize = cursor.getInt(column_index);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return fileRealSize;
    }

    public static int getFileTypeFromFileName(String filePath) {
        String fileMimeType = getFileMimeType(filePath);
        return getFileTypeFromMimeType(fileMimeType);
    }


    public static int getFileTypeFromMimeType(String fileMimeType){
        int fileType = 0;
        LogUtil.i("fileMimeType = "+fileMimeType);
        //added by luorw for GNSPR #24388 begin
        if(fileMimeType == null || "".equals(fileMimeType)){
            return Constants.TYPE_FILE;
        }
        //added by luorw for GNSPR #24388 end
        if (fileMimeType.startsWith("image/")) {
            fileType = Constants.TYPE_IMAGE;
        } else if (fileMimeType.startsWith("audio/")) {
            fileType = Constants.TYPE_MUSIC;
        } else if (fileMimeType.startsWith("video/")) {
            fileType = Constants.TYPE_VIDEO;
        } else if (fileMimeType.startsWith("text/")) {
            fileType = Constants.TYPE_FILE;
        }//added by luorw for GNSPR #32680 #48838 #52636 #52616 20161102 begin
        else if(fileMimeType.equals("application/vnd.android.package-archive")){
            fileType = Constants.TYPE_APPS;
        }else {
            fileType = Constants.TYPE_FILE;
        }
        //added by luorw for GNSPR #32680 #48838 #52636 #52616 20161102 end
        return fileType;
    }

    public static String getFileMimeType(String filePath) {
        LogUtil.i("getFileMimeType, filePath= "+filePath);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String mime = "text/plain";
        String[] filePathSplit = filePath.split("/");
        String fileName = filePathSplit[filePathSplit.length-1];
        String extension = getFileExtension(fileName);
        LogUtil.i("extension = "+extension);
        //added by luorw for GNSPR #45807 20160907 begin
        if("apk".equals(extension)){
            mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if(mime == null){
                mime = "text/plain";
            }
            LogUtil.i("apk mimeType = "+mime);
            return mime;
        }
        //added by luorw for GNSPR #45807 20160907 end
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath);
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                return mime;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                return mime;
            } catch (RuntimeException e) {
                e.printStackTrace();
                mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                return mime;
            }
        }
        Log.i("luorw","mimeType = "+mime);
        return mime;
    }

    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        String extension = null;

        if ((lastDot > 0) && (lastDot < fileName.length() - 1)) {
            extension = fileName.substring(lastDot + 1).toLowerCase();
        }
        return extension;
    }


    public static boolean getAvailableStorage(Context context, long fileSize){
        LogUtil.i("getAvailableStoragePath------------");
        String root_dir = Environment.getExternalStorageDirectory().toString();
        if (getVolumeSpace(root_dir) > fileSize){
            return true;
        }
        return false;
    }

    private static long getVolumeSpace(String volumePath) {
        String state = null;
        Class<?> c;
        try {
            c = Class.forName("android.os.storage.StorageManager");
            Method m = c.getMethod("getVolumeState", new Class[] {
                    String.class });
            state = (String) m.invoke(volumePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (Environment.MEDIA_MOUNTED.equals(state)) {
            try {
                StatFs stat = new StatFs(volumePath);
                return stat.getAvailableBlocks() * (long) stat.getBlockSize();
            } catch (Exception e) {
                LogUtil.i("Fail to access external storage,"+ e);
            }
//        }
        return UNAVAILABLE;
    }

    public static void showStorespaceFullDialog(Context context, final IDeviceCallBack deviceCallBack) {
        LogUtil.i("showStorespaceFullDialog---------");
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(context, R.style.AmigoDialogTheme);
        builder.setMessage(R.string.toast_storespace_full);
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deviceCallBack.onFullStorage();
                    }
                });
        AmigoAlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow()
                .setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    public static void showStorespaceFullDialog(Context context, final IMultiDeviceCallBack deviceCallBack) {
        LogUtil.i("showStorespaceFullDialog---------");
        AmigoAlertDialog.Builder builder = new AmigoAlertDialog.Builder(context, R.style.AmigoDialogTheme);
        builder.setMessage(R.string.toast_storespace_full);
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deviceCallBack.onFullStorage();
                    }
                });
        AmigoAlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow()
                .setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    public static void openFileFromUri(Context context,FileInfo fileInfo){
        String filePath = fileInfo.getFilePath();
        if (filePath != null) {
            String mimeType = FileTransferUtil.getFileMimeType(filePath);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), mimeType);
            try {
                context.startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                e.printStackTrace();
                String msg = context.getResources().getString(R.string.msg_unable_open_file);
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        }
    }

    public static String getStorageFileDir(Context context,FileInfo fileInfo){
        String dir = Environment.getExternalStorageDirectory() + "/" + context.getResources().getString(R.string.dir_name) + "/";
        switch (fileInfo.getFileType()){
            case Constants.TYPE_APPS:
                dir += Constants.DIR_APP+"/";
                break;
            case Constants.TYPE_IMAGE:
                dir += Constants.DIR_IMAGE+"/";
                break;
            case Constants.TYPE_MUSIC:
                dir += Constants.DIR_MUSIC+"/";
                break;
            case Constants.TYPE_VIDEO:
                dir += Constants.DIR_VIDEO+"/";
                break;
            case Constants.TYPE_FILE:
                dir += Constants.DIR_FILE+"/";
                break;
            default:
                break;
        }
        return dir;
    }

    public static String getStorageFileDirByType(Context context,int type){
        String dir = Environment.getExternalStorageDirectory() + "/" + context.getResources().getString(R.string.dir_name) + "/";
        switch (type){
            case Constants.TYPE_APPS:
                dir += Constants.DIR_APP;
                break;
            case Constants.TYPE_IMAGE:
                dir += Constants.DIR_IMAGE;
                break;
            case Constants.TYPE_MUSIC:
                dir += Constants.DIR_MUSIC;
                break;
            case Constants.TYPE_VIDEO:
                dir += Constants.DIR_VIDEO;
                break;
            case Constants.TYPE_FILE:
                dir += Constants.DIR_FILE;
                break;
            default:
                break;
        }
        return dir;
    }

    public static String getReceiveFilePath(Context context,FileInfo fileInfo) {
        String fileName = null;
        String dirPath = getStorageFileDir(context, fileInfo);
        if (null != dirPath) {
            try {
                File dirFile = new File(dirPath);
                File[] files = dirFile.listFiles();
                String realName = null;
                if (-1 == fileInfo.getFileName().lastIndexOf(".")) {
                    realName = fileInfo.getFileName();
                } else {
                    realName =  fileInfo.getFileName().substring(0, fileInfo.getFileName().lastIndexOf("."));
                }
                LogUtil.v("realName: " + realName);
                ArrayList<String> fileNames = getSameFileNames(files, realName);
                int num = -1;
                for (int i=0; i<fileNames.size(); i++) {
                    if (!fileNames.contains(String.valueOf(i + 1))) {
                        num = i+1;
                        break;
                    }
                }
                fileName = formatRecordFileName(num, fileInfo.getFileName());
                LogUtil.v("fileName: " + fileName);
                if(fileInfo.getFileType() == Constants.TYPE_APPS){
                    fileName = fileName+".apk";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dirPath + fileName;
    }

    private static ArrayList<String> getSameFileNames (File[] files, String name) {
        ArrayList<String> recordNames = new ArrayList<String>();
        if (null != files && files.length > 0) {
            String str = "";
            String fileName = "";
            for (int i = 0; i< files.length; i++) {
                fileName = files[i].getName();

                if (fileName.lastIndexOf(".") != -1) {
                    fileName =  fileName.substring(0, fileName.lastIndexOf("."));
                }

                if (fileName.equals(name)) {
                    recordNames.add(" ");
                } else if (fileName.startsWith(name)) {
                    str = fileName.substring(name.length(), fileName.length()).trim();
                    if (str == null || str.length() <= 0) {
                        recordNames.add(" ");
                    } else if (str.startsWith("(") && str.endsWith(")")) {
                        recordNames.add(str.substring(1, str.length() - 1).trim());
                    }
                }
            }
        }
        return recordNames;
    }

    private static String formatRecordFileName(int num, String name) {
        String temp = "";
        if (num > 0) {
            temp = "(" + String.valueOf(num) + ")";
        }
        LogUtil.e("name:" + name + " tmp" + temp);
        if (name.lastIndexOf(".") == -1) {
            return name + temp;
        } else {
            return name.substring(0, name.lastIndexOf(".")) + temp + name.substring(name.lastIndexOf("."));
        }
    }

    public static Map<String,List<FileInfo>> getReceiveFileByType(Context context,int type) {
        String dirPath = getStorageFileDirByType(context, type);
        Map<String,List<FileInfo>> map = new ArrayMap<>();
        if (null != dirPath) {
            try {
                File dirFile = new File(dirPath);
                File[] files = dirFile.listFiles();
                for(File file : files){

                    long dateLong = file.lastModified();
                    String dateStr = new SimpleDateFormat(Constants.DATE_FORMAT).format(new Date(dateLong));

                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setFileName(file.getName());
                    fileInfo.setUriString(Uri.fromFile(file).toString());
                    fileInfo.setFileSize(file.length());
                    fileInfo.setFileType(type);
                    fileInfo.setModifiedDate(dateLong);
                    fileInfo.setFilePath(file.getAbsolutePath());

                    if(map.containsKey(dateStr)){
                        List<FileInfo> fileInfoList = map.get(dateStr);
                        fileInfoList.add(fileInfo);
                    }else{
                        List<FileInfo> fileInfoList = new ArrayList<>();
                        fileInfoList.clear();
                        fileInfoList.add(fileInfo);
                        map.put(dateStr, fileInfoList);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**added by luorw for GNSPR #46111 20160907
     * @param path
     * @return
     */
    public static long getFileAvailableSize(String path){
        long size = 0;
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            size = fileInputStream.available();
            LogUtil.i("getFileAvailableSize = "+size);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }
}
