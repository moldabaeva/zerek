package com.zerek.ABC.log.task;


import android.app.Activity;
import android.content.ContextWrapper;
import android.os.Environment;
import com.zerek.ABC.MainActivity;
import com.zerek.ABC.log.FileDesc;
import com.zerek.ABC.log.LoadHandler;
import com.zerek.ABC.log.WebLoader;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileTask extends Task {
    // File name + file size
    public final FileDesc fileDesc;
    protected final LoadHandler handler;

    // Output
    public String filename;

    public FileTask(FileDesc fileDesc, LoadHandler handler) {
        super(WebLoader.C_ROOT_URL + fileDesc.name);
        this.fileDesc = fileDesc;
        this.handler = handler;
    }

    public static String getCacheFolder(ContextWrapper context) {
//            getApplicationContext
        String result;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            result = Environment.getExternalStorageDirectory() + "/abc_zerek/";
        else
            result = context.getCacheDir().toString() + "/";

        // Create sub folders
        WebLoader.makeDirs(new File(result));

        return result;
    }

    public static String getFileDate(File file) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date(file.lastModified()));
    }

    public static boolean isCached(String sLocalFolder, FileDesc fileDesc) {
        // Already downloaded
        File file = new File(sLocalFolder + fileDesc.name);

        // Check size and date
        return file.length() == fileDesc.size && Long.parseLong(getFileDate(file)) >= Long.parseLong(fileDesc.date);
    }

    @Override
    public void doHttpLoad(Activity activity) {
        // Full path
        filename = getCacheFolder(activity) + fileDesc.name;

        // Load from server or not
        if (isCached(getCacheFolder(activity), fileDesc)) {
            MainActivity.debug("Ok no load of " + filename);
            handler.checkProgress(0, fileDesc);
            activity.runOnUiThread(this);
            return;
        };

        // Load from web
        super.doHttpLoad(activity);
    }

    @Override
    protected void parse(InputStream inputStream) {
        try {
            // Create all folders
            WebLoader.makeDirs(new File(filename).getParentFile());

            OutputStream outputStream = new FileOutputStream(filename);

            int iBufferSize = 4096 * 4;
            byte[] buffer = new byte[iBufferSize];

            //Copy row by row
            int iLen;
//            if (handler != null) handler.init(fileDesc.size);
            while ((iLen = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, iLen);

//                   update
//                if (handler != null) handler.sendProgress(iLen);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            filename = "";
            showErrInfo(act, e.getLocalizedMessage());
        }

        if (handler != null)
            handler.checkProgress(0, fileDesc);
    }

    @Override
    public final void run() {
        MainActivity.debug("File loaded " + fileDesc);
    }
}