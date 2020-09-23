package com.zerek.ABC.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import com.zerek.ABC.MainActivity;
import com.zerek.ABC.R;
import com.zerek.ABC.log.task.FileTask;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: mode
 * Date: 9/29/13
 * Time: 12:35 PM
 * For alerts
 */

public class Tools {
    public static Dialog createDialog(Activity activity, String sName, int title, DialogInterface.OnClickListener okClick) {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle(String.format(activity.getResources().getString(title), sName));
//        alertDialogBuilder.setMessage("Are you sure?");

        //null should be your on click listener
        alertDialogBuilder.setPositiveButton(R.string.alert_dialog_ok, okClick);
        alertDialogBuilder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return alertDialogBuilder.create();
    }

    private static File getFile(ContextWrapper context, String sName) {
        String sPath = FileTask.getCacheFolder(context);
        return new File(sPath + sName);
    }

    public static void objectSerSave(Object obj, ContextWrapper context, String sFileName) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getFile(context, sFileName)));
            oos.writeObject(obj);
            oos.flush();
            oos.close();
        } catch (Exception ex) {
            MainActivity.debug(sFileName + " serialization Save Error : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static Object objectSerLoad(ContextWrapper context, String sFileName) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getFile(context, sFileName)));
            return ois.readObject();
        } catch (Exception ex) {
            MainActivity.debug(sFileName + "serialization Read Error : " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }
}