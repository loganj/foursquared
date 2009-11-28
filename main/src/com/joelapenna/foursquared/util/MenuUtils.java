/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.widget.Toast;

import com.joelapenna.foursquared.Foursquared;
import com.joelapenna.foursquared.R;

/**
 * @author Alex Volovoy (avolovoy@gmail.com) Collection of common functions
 *         which are called from the menu
 */
public class MenuUtils {

    public static void SendFeedBack(Context ctx, Foursquared foursquared) {

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        String[] mailto = {
            "foursquared-dev@googlegroups.com"
        };
        String separator = "|";
        String new_line = "\n";
        StringBuilder body = new StringBuilder();
        Resources res = ctx.getResources();
        body.append(new_line);
        body.append(new_line);
        body.append(res.getString(R.string.feedback_more));
        body.append(new_line);
        body.append(res.getString(R.string.feedback_q1));
        body.append(new_line);
        body.append(new_line);
        body.append(res.getString(R.string.feedback_q2));
        body.append(new_line);
        body.append(new_line);
        body.append(res.getString(R.string.feedback_q3));
        body.append(new_line);
        body.append(new_line);
        body.append("--------------------------------------");
        body.append(new_line);
        body.append("ver:");
        body.append(foursquared.getVersion());
        body.append(separator);
        body.append("city:");
        body.append(foursquared.getUserCity().getName());
        body.append(separator);
        body.append("user:");
        body.append(foursquared.getUserId());
        body.append(separator);
        body.append("p:");
        body.append(Build.MODEL);
        body.append(separator);
        body.append("os:");
        body.append(Build.VERSION.RELEASE);
        body.append(separator);
        body.append("build#:");
        body.append(Build.DISPLAY);
        body.append(new_line);
        body.append(new_line);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, ctx.getString(R.string.feedback_subject));
        sendIntent.putExtra(Intent.EXTRA_EMAIL, mailto);
        sendIntent.putExtra(Intent.EXTRA_TEXT, body.toString());
        sendIntent.setType("message/rfc822");
        try {
            ctx.startActivity(Intent.createChooser(sendIntent, ctx.getText(R.string.feedback_subject)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ctx, ctx.getText(R.string.feedback_error), Toast.LENGTH_SHORT).show();
        }
    }
}
