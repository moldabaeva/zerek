package com.zerek.ABC;

/**
 * Created with IntelliJ IDEA.
 * User: mode
 * Date: 6/28/13
 * Time: 11:27 AM
 */

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.Window;
import android.widget.TextView;
import com.zerek.ABC.log.WebLoader;
import com.zerek.ABC.log.task.TextTask;

public class About extends Activity { // SherlockActivity // SherlockFragment // SherlockDialogFragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // use own theme
//        this.setTheme(R.style.Theme_Sherlock_Light);
//        MainActivity.prepWindow(this, false, R.string.about_title);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        // Add company name info
        ((TextView) findViewById(R.id.about_copyrights)).setText(SettingsActivity.getCompName(this, true));


        // General vars
        final String sLang = SettingsActivity.getLang(this);
        final WebLoader webLoader = WebLoader.getInstance(null);

        // Url №1
        String sUrl = WebLoader.C_ROOT_URL + "about_cont_" + sLang + ".html";
        webLoader.addTask(new TextTask(sUrl) {
            private static final String C_ABOUT_ID = "about_cont_";

            @Override
            public void run() {
                setAbout(this.text);
                SettingsActivity.setOpt(About.this, C_ABOUT_ID + sLang, this.text);
            }

            @Override
            public void showErrInfo(final Activity act, final String sMes) {
                String sText = SettingsActivity.getOpt(About.this, C_ABOUT_ID + sLang);
                if (sText.equals(""))
                    super.showErrInfo(act, sMes);
                else
                    setAbout(sText);
            }

            private void setAbout(String sText) {
                ((TextView) findViewById(R.id.thanks_contact_body)).setText(Html.fromHtml(sText));
            }
        });
        webLoader.doNotify();
    }
}
