package com.zerek.ABC.act;


import android.app.Activity;
import android.net.Uri;
import android.widget.Gallery;
import android.widget.ImageView;

import java.io.File;

public class GalleryItem {
    public final ImageView icon;
    private final File fImg;
    public final File sound;

    public GalleryItem(Activity activity, File fIcon, File fImg, File fSound, int itemBackground) {
        this.fImg = fImg;
        this.sound = fSound;

        this.icon = new ImageView(activity);
        Uri uri = Uri.fromFile(fIcon);
//            MainActivity.debug(uri.toString());
//            try {
        this.icon.setImageURI(uri);
//            } catch (Exception e) {
//               e.printStackTrace();
//
//            }

//            this.icon.setImageBitmap(BitmapFactory.decodeFile(fIcon.getAbsolutePath()));
        this.icon.setScaleType(ImageView.ScaleType.FIT_XY);
        this.icon.setLayoutParams(new Gallery.LayoutParams(getPx(activity, 100), getPx(activity, 80)));
        this.icon.setBackgroundResource(itemBackground);
    }

    public int getPx(Activity activity, int dimensionDp) {
        float density = activity.getResources().getDisplayMetrics().density;
        return (int) (dimensionDp * density + 0.5f);
    }

    public void setBitmap(ImageView imageView) {
        if (imageView != null) {
//                imageView.destroyDrawingCache();
            Uri uri = Uri.fromFile(fImg);
            imageView.setImageURI(uri);
//                imageView.setImageBitmap(BitmapFactory.decodeFile(fImg.getAbsolutePath()));
        }
    }
}