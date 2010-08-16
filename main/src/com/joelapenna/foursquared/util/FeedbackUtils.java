/**
 * Copyright 2009 Joe LaPenna
 */

package com.joelapenna.foursquared.util;

import com.joelapenna.foursquared.Foursquared;
import com.joelapenna.foursquared.R;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.widget.Toast;

/**
 * Collection of common functions for sending feedback.
 * 
 * @author Alex Volovoy (avolovoy@gmail.com)
 */
public class FeedbackUtils {

    private static final String FEEDBACK_EMAIL_ADDRESS = "crashreport-android@foursquare.com";

    public static void SendFeedBack(Context context, Foursquared foursquared) {

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        final String[] mailto = {
            FEEDBACK_EMAIL_ADDRESS
        };
        final String new_line = "\n";
        StringBuilder body = new StringBuilder();
        Resources res = context.getResources();
        body.append(new_line);
        body.append(new_line);
        body.append(res.getString(R.string.feedback_more));
        body.append(new_line);
        body.append(res.getString(R.string.feedback_question_how_to_reproduce));
        body.append(new_line);
        body.append(new_line);
        body.append(res.getString(R.string.feedback_question_expected_output));
        body.append(new_line);
        body.append(new_line);
        body.append(res.getString(R.string.feedback_question_additional_information));
        body.append(new_line);
        body.append(new_line);
        body.append("--------------------------------------");
        body.append(new_line);
        body.append("ver: ");
        body.append(foursquared.getVersion());
        body.append(new_line);
        body.append("user: ");
        body.append(foursquared.getUserId());
        body.append(new_line);
        body.append("p: ");
        body.append(Build.MODEL);
        body.append(new_line);
        body.append("os: ");
        body.append(Build.VERSION.RELEASE);
        body.append(new_line);
        body.append("build#: ");
        body.append(Build.DISPLAY);
        body.append(new_line);
        body.append(new_line);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.feedback_subject));
        sendIntent.putExtra(Intent.EXTRA_EMAIL, mailto);
        sendIntent.putExtra(Intent.EXTRA_TEXT, body.toString());
        sendIntent.setType("message/rfc822");
        try {
            context.startActivity(Intent.createChooser(sendIntent, context
                    .getText(R.string.feedback_subject)));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, context.getText(R.string.feedback_error), Toast.LENGTH_SHORT)
                    .show();
        }
    }

}
