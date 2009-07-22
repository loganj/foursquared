/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared;

import com.joelapenna.foursquare.types.Checkin;
import com.joelapenna.foursquare.types.Tip;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

import java.util.List;

/**
 * @author Joe LaPenna (joe@joelapenna.com)
 */
public class VenueCheckinActivity extends ListActivity {

    private static final int DIALOG_CHECKIN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.venue_checkin_activity);

        setListAdapter(new TipsListAdapter(this));
        List<Tip> tips = Foursquared.createTestTips();
        for (int i = 0; i < tips.size(); i++) {
            ((TipsListAdapter)getListAdapter()).add(tips.get(i));
        }

        Button checkinButton = (Button)findViewById(R.id.checkinButton);
        checkinButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_CHECKIN);
                v.setEnabled(false);
            }
        });
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CHECKIN:
                Checkin checkin = Foursquared.createIncomingCheckin();
                WebView webView = new WebView(this);
                webView.loadUrl(checkin.getUrl());
                Spanned title = Html.fromHtml(checkin.getMessage());
                return new AlertDialog.Builder(this) // the builder
                        .setView(webView) // use a web view
                        .setIcon(android.R.drawable.ic_dialog_info) // show an icon
                        .setTitle(title).create(); // return it.
        }
        return null;
    }
}
