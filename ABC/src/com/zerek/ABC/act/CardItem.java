package com.zerek.ABC.act;


import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.widget.Toast;
import com.zerek.ABC.R;
import com.zerek.ABC.log.FileDesc;
import com.zerek.ABC.log.LoadHandler;
import com.zerek.ABC.log.WebLoader;
import com.zerek.ABC.log.task.FileListTask;
import com.zerek.ABC.log.task.FileTask;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

// What will load
public class CardItem implements Serializable {
    public String name;
    public String desc;
    public String auth;
    String db;
    int cnt;
    public boolean hide;

    // Icon
    FileDesc icon;
    public transient Bitmap iconImg;

    // Data
    public transient GalleryItem[] galleryItems;

    // For loading data
//    transient LoadHandler handler;
    transient FileListTask fileListTask = null;
    transient int progress = 0;

    public CardItem() {

    }

    public CardItem(String name, String desc, String auth, String db, int cnt, boolean hide, FileDesc icon) {
        this.name = name;
        this.desc = desc;
        this.auth = auth;
        this.db = db;
        this.cnt = cnt;
        this.hide = hide;
        this.icon = icon;
    }

    public String getWebFolder() {
        return AllWebAct.C_CARDS_FOLDER + db + "/";
    }

    // Shows load progress
    private LoadHandler getLoadHandler(final AllWebAct allWebAct) {
        return new LoadHandler() {

            public void handleMessage(Message msg) {

                if (msg.what != LoadHandler.whatComplete) {
                    // Show Изменение progress
                    progress = msg.arg1; // getRate();
                    allWebAct.webCardAdapter.notifyDataSetChanged();
                    return;
                }


                // msg.arg1 == C_WEB
//                        if (msg.arg2 > 0)
                else {
                    // Show Скрыть progress
                    progress = 0;
                    hide = false;
                    allWebAct.doSave();
                    // Congratulations!
                    Toast.makeText(allWebAct, String.format(allWebAct.getString(R.string.info_download_comp), name), Toast.LENGTH_SHORT).show();
                    fileListTask.init();
                }
            }
        };
    }

    //TODO FileListTask
    public void addFileListTask(final AllWebAct allWebAct) {
        final CardItem _this = this;

        WebLoader webLoader = WebLoader.getInstance(null);
        LoadHandler handler = getLoadHandler(allWebAct);
        fileListTask = new FileListTask(webLoader, getWebFolder(), handler) {
            @Override
            public void run() {
                super.run();
                fileListTask.checkUpToDate(allWebAct, _this);

                handler.checkProgress(fileListTask.loadSize(), null);

                // Show Нужна ли загрузка файлов
                allWebAct.webCardAdapter.notifyDataSetChanged();
            }
        };

        webLoader.addTask(fileListTask);
    }

    public boolean checkAllLoaded(Activity act, boolean bLoad) {
        String sPath = FileTask.getCacheFolder(act) + getWebFolder();

        // Prepare files map
        String[] arrFiles = new File(sPath).list();
        Map<String, String> mapFiles = new HashMap<>();
        if (arrFiles == null)
            return false;

        for (String sFile : arrFiles)
            mapFiles.put(sFile.substring(0, sFile.lastIndexOf('.')), sFile);

        // Load data
        int itemBackground = 0;

        if (bLoad) {
            galleryItems = new GalleryItem[cnt];
            TypedArray a = act.obtainStyledAttributes(R.styleable.Gallery1);
            itemBackground = a.getResourceId(R.styleable.Gallery1_android_galleryItemBackground, 0);
            a.recycle();
        }

        for (int i = 1; i <= cnt; i++) {
            File fIcon = getFileInfo(sPath, mapFiles.get(String.format("%03d", i) + "_ICON"));
            if (fIcon == null)
                return false;

            File fImg = getFileInfo(sPath, mapFiles.get(String.format("%03d", i) + "_IMG"));
            if (fImg == null)
                return false;

            File fSound = getFileInfo(sPath, mapFiles.get(String.format("%03d", i) + "_SOUND"));
            if (fSound == null)
                return false;

            if (bLoad)
                galleryItems[i - 1] = new GalleryItem(act, fIcon, fImg, fSound, itemBackground);
        }
        return true;
    }

    // Exist ?
    private File getFileInfo(String sPath, String sFile) {
        if (sFile == null)
            return null;
        File file = new File(sPath + sFile);
        if (!file.exists())
            return null;
        if (file.length() == 0)
            return null;
        return file;
    }

    public void loadIcon(String sPath) {
        iconImg = BitmapFactory.decodeFile(sPath);
    }
}
