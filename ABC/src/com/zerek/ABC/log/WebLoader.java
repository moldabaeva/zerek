package com.zerek.ABC.log;

import android.app.Activity;
import com.zerek.ABC.MainActivity;
import com.zerek.ABC.log.task.Task;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * Date: 8/3/13
 * Time: 3:44 PM
 */

public class WebLoader extends Thread {
    // Singleton
    private static WebLoader instance;

    private final Activity activity;
    private final LinkedList<Task> tasks;

    //Paths
    public static final String C_ROOT_URL;

    static {
//        C_ROOT_URL = "http://192.168.0.113:8000/_res/";
        C_ROOT_URL = "https://raw.githubusercontent.com/moldabaeva/zerek/master/data/_res/";
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void makeDirs(File file) {
        file.mkdirs();
    }

    public static String escapeUrl(String sUrlStr) {
        try {
            return URLEncoder.encode(sUrlStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private WebLoader(Activity activity) {
        this.activity = activity;
        this.tasks = new LinkedList<Task>();
    }

    // Singleton
    public static WebLoader getInstance(Activity activity) {
        if (instance == null) {
            instance = new WebLoader(activity);
            instance.start();
        }
        return instance;
    }

    public static void doInterrupt(Task task) {
        if (instance == null)
            return;

        // Remove
        instance.interrupt();
        instance.tasks.remove(task);
        LinkedList<Task> prev = instance.tasks;

        // Restart
        instance = new WebLoader(instance.activity);
        instance.tasks.addAll(prev);
        instance.start();
    }

    public void addTask(Task task) {
        synchronized (tasks) {
            tasks.add(task);
        }
    }

    public void doNotify() {
        synchronized (tasks) {
            tasks.notifyAll();
        }
    }

    public void run() {

        //noinspection InfiniteLoopStatement
        while (true) {
            Task task;
            synchronized (tasks) {
                // Queue FIFO
                task = tasks.poll();

                if (task == null)
                    try {
                        tasks.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }

            // Load next
            if (task != null) {
                MainActivity.debug(task.sUrl);
                task.parent = this;
                task.doHttpLoad(activity);
            }
        }
    }
}
