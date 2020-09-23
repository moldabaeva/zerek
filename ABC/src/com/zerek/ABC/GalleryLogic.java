package com.zerek.ABC;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Message;
import android.view.View;
import android.widget.Gallery;
import android.widget.ImageView;
import com.zerek.ABC.log.FileDesc;
import com.zerek.ABC.log.LoadHandler;
import com.zerek.ABC.log.WebLoader;
import com.zerek.ABC.log.task.FileTask;
import com.zerek.ABC.log.task.MuzFolder;

import java.io.FileInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: mode
 * Date: 7/8/13
 * Time: 6:59 PM
 */

public class GalleryLogic extends Thread {
    public static final Integer C_EVEN = -111;
    private static final String C_CURR_IND = "GALLERY_IND";

    // Appearance
    private Activity act;
    private Gallery gallery;
    public static MainActivity.GalleryAdapter galleryAdapter;

    // Slider
    private ImageView but_slide;
    private boolean bRun;
    private int iInterval;
    private GalleryMover galleryMover;

    // Music
    private MediaPlayer mp;
    private boolean bPaused;

    public GalleryLogic(final Activity act) {
        this.act = act;

        ////////////////////////////////
        // prepare image
        but_slide = (ImageView) act.findViewById(R.id.but_slide);
        but_slide.setImageResource(R.drawable.slide_start);
        but_slide.setAlpha(127);

        but_slide.setOnClickListener((new View.OnClickListener() {
            public void onClick(View view) {
                sliderRun(!bRun);
            }
        }));

        but_slide.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                act.startActivity(new Intent(act, SettingsActivity.class));
                return true;
            }
        });

        ////////////////////////////////
        // Prepare gallery
        this.gallery = (Gallery) act.findViewById(R.id.gallery1);

        // If need to update activity
        SettingsActivity.saveRefreshPos(act);

        // Select 1st item  //savedInstanceState
        int iPos = act.getIntent().getIntExtra(C_CURR_IND, ((MainActivity.GalleryAdapter) gallery.getAdapter()).getDefaultIndex());
        gallery.setSelection(iPos);
        gallery.getOnItemClickListener().onItemClick(null, null, iPos, gallery.getId());

        // Let's go
        galleryMover = new GalleryMover();
        start();
    }

    public static Gallery getGallery(Activity act) {
        Gallery gallery = (Gallery) act.findViewById(R.id.gallery1);
        gallery.setAdapter(galleryAdapter);

        return gallery;
    }

    public void sliderRun(boolean bRun) {
        this.bRun = bRun;
        if (bRun)
            but_slide.setImageResource(R.drawable.slide_stop);
        else
            but_slide.setImageResource(R.drawable.slide_start);
    }

    @Override
    public void run() {
        int iSleep = 750;
        int iSum = 0;
        int iSelPos;

        try {
            while (true) {
                iSelPos = gallery.getSelectedItemPosition();
                Thread.sleep(iSleep);

                if (bRun) {
//                    Move to next slide
                    iSum += iSleep;
                    if (iSum >= iInterval) {
                        iSum = 0;
                        galleryMover.showMove(gallery.getSelectedItemPosition() + 1, gallery.getId());
                    }
                } else {
                    iSum = 0;
//                    Check in center
                    if (iSelPos == gallery.getSelectedItemPosition())
                        galleryMover.showMove(iSelPos, C_EVEN);
                }
            }
        } catch (InterruptedException e) {

        }
    }

    private class GalleryMover implements Runnable {
        private int iPos;
        private int id;

        public void showMove(int iPos, int id) {
            this.iPos = iPos;
            this.id = id;
            act.runOnUiThread(this);
        }

        @Override
        public void run() {
            // Unreal
            MainActivity.GalleryAdapter adapter = (MainActivity.GalleryAdapter) gallery.getAdapter();

            int iMiddle = adapter.getDefaultIndex();
            int iTrueCount = adapter.getTrueCount();

            if (Math.abs(iPos - iMiddle) > iTrueCount || iPos == 0 || iPos == (adapter.getCount() - 1)) {
                iPos = iMiddle + iPos % iTrueCount;
            }

            if (iPos != gallery.getSelectedItemPosition())
                gallery.setSelection(iPos);

            // Second parameter what to do
            gallery.getOnItemClickListener().onItemClick(null, null, iPos, id);
        }
    }

    public void Resume() {
        // Update
        Class cl = SettingsActivity.getCardMode();
        if (SettingsActivity.checkRefreshPos(act, false) || !cl.equals(act.getClass())) {
            Intent intent = new Intent(act, cl); //act.getIntent()
            intent.putExtra(C_CURR_IND, gallery.getSelectedItemPosition());
            act.finish();
            act.startActivity(intent);
            return;
        }

        // Get slider interval
        iInterval = SettingsActivity.getSlideInterval(act) * 1000;
        Pause();

        // Start music only if not disabled in preferences
        bPaused = false;
        if (SettingsActivity.getBackMusic(act)) {
            // Play muz
            new MuzFolder(WebLoader.getInstance(act), MuzFolder.C_MUZ_BACK, new LoadHandler() {
                public void handleMessage(Message msg) {
                    if (msg.what != LoadHandler.whatComplete)
                        return;

                    if (bPaused)
                        return;

                    FileDesc fileDesc = (FileDesc) msg.obj;
                    String sFullPath = FileTask.getCacheFolder(act) + fileDesc.name;
                    try {
                        mp = new MediaPlayer(); //.create(act, R.raw.back);
                        mp.setDataSource(new FileInputStream(sFullPath).getFD());
                        mp.prepare();
                        mp.setLooping(true);
                        mp.setVolume(0.13f, 0.13f);
                        mp.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void Pause() {
        sliderRun(false);

        bPaused = true;
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }
}
