package com.zerek.ABC.act;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.zerek.ABC.MainActivity;
import com.zerek.ABC.R;
import com.zerek.ABC.log.FileDesc;
import com.zerek.ABC.log.LoadHandler;
import com.zerek.ABC.log.WebLoader;
import com.zerek.ABC.log.task.FileTask;
import com.zerek.ABC.log.task.FileListTask;
import com.zerek.ABC.log.task.GsonTask;
import com.zerek.ABC.ui.Tools;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: mode
 * Date: 9/29/13
 * Time: 7:52 PM
 */

public class AllWebAct extends SherlockListActivity {
    public static final String C_CARDS_FOLDER = "cards/";
    private static final String C_CARD_ITEMS = "items.db";

    // Own vars
    WebCardAdapter webCardAdapter;
    private SherlockListActivity act;
    private Dialog diag;

    //Selected item
    private CardItem selCard;
    private static String sCardSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        act = this;
        MainActivity.prepWindow(act, true, 0);
        super.onCreate(savedInstanceState);

        // Format for number of MB
        sCardSize = getString(R.string.Card_New_Size);

        WebLoader webLoader = WebLoader.getInstance(null);
        webCardAdapter = new WebCardAdapter();
        setListAdapter(webCardAdapter);
        setSupportProgressBarIndeterminateVisibility(true);

        // Load newWebFileDescs data
        webLoader.addTask(new GsonTask(WebLoader.C_ROOT_URL +
                "cards/abcMain.json", CardItem[].class) {
            @Override
            public void run() {
                CardItem[] all = (CardItem[]) gson;
                for (CardItem cardItem : all)
                    webCardAdapter.add(cardItem);

                loadIcons(true);
            }

            @Override
            public void showErrInfo(final Activity act, final String sMes) {
                super.showErrInfo(act, sMes);
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadIcons(false);
                    }
                });
            }
        });
        webLoader.doNotify();
    }

    // Save to file
    void doSave() {
        webCardAdapter.notifyDataSetChanged();

        ArrayList<CardItem> arrCards = new ArrayList<CardItem>();

        for (int i = 0; i < webCardAdapter.getCount(); i++) {
            CardItem cardItem = webCardAdapter.getItem(i);
            if (cardItem.checkAllLoaded(this, false))
                arrCards.add(cardItem);
        }

        Tools.objectSerSave(arrCards, this, C_CARD_ITEMS);
    }

//    public static ArrayList<CardItem> tempInit(Activity act) {
//        ArrayList<CardItem> result = new ArrayList<CardItem>();
//
//        result.add(new CardItem("KZ - Әліппе", "Қазақ тілінің әліппесі", "Zh.Moldabayeva", "KZ_ABC", 42, false,
//                new FileDesc("icons/KZ_ABC.jpg", 42, "20200101000000")));
//
//        Tools.objectSerSave(result, act, C_CARD_ITEMS);
//        return result;
//    }

    public static ArrayList<CardItem> getCurrentCardItems(Activity act) {
        ArrayList<CardItem> result = (ArrayList<CardItem>) Tools.objectSerLoad(act, C_CARD_ITEMS);
        if (result == null || result.size() == 0) {
//            result = tempInit(act);
            return null;
        }
        String sCache = FileTask.getCacheFolder(act);
        for (CardItem cardItem : result)
            cardItem.loadIcon(sCache + cardItem.icon.name);
        return result;
    }

    //TODO loadicons
    private void loadIcons(boolean bListLoadedFromWeb) {
        setSupportProgressBarIndeterminateVisibility(false);

        // Fill with existed
        ArrayList<CardItem> cardItems = getCurrentCardItems(this);

        if (cardItems != null)
            for (CardItem item1 : cardItems) {
                boolean bFind = false;
                for (int i = 0; i < webCardAdapter.getCount(); i++) {
                    CardItem item2 = webCardAdapter.getItem(i);
                    if (item1.db.equals(item2.db)) {
                        bFind = true;
                        webCardAdapter.remove(item2);
                        webCardAdapter.insert(item1, i);
                        break;
                    }
                }

                // Old items or not connected?
                if (!bFind)
                    webCardAdapter.add(item1);
            }

        // Load icons 1-st task
        if (bListLoadedFromWeb) {
            final WebLoader webLoader = WebLoader.getInstance(this);

            // All icons
            FileListTask folderTask = new FileListTask(webLoader, "icons/", new LoadHandler() {

                public void handleMessage(Message msg) {
                    if (msg.what != LoadHandler.whatComplete)
                        return;

                    FileDesc fileDesc = (FileDesc) msg.obj;
                    for (int i = 0; i < webCardAdapter.getCount(); i++) {
                        CardItem cardItem = webCardAdapter.getItem(i);

                        if (cardItem.icon.name.equals(fileDesc.name)) {
                            String sFullPath = FileTask.getCacheFolder(act) + fileDesc.name;
                            cardItem.loadIcon(sFullPath);
                            webCardAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }) {
                @Override
                public void run() {
                    super.run();
                    startNewWebTasks(null);
                }
            };

            webLoader.addTask(folderTask);

            // Then check size tasks
            for (int i = 0; i < webCardAdapter.getCount(); i++) {
                CardItem cardItem = webCardAdapter.getItem(i);
                cardItem.addFileListTask(this);
            }
            // Go №1
            webLoader.doNotify();
        }

        // Show Список заполнился
        webCardAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        selCard = webCardAdapter.getItem(position); //(CardItem) l.getAdapter()
        // Instead of menu
        startActionMode(new WebActionMode(selCard));
        webCardAdapter.notifyDataSetChanged();
    }

    private void setDiag(Dialog dialog, int id) {
        diag = dialog;
        removeDialog(id);
        showDialog(id);
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        return diag;
    }

    class WebActionMode implements ActionMode.Callback {
        private final CardItem cardItem;

        private MenuItem mnHide;
        private MenuItem mnShow;
        private MenuItem mnDelete;
        private MenuItem mnCancel;
        private MenuItem mnDownload;

        public WebActionMode(CardItem cardItem) {
            this.cardItem = cardItem;
        }

        private void setHide(final boolean bHide) {
            int iTitle = bHide ? R.string.alert_hide : R.string.alert_show;

            setDiag(Tools.createDialog(act, cardItem.name, iTitle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Set
                    cardItem.hide = bHide;

                    // What update
                    doSave();
                }
            }), 1);
        }

        // Start load
        private void startDownload() {
            setDiag(Tools.createDialog(act, cardItem.name, R.string.alert_start_download, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cardItem.progress = 1;
                    cardItem.fileListTask.startNewWebTasks(null); // , cardItem.getLoadHandler()

//                    WebLoader webLoader = WebLoader.getInstance(null);
//                    webLoader.addTask(cardItem.folderTask);
//                    webLoader.doNotify();
                }
            }), 2);
        }

        private void deleteCard(final boolean bAsk) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!bAsk)
                        WebLoader.doInterrupt(cardItem.fileListTask);

                    MainActivity.delete_folder(FileTask.getCacheFolder(AllWebAct.this) + cardItem.getWebFolder());

                    // Delete
                    if (cardItem.fileListTask == null)
                        webCardAdapter.remove(cardItem);
                    else {
                        cardItem.addFileListTask(AllWebAct.this);
                        WebLoader.getInstance(AllWebAct.this).doNotify();
                    }
                    cardItem.progress = 0;

                    doSave();

                    // Show message
                    MainActivity.showToast(AllWebAct.this, AllWebAct.this.getString(bAsk ? R.string.mes_deleted : R.string.mes_canceled));
                }
            };

            if (bAsk)
                setDiag(Tools.createDialog(act, cardItem.name, R.string.alert_delete_card, listener), 3);
            else
                listener.onClick(null, 0);
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // getSupportMenuInflater().inflate(R.menu.web_menu, menu);
            mode.getMenuInflater().inflate(R.menu.web_menu, menu);
            mode.setTitle(cardItem.name);

            // Hide all buttons
            mnHide = menu.findItem(R.id.menu_web_hide).setVisible(false);
            mnShow = menu.findItem(R.id.menu_web_show).setVisible(false);
            mnDelete = menu.findItem(R.id.menu_web_delete).setVisible(false);
            mnCancel = menu.findItem(R.id.menu_web_cancel).setVisible(false);
            mnDownload = menu.findItem(R.id.menu_web_download).setVisible(false);

            // Up-to-date
            if (cardItem.fileListTask == null || cardItem.fileListTask.loadCnt() == 0) {
                mnDelete.setVisible(true);
                if (cardItem.hide)
                    mnShow.setVisible(true);
                else
                    mnHide.setVisible(true);
            } else {
                if (cardItem.progress > 0)
                    mnCancel.setVisible(true);
                else
                    mnDownload.setVisible(true);
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_web_hide:
                    setHide(true);
                    break;

                case R.id.menu_web_show:
                    setHide(false);
                    break;

                case R.id.menu_web_download:
                    startDownload();
                    break;

                case R.id.menu_web_cancel:
                    deleteCard(false);
                    break;

                case R.id.menu_web_delete:
                    deleteCard(true);
                    break;
            }
            selCard = null;
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }
    }

    private static class ViewHolder {
        public TextView name;
        public TextView desc;
        public TextView auth;
        public TextView web_desc;
        public ImageView icon;
        public ImageView new_vers;
        public ImageView hidden;
        public ProgressBar bar;
    }

    class WebCardAdapter extends ArrayAdapter<CardItem> {
        public WebCardAdapter() {
            super(act, R.layout.web_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            CardItem cardItem = getItem(position);

            ViewHolder holder;
            if (rowView == null) {
                rowView = act.getLayoutInflater().inflate(R.layout.web_item, null, true); //context.

                holder = new ViewHolder();
                holder.name = (TextView) rowView.findViewById(R.id.card_name);
                holder.desc = (TextView) rowView.findViewById(R.id.card_desc);
                holder.auth = (TextView) rowView.findViewById(R.id.card_auth);
                holder.web_desc = (TextView) rowView.findViewById(R.id.web_desc);
                holder.bar = (ProgressBar) rowView.findViewById(R.id.card_bar);
                holder.icon = (ImageView) rowView.findViewById(R.id.card_icon);
                holder.new_vers = (ImageView) rowView.findViewById(R.id.card_new_vers);
                holder.hidden = (ImageView) rowView.findViewById(R.id.card_hidden);

                // Save
                rowView.setTag(holder);
            } else {
                holder = (ViewHolder) rowView.getTag();
            }

            if (cardItem == selCard)
                rowView.setBackgroundResource(R.drawable.bg_borber_sel);
            else
                rowView.setBackgroundResource(0);

            holder.name.setText(cardItem.name);
            holder.desc.setText(cardItem.desc);
            holder.auth.setText(String.format(getString(R.string.Card_Author), cardItem.auth));

            // Main icon
            holder.icon.setImageBitmap(cardItem.iconImg);

            // Hided
            holder.hidden.setVisibility(cardItem.hide ? View.VISIBLE : View.INVISIBLE);

            // Update with current & webText
            if (cardItem.progress > 0) {
                holder.bar.setVisibility(View.VISIBLE);
                holder.web_desc.setVisibility(View.INVISIBLE);
                holder.bar.setProgress(cardItem.progress);
            } else {
                holder.bar.setVisibility(View.INVISIBLE);
                holder.web_desc.setVisibility(View.VISIBLE);
            }

            long iSize = 0;
            if (cardItem.fileListTask != null)
                iSize = cardItem.fileListTask.loadSize();

            if (iSize == 0) {
                holder.web_desc.setText(R.string.Card_Uptodate);
                holder.new_vers.setVisibility(View.INVISIBLE);
            } else {
                holder.web_desc.setText(String.format(AllWebAct.sCardSize, (float) iSize / 1024 / 1024));
                holder.new_vers.setVisibility(View.VISIBLE);
            }

            return rowView;
        }
    }
}
