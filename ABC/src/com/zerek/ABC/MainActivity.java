package com.zerek.ABC;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.zerek.ABC.act.AllWebAct;
import com.zerek.ABC.act.CardItem;
import com.zerek.ABC.act.GalleryItem;
import com.zerek.ABC.log.FileDesc;
import com.zerek.ABC.log.LoadHandler;
import com.zerek.ABC.log.WebLoader;
import com.zerek.ABC.log.WebNotif;
import com.zerek.ABC.log.task.FileTask;
import com.zerek.ABC.log.task.GsonTask;
import com.zerek.ABC.log.task.MuzFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends SherlockListActivity {
    // TODO Полный пипец
    public static MainActivity cur;

    private ArrayList<CardItem> cardItems;
    private CardAdapter listAdapter;
    private MediaPlayer mp;

    private OpenGameTask gameTask;
    private Handler handler;

    // If newWebFileDescs is empty
    private boolean bSkip = false;
    private boolean bPaused;

    // Show gallery
    public static final int C_START_ACTIVITY = 1;
//    public static final int C_SHOW_LOAD_ERROR = 2;

    public static void prepWindow(Activity act, boolean bSetTheme, int iTitle) {
        act.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Volume up or down key
        act.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Set theme
        if (bSetTheme)
            act.setTheme(SettingsActivity.getTheme(act));

//        // Only portrait
//        act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SettingsActivity.setLang(act);
        if (iTitle != 0)
            act.setTitle(iTitle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        prepWindow(this, true, R.string.app_name);
        super.onCreate(savedInstanceState);

        // Delete previous
        // TODO delete_folder(WebLoader.FileTask.getCacheFolder(this) + "abc/databases/");
        MainActivity.cur = this;

        // Have this feature
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        // Load items
        listAdapter = new CardAdapter();
        setListAdapter(listAdapter);

        handler = new Handler() {
            public void handleMessage(Message msg) {
                setSupportProgressBarIndeterminateVisibility(false);

                switch (msg.what) {
                    case C_START_ACTIVITY:
                        if (msg.obj == null) return;
                        Intent intent = new Intent(MainActivity.this, (Class) msg.obj);
                        startActivity(intent);
                        break;

//                    case C_SHOW_LOAD_ERROR:
//                        Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
//                        break;
                }
            }
        };
        // Set it off
        handler.sendEmptyMessage(0);

        // If need to update activity
        SettingsActivity.saveRefreshPos(this);
        SettingsActivity.setTilePerRow(this);

        // Load simple notification
        WebLoader webLoader = WebLoader.getInstance(this);
        webLoader.addTask(new GsonTask(WebLoader.C_ROOT_URL + "event.txt", WebNotif.Event.class, false) {
            @Override
            public void run() {
                if (gson == null)
                    return;
                WebNotif.Event ev = (WebNotif.Event) gson;
                if (!ev.compName.equals(""))
                    SettingsActivity.setCompName(MainActivity.this, ev.compName);

                // For another app
                if (!ev.project.equals("") && !WebLoader.C_ROOT_URL.startsWith(ev.project)) // TODO C_PROJECT_PATH
                    return;

                // Already read notif
                if (SettingsActivity.getLastEvDate(MainActivity.this) >= ev.date)
                    return;
                SettingsActivity.setLastEvDate(MainActivity.this, ev.date);

                // Add notif
                WebNotif.display(new Intent(Intent.ACTION_VIEW, Uri.parse(ev.url)), MainActivity.this, "WebInfo", ev.ticker, ev.title, ev.text, null, true);
            }
        });
        webLoader.doNotify();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update
        if (SettingsActivity.checkRefreshPos(this, true))
            return;

        getListView().setDividerHeight(0);
        cardItems = AllWebAct.getCurrentCardItems(this);
        if ((cardItems == null || cardItems.size() == 0) && !bSkip) {
            startActivity(new Intent(this, AllWebAct.class));
        }
        bSkip = true;

        listAdapter.fillWithVisible();

        // Play muz
        bPaused = false;
        final Activity _this = this;
        new MuzFolder(WebLoader.getInstance(this), MuzFolder.C_MUZ_MAIN, new LoadHandler() {
            public void handleMessage(Message msg) {
                if (msg.what != LoadHandler.whatComplete)
                    return;

                if (bPaused)
                    return;

                FileDesc fileDesc = (FileDesc) msg.obj;
                String sFullPath = FileTask.getCacheFolder(_this) + fileDesc.name;
                musicPlay(sFullPath);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        bPaused = true;
        endOpenTask();
        musicStop();
    }

    // Delete file
    public static void delete_file(File file) {
        if (!file.delete())
            debug("Cannot delete " + file.getAbsolutePath());
    }

    public static void delete_folder(String sFolder) {
        String[] arrFiles = new File(sFolder).list();
        if (arrFiles != null)
            for (String sFile : arrFiles)
                delete_file(new File(sFolder, sFile));
        delete_file(new File(sFolder));
    }

    // Just for test
    public static void debug(String sMes) {
        if (BuildConfig.DEBUG)
            Log.d("!!!", sMes);
    }

    public static void showToast(Activity act, String sMes) {
        Toast.makeText(act, sMes, Toast.LENGTH_LONG).show();
        debug(sMes);
    }

    public void soundPlay(int ind, boolean bPlay) {
        GalleryItem galleryItem = GalleryLogic.galleryAdapter.getGalleryItem(ind);
        if (!bPlay)
            return;

        musicPlay(galleryItem);
    }

    public void musicPlay(String sPath) { //int resource
        musicStop();

        // Start music only if not disabled in preferences
        if (SettingsActivity.getMusic(this))
            try {
                mp = new MediaPlayer(); //.create(this, resource);
                mp.setDataSource(new FileInputStream(sPath).getFD());
                mp.prepare();
                mp.setLooping(true);
                mp.setVolume(0.3f, 0.3f);
                mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void musicPlay(GalleryItem galleryItem) {
        musicStop();

        if (!SettingsActivity.getImgSound(this) || galleryItem == null || galleryItem.sound == null)
            return;

        try {
            mp = new MediaPlayer();
            FileInputStream fis = new FileInputStream(galleryItem.sound);
            mp.setDataSource(fis.getFD());

            mp.prepare();
            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void musicStop() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        // Dark or light
        boolean isLight = SettingsActivity.getTheme(this) == R.style.Theme_Sherlock_Light;

        super.onCreateOptionsMenu(menu);
        getSupportMenuInflater().inflate(R.menu.card_menu, menu);

        MenuItem item = menu.findItem(R.id.menu_compose);
        item.setIcon(isLight ? R.drawable.ic_compose_inverse : R.drawable.ic_compose);

        item = menu.findItem(R.id.menu_holo);
        item.setIcon(isLight ? R.drawable.abs__ic_menu_moreoverflow_normal_holo_light : R.drawable.abs__ic_menu_moreoverflow_normal_holo_dark);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.menu_exit:
                endOpenTask();
                finish();
                return true;

            case R.id.menu_pref:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.menu_about:
                startActivity(new Intent(this, About.class));
                return true;

            case R.id.menu_share:
                startActivity(new Intent("android.intent.action.SEND")
                        .setType("text/plain").putExtra("android.intent.extra.TEXT",
                                "https://market.android.com/details?id=" + getPackageName()));
                return true;

            case R.id.menu_rate:
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "Couldn't launch the market", Toast.LENGTH_LONG).show();
                }

                return true;

            case R.id.menu_all:
                if (gameTask != null) {
                    return true;
                }

                startActivity(new Intent(this, AllWebAct.class));
                return true;
        }

        return false;
    }

    public void endOpenTask() {
        handler.sendEmptyMessage(0);

        if (gameTask != null)
            gameTask.interrupt();

        gameTask = null;
    }

    public void showActivity(Class cl, CardItem cardItem) {
        if (gameTask != null) {
            return;
        }

        gameTask = new OpenGameTask(cl, cardItem);
        setSupportProgressBarIndeterminateVisibility(true);
        gameTask.start();
    }

    class OpenGameTask extends Thread {
        private Class actCl;
        private CardItem cardItem;

        public OpenGameTask(Class cl, CardItem cardItem) {
            this.actCl = cl;
            this.cardItem = cardItem;
        }

        public void run() {
            try {

                // TODO show err
                if (cardItem.checkAllLoaded(MainActivity.this, true))
                    GalleryLogic.galleryAdapter = new GalleryAdapter(cardItem);
                else
                    showToast(MainActivity.this, getString(R.string.mes_arc_broken));
            } catch (Exception e) {
                showToast(MainActivity.this, e.toString());
                e.printStackTrace();
                return;
            }

            if (isInterrupted())
                actCl = null;
            handler.sendMessage(handler.obtainMessage(C_START_ACTIVITY, actCl));
        }
    }

    // Adapter 1
    class CardAdapter extends BaseAdapter { //ArrayAdapter<CardItem> {
        private final ArrayList<CardItem> arr;

        // Not hidden and not web items
        public void fillWithVisible() {
            arr.clear();
            if (cardItems != null)
                for (CardItem cardItem : cardItems)
                    if (!cardItem.hide)
                        arr.add(cardItem);

            notifyDataSetChanged();
        }

        public CardAdapter() {
//        super(activity, R.layout.card_row, new ArrayList<CardItem>());
            this.arr = new ArrayList<CardItem>();
        }

        @Override
        public int getCount() {
            return (int) Math.ceil((double) arr.size() / SettingsActivity.I_TILE_PER_ROW);
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position / SettingsActivity.I_TILE_PER_ROW;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CardHolder holder;
            if (convertView == null) {
                switch (SettingsActivity.I_TILE_PER_ROW) {
                    case 1:
                        holder = new HolderRow(MainActivity.this);
                        break;
                    case 2:
                        holder = new Holder2Tile(MainActivity.this);
                        break;

                    default:
                        return null;
                }

                convertView = holder.createView();
                convertView.setTag(holder);
            } else
                holder = (CardHolder) convertView.getTag();
            holder.updateInfo(position);

            return convertView;
        }


        // For optimization
        private abstract class CardHolder {
            protected MainActivity activity;

            public CardHolder(MainActivity activity) {
                this.activity = activity;
            }

            public abstract View createView();

            public abstract void updateInfo(int position);
        }

        private class HolderRow extends CardHolder implements View.OnClickListener {
            private CardItem cardItem;
            private TextView name;
            private TextView desc;
            private TextView auth;
            //            private TextView vers;
            private ImageView icon;

            public HolderRow(MainActivity activity) {
                super(activity);
            }

            @Override
            public View createView() {
                View cardView = activity.getLayoutInflater().inflate(R.layout.card_row, null, true);

                name = (TextView) cardView.findViewById(R.id.card_name);
                desc = (TextView) cardView.findViewById(R.id.card_desc);
                auth = (TextView) cardView.findViewById(R.id.card_auth);
//                vers = (TextView) cardView.findViewById(R.id.card_vers);
                icon = (ImageView) cardView.findViewById(R.id.card_icon);
                cardView.setOnClickListener(this);
                return cardView;
            }

            @Override
            public void updateInfo(int position) {
                cardItem = arr.get(position);
                name.setText(cardItem.name);
                desc.setText(cardItem.desc);
                auth.setText(String.format(activity.getString(R.string.Card_Author), cardItem.auth));
//                vers.setText(String.format(activity.getString(R.string.Card_Version), cardItem.file_vers));
                icon.setImageBitmap(cardItem.iconImg);
            }

            @Override
            public void onClick(View v) {
                activity.showActivity(SettingsActivity.getCardMode(), cardItem);
            }
        }

        private class Holder2Tile extends CardHolder implements View.OnClickListener {
            private CardItem cardItem_l;
            private CardItem cardItem_r;

            private TextView desc_l;
            private TextView desc_r;
            private ImageView icon_l;
            private ImageView icon_r;

            public Holder2Tile(MainActivity activity) {
                super(activity);
            }

            @Override
            public View createView() {
                View cardView = activity.getLayoutInflater().inflate(R.layout.card_2tile, null, true);

                desc_l = (TextView) cardView.findViewById(R.id.card_desc_l);
                desc_r = (TextView) cardView.findViewById(R.id.card_desc_r);
                icon_l = (ImageView) cardView.findViewById(R.id.card_icon_l);
                icon_r = (ImageView) cardView.findViewById(R.id.card_icon_r);

                LinearLayout l = (LinearLayout) cardView.findViewById(R.id.card_2tile_l);
                l.setOnClickListener(this);

                LinearLayout r = (LinearLayout) cardView.findViewById(R.id.card_2tile_r);
                r.setOnClickListener(this);
                return cardView;
            }

            @Override
            public void updateInfo(int position) {
                cardItem_l = arr.get(position * SettingsActivity.I_TILE_PER_ROW);
                desc_l.setText(cardItem_l.name);
                icon_l.setImageBitmap(cardItem_l.iconImg);

                int iInd = position * SettingsActivity.I_TILE_PER_ROW + 1;
                if (iInd >= arr.size()) {
                    cardItem_r = null;
                    desc_r.setText("");
                    icon_r.setImageBitmap(null);
                    return;
                }

                cardItem_r = arr.get(iInd);
                desc_r.setText(cardItem_r.name);
                icon_r.setImageBitmap(cardItem_r.iconImg);
            }

            @Override
            public void onClick(View v) {
                CardItem cardItem = null;
                switch (v.getId()) {
                    case R.id.card_2tile_l:
                        cardItem = cardItem_l;
                        break;
                    case R.id.card_2tile_r:
                        cardItem = cardItem_r;
                        break;
                }
                if (cardItem == null) return;

                activity.showActivity(SettingsActivity.getCardMode(), cardItem);
            }
        }
    }

    // Adapter 2
    public static class GalleryAdapter extends BaseAdapter {
        private final CardItem cardItem;

        public GalleryAdapter(CardItem cardItem) {
            this.cardItem = cardItem;
        }

        public int getDefaultIndex() {
            int iMax = getCount();
            return iMax / 2 - ((iMax / 2) % getTrueCount());
        }

        public int getTrueCount() {
            if (cardItem == null || cardItem.galleryItems == null)
                return 0;

            return cardItem.galleryItems.length;
        }

        public int getCount() {
            int iVal = (int) (getTrueCount() * 2.5);
            if (iVal < 75)
                iVal = 75;
            return iVal;
        }

        public Object getItem(int position) {
            return 0;
        }

        public long getItemId(int position) {
            return 0;
        }

        public GalleryItem getGalleryItem(int ind) {
            return cardItem.galleryItems[ind % cardItem.galleryItems.length];
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getGalleryItem(position).icon;
        }
    }

}