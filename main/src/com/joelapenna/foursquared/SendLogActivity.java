/*
 * Copyright (C) 2010 Mark Wyszomierski
 * 
 * Portions Copyright (C) 2009 Xtralogic, Inc.
 */
package com.joelapenna.foursquared;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * This is taken from the android-log-collector project here:
 * 
 *   http://code.google.com/p/android-log-collector/
 *   
 * so as we can dump the last set of system logs from the user's device at the
 * bottom of their feedback email. If they are reporting a crash, the logs 
 * might show exceptions etc. Android 2.2+ reports this directly to the marketplace
 * for us so this will be phased out eventually.
 * 
 * @date July 8, 2010
 * @author Mark Wyszomierski (markww@gmail.com)
 *
 */
public class SendLogActivity extends Activity 
{
    public final static String TAG = "com.xtralogic.android.logcollector";//$NON-NLS-1$
    
    private static final String FEEDBACK_EMAIL_ADDRESS = "crashreport-android@foursquare.com";

    public static final String ACTION_SEND_LOG = "com.xtralogic.logcollector.intent.action.SEND_LOG";//$NON-NLS-1$
    public static final String EXTRA_SEND_INTENT_ACTION = "com.xtralogic.logcollector.intent.extra.SEND_INTENT_ACTION";//$NON-NLS-1$
    public static final String EXTRA_DATA = "com.xtralogic.logcollector.intent.extra.DATA";//$NON-NLS-1$
    public static final String EXTRA_ADDITIONAL_INFO = "com.xtralogic.logcollector.intent.extra.ADDITIONAL_INFO";//$NON-NLS-1$
    public static final String EXTRA_SHOW_UI = "com.xtralogic.logcollector.intent.extra.SHOW_UI";//$NON-NLS-1$
    public static final String EXTRA_FILTER_SPECS = "com.xtralogic.logcollector.intent.extra.FILTER_SPECS";//$NON-NLS-1$
    public static final String EXTRA_FORMAT = "com.xtralogic.logcollector.intent.extra.FORMAT";//$NON-NLS-1$
    public static final String EXTRA_BUFFER = "com.xtralogic.logcollector.intent.extra.BUFFER";//$NON-NLS-1$
    
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    final int MAX_LOG_MESSAGE_LENGTH = 100000;
    
    private AlertDialog mMainDialog;
    private Intent mSendIntent;
    private CollectLogTask mCollectLogTask;
    private ProgressDialog mProgressDialog;
    private String mAdditonalInfo;
    private String[] mFilterSpecs;
    private String mFormat;
    private String mBuffer;
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        
        mSendIntent = new Intent(Intent.ACTION_SEND);
        mSendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.send_log_message_subject));
        mSendIntent.setType("text/plain");

        Foursquared foursquared = (Foursquared)getApplication();
        
        StringBuilder body = new StringBuilder();
        Resources res = getResources();
        body.append(res.getString(R.string.feedback_more));
        body.append(LINE_SEPARATOR);
        body.append(res.getString(R.string.feedback_question_how_to_reproduce));
        body.append(LINE_SEPARATOR);
        body.append(LINE_SEPARATOR);
        body.append(res.getString(R.string.feedback_question_expected_output));
        body.append(LINE_SEPARATOR);
        body.append(LINE_SEPARATOR);
        body.append(res.getString(R.string.feedback_question_additional_information));
        body.append(LINE_SEPARATOR);
        body.append(LINE_SEPARATOR);
        body.append("--------------------------------------");
        body.append(LINE_SEPARATOR);
        body.append("ver: ");
        body.append(foursquared.getVersion());
        body.append(LINE_SEPARATOR);
        body.append("user: ");
        body.append(foursquared.getUserId());
        body.append(LINE_SEPARATOR);
        body.append("p: ");
        body.append(Build.MODEL);
        body.append(LINE_SEPARATOR);
        body.append("os: ");
        body.append(Build.VERSION.RELEASE);
        body.append(LINE_SEPARATOR);
        body.append("build#: ");
        body.append(Build.DISPLAY);
        body.append(LINE_SEPARATOR);
        body.append(LINE_SEPARATOR);
        mSendIntent.putExtra(Intent.EXTRA_SUBJECT, res.getString(R.string.feedback_subject));
        mSendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { FEEDBACK_EMAIL_ADDRESS });
        mSendIntent.setType("message/rfc822"); 
        
        mAdditonalInfo = body.toString();
        mFormat = "process";
         
        collectAndSendLog();
    }
    
    @SuppressWarnings("unchecked")
    void collectAndSendLog(){
        /*Usage: logcat [options] [filterspecs]
        options include:
          -s              Set default filter to silent.
                          Like specifying filterspec '*:s'
          -f <filename>   Log to file. Default to stdout
          -r [<kbytes>]   Rotate log every kbytes. (16 if unspecified). Requires -f
          -n <count>      Sets max number of rotated logs to <count>, default 4
          -v <format>     Sets the log print format, where <format> is one of:

                          brief process tag thread raw time threadtime long

          -c              clear (flush) the entire log and exit
          -d              dump the log and then exit (don't block)
          -g              get the size of the log's ring buffer and exit
          -b <buffer>     request alternate ring buffer
                          ('main' (default), 'radio', 'events')
          -B              output the log in binary
        filterspecs are a series of
          <tag>[:priority]

        where <tag> is a log component tag (or * for all) and priority is:
          V    Verbose
          D    Debug
          I    Info
          W    Warn
          E    Error
          F    Fatal
          S    Silent (supress all output)

        '*' means '*:d' and <tag> by itself means <tag>:v

        If not specified on the commandline, filterspec is set from ANDROID_LOG_TAGS.
        If no filterspec is found, filter defaults to '*:I'

        If not specified with -v, format is set from ANDROID_PRINTF_LOG
        or defaults to "brief"*/

        ArrayList<String> list = new ArrayList<String>();
        
        if (mFormat != null){
            list.add("-v");
            list.add(mFormat);
        }
        
        if (mBuffer != null){
            list.add("-b");
            list.add(mBuffer);
        }

        if (mFilterSpecs != null){
            for (String filterSpec : mFilterSpecs){
                list.add(filterSpec);
            }
        }
        
        mCollectLogTask = (CollectLogTask) new CollectLogTask().execute(list);
    } 
    
    private class CollectLogTask extends AsyncTask<ArrayList<String>, Void, StringBuilder>{
        @Override
        protected void onPreExecute(){
            showProgressDialog(getString(R.string.send_log_acquiring_log_progress_dialog_message));
        }
        
        @Override
        protected StringBuilder doInBackground(ArrayList<String>... params){
            final StringBuilder log = new StringBuilder();
            try{
                ArrayList<String> commandLine = new ArrayList<String>();
                commandLine.add("logcat");//$NON-NLS-1$
                commandLine.add("-d");//$NON-NLS-1$
                ArrayList<String> arguments = ((params != null) && (params.length > 0)) ? params[0] : null;
                if (null != arguments){
                    commandLine.addAll(arguments);
                }
                
                Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[0]));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                
                String line;
                while ((line = bufferedReader.readLine()) != null){ 
                    log.append(line);
                    log.append(LINE_SEPARATOR); 
                }
            } 
            catch (IOException e){
                Log.e(TAG, "CollectLogTask.doInBackground failed", e);//$NON-NLS-1$
            } 

            return log;
        }

        @Override
        protected void onPostExecute(StringBuilder log){
            if (null != log){
                //truncate if necessary
                int keepOffset = Math.max(log.length() - MAX_LOG_MESSAGE_LENGTH, 0);
                if (keepOffset > 0){
                    log.delete(0, keepOffset);
                } 
                
                if (mAdditonalInfo != null){
                    log.insert(0, mAdditonalInfo);
                }
                
                mSendIntent.putExtra(Intent.EXTRA_TEXT, log.toString());
                startActivity(Intent.createChooser(mSendIntent, getString(R.string.send_log_chooser_title)));
                dismissProgressDialog();
                dismissMainDialog();
                finish();
            }
            else{ 
                dismissProgressDialog();
                showErrorDialog(getString(R.string.send_log_failed_to_get_log_message));
            }
        }
    }
    
    void showErrorDialog(String errorMessage){
        new AlertDialog.Builder(this)
        .setTitle(getString(R.string.app_name))
        .setMessage(errorMessage)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){
                finish();
            }
        })
        .show();
    }
    
    void dismissMainDialog(){
        if (null != mMainDialog && mMainDialog.isShowing()){
            mMainDialog.dismiss();
            mMainDialog = null;
        }
    }
    
    void showProgressDialog(String message){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage(message);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            public void onCancel(DialogInterface dialog){
                cancellCollectTask();
                finish();
            }
        });
        mProgressDialog.show();
    }
    
    private void dismissProgressDialog(){
        if (null != mProgressDialog && mProgressDialog.isShowing())
        {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
    
    void cancellCollectTask(){
        if (mCollectLogTask != null && mCollectLogTask.getStatus() == AsyncTask.Status.RUNNING) 
        {
            mCollectLogTask.cancel(true);
            mCollectLogTask = null;
        }
    }
    
    @Override
    protected void onPause(){
        cancellCollectTask();
        dismissProgressDialog();
        dismissMainDialog();
        
        super.onPause();
    }
}