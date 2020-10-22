package com.example.rvdownloads;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterDownload.Download {

    private static String file_url = "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3";
    private long downloadID;
    private int position;
    private AdapterDownload adapterDownload;
    private DownloadManager manager;
    private String filePath;
    private int dl_progress;
    private static int PERMISSIONS_REQUEST_READ_CONTACTS = 8888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!isDownloadManagerAvailable()){
             Toast.makeText(this,"אפליקציה זו בנויה על DownloadManager המכשיר שלך אינו תומך בזה!", Toast.LENGTH_SHORT).show();
        }else {
            initViews();
            registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }

    public static boolean isDownloadManagerAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return true;
        }
        return false;
    }

    private void initViews() {
        RecyclerView mRecyclerView = findViewById(R.id.RecyclerView_RV);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapterDownload = new AdapterDownload(this,initArray());
        mRecyclerView.setAdapter(adapterDownload);
    }

    private ArrayList<File> initArray() {
        ArrayList<File> myDownloads = new ArrayList<>();
        for (int i = 1; i < 40 ; i++) {
            myDownloads.add(new File(i,"הורדה",0));
        }
        return myDownloads;
    }



    @Override
    public void downloadClick(int position) {
       this.position = position;
        askPermissions();
    }

    private void askPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            Log.d("TAG", "He has not Permissions, we ask now");
        } else {
            Log.d("TAG", "He has permissions");
            downloadManager(position);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NotNull String permissions[], @NotNull int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadManager(position);
                Log.d("TAG", "He approved the permissions request");

            } else {
                Log.d("TAG", "He did not approved the permissions request");
            }
        }
    }

    private void downloadManager(int position) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(file_url));
        request.setDescription("שיר יפה");
        request.setTitle(String.valueOf(position));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Kalimba"+position+".mp3");
        manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadID = manager.enqueue(request);
        updateBar();
    }

    private void updateBar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean downloading = true;
                while (downloading) {
                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(downloadID);
                    Cursor cursor = manager.query(q);
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }

                    dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapterDownload.updateDownloadBar(position,dl_progress);
                        }
                    });
                    cursor.close();
                }
            }
        }).start();
    }



    @Override
    public void shareClick(int position) {
        java.io.File download = new java.io.File(Environment.getExternalStorageDirectory() + "/download/");
        filePath = download + "/" + "Kalimba"+position+".mp3";
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        intentShareFile.setType("audio/*");
        intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+ filePath));
        startActivity(Intent.createChooser(intentShareFile, "Share File"));
    }




    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
            Cursor c = manager.query(q);

            if (c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {

                    String title;
                            title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                    Toast.makeText(MainActivity.this, "Download "+ title+" Completed", Toast.LENGTH_SHORT).show();
                    adapterDownload.updateDownloadFinish(Integer.parseInt(title));
                    c.close();
                    }
                }
            }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }
}