package com.zerek.ABC.log.task;

import android.app.Activity;
import com.zerek.ABC.log.FileDesc;
import com.zerek.ABC.log.LoadHandler;
import com.zerek.ABC.log.WebLoader;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * Date: 10/5/13
 * Time: 11:15 AM
 * For downloading small files
 */

public class MuzFolder extends FileListTask {
    public static final String C_MUZ_MAIN = "main.mid";
    public static final String C_MUZ_BACK = "back.mid";

    private String sFileName;

    public MuzFolder(WebLoader webLoader, String sFileName, LoadHandler handler) {
        super(webLoader, "muz/", handler);
        this.sFileName = sFileName;

        // Go
        webLoader.addTask(this);
        webLoader.doNotify();
    }

    // Play music without internet
    @Override
    public void showErrInfo(final Activity act, String sMes) {
//        super.showErrInfo(act, sMes); No err

        String sPath = FileTask.getCacheFolder(act) + sFolderName;

        // Get any file from dir
        sPath += sFileName;
        File file = new File(sPath);
        if (!file.exists() || file.length() == 0)
            return;

        final FileDesc fileDesc = new FileDesc(sFolderName + sFileName, file.length(), FileTask.getFileDate(file));
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // And finally send
                handler.checkProgress(0, fileDesc);
            }
        });
    }

    @Override
    public void run() {
        super.run();
        startNewWebTasks(sFileName);
    }
}
