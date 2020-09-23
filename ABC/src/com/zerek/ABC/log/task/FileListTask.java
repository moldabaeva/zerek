package com.zerek.ABC.log.task;

import android.app.Activity;
import com.zerek.ABC.MainActivity;
import com.zerek.ABC.act.CardItem;
import com.zerek.ABC.log.FileDesc;
import com.zerek.ABC.log.LoadHandler;
import com.zerek.ABC.log.WebLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

// Get all files
public class FileListTask extends GsonTask {
    // Folder data info (GET)
    private static final String C_ALL_FILES_JSON = "_all.json";
    protected final LoadHandler handler;

    private WebLoader webLoader;

    // Output
    String sFolderName;

    private int lAllSize;
    private Map<String, FileDesc> postPair;

    // Only folder in eng ?
    public FileListTask(WebLoader webLoader, String sFolderName, LoadHandler handler) {
        super(WebLoader.C_ROOT_URL + sFolderName + C_ALL_FILES_JSON, FileDesc[].class);
        this.webLoader = webLoader;
        this.sFolderName = sFolderName;
        this.handler = handler;

        init();
    }

    public void init(){
        lAllSize = 0;
        postPair = new HashMap<>();
    }

    public long loadSize() {
        return lAllSize;
    }

    public int loadCnt() {
        return postPair.size();
    }

    private FileDesc[] getNewWebFileDescs() {
        return (FileDesc[]) gson;
    }

    public void startNewWebTasks(String sPostFix) {
        for (FileDesc fileDesc : getNewWebFileDescs()) {
            if (sPostFix != null && !fileDesc.name.endsWith(sPostFix))
                continue;

            // Get file content itself
            webLoader.addTask(new FileTask(fileDesc, handler));
        }

        webLoader.doNotify();
    }

    @Override
    public void run() {
        MainActivity.debug("File list loaded " + sFolderName);
    }

    public void checkUpToDate(Activity activity, CardItem cardItem) {
        init();

        HashMap<Object, Object> sdCardFiles = new HashMap<>();
        String[] arrFiles = new File(FileTask.getCacheFolder(activity) + cardItem.getWebFolder()).list();
        if (arrFiles != null)
            for (String sFile : arrFiles)
                sdCardFiles.put(sFolderName + sFile, true);

        for (FileDesc fileDesc : getNewWebFileDescs()) {
            // need to load
            if (!sdCardFiles.containsKey(fileDesc.name)) {
                // Add from web
                postPair.put(fileDesc.name, fileDesc);
                lAllSize += fileDesc.size;
            }
        }
    }
}