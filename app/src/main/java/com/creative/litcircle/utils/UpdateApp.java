package com.creative.litcircle.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;

/**
 * Created by comsol on 06-Apr-17.
 */
public class UpdateApp extends AsyncTask<String,Void,Void> {
    private Activity context;
    public void setContext(Context contextf){
        context = (Activity) contextf;
    }

    ProgressDialog progressBarDialog;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //System.out.println("Starting download");

        progressBarDialog= new ProgressDialog(context);
        progressBarDialog.setTitle("Updating App, Please Dont Close The App Until Download!");
        progressBarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBarDialog.setIndeterminate(false);
        progressBarDialog.setCancelable(false);
        progressBarDialog.setProgress(0);

        //show the dialog
        progressBarDialog.show();
    }


    @Override
    protected Void doInBackground(String... arg0) {

            //get destination to update file and set Uri
            //TODO: First I wanted to store my update .apk file on internal storage for my app but apparently android does not allow you to open and install
            //aplication with existing package from there. So for me, alternative solution is Download directory in external storage. If there is better
            //solution, please inform us in comment
            String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
            String fileName = "BGB.apk";
            destination += fileName;
            final Uri uri = Uri.parse("file://" + destination);

            //Delete update file if exists
            File file = new File(destination);
            if (file.exists())
                //file.delete() - test this, I think sometimes it doesnt work
                file.delete();

            //get url of app on server
            String url = arg0[0];


            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription("UPDATING");
            request.setTitle("BGB APP IS DOWNLOADING");

            //set destination
            request.setDestinationUri(uri);

            // get download service and enqueue file
            final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            final long DownloadManagerId = manager.enqueue(request);

            new Thread(new Runnable() {

                @Override
                public void run() {

                    boolean downloading = true;


                    while (downloading) {

                        DownloadManager.Query q = new DownloadManager.Query();
                        q.setFilterById(DownloadManagerId); //filter by id which you have receieved when reqesting download from download manager
                        Cursor cursor = manager.query(q);
                        cursor.moveToFirst();
                        int bytes_downloaded = cursor.getInt(cursor
                                .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false;
                        }

                        final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

                        context.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                progressBarDialog.setProgress((int) dl_progress);

                            }
                        });

                        // Log.d(Constants.MAIN_VIEW_ACTIVITY, statusMessage(cursor));
                        cursor.close();
                    }

                }
            }).start();


            //set downloadmanager


            //set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    install.setDataAndType(uri,
                            manager.getMimeTypeForDownloadedFile(DownloadManagerId));
                    context.startActivity(install);

                    context.unregisterReceiver(this);
                    //finish();
                }
            };
            //register receiver for when .apk download is compete
            context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));



        return null;
    }
}