package com.the;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private Button download, view;
    private String url;
    private String chiled = "kotlin-reference.pdf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        king();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        download = (Button) findViewById(R.id.Download);
        view = (Button) findViewById(R.id.View);

        fileexits();
        PRDownloader.initialize(getApplicationContext());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imagref = storage.getReference()
                .child(chiled);
        imagref.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        url = uri.toString();

                    }
                });
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentcheck();
                fileexits();
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              vieww();

            }
        });
    }

    public void vieww() {
        Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
        intent.putExtra("URL", url);
        startActivity(intent);

    }

    public void king() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {

                } else {
                    Toast.makeText(getApplicationContext(), "Allow All Permissions", Toast.LENGTH_SHORT);
                }

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();
    }

    public void downlaod() {

        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + chiled);
        if (pdfFile.exists()) //Checking if the file exists or not
        {
            pdfopen();

        } else {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            String title = URLUtil.guessFileName(url, null, null);
            request.setTitle(title);
            request.setDescription("Download PDF");
            String cookie = CookieManager.getInstance().getCookie(url);
            request.addRequestHeader("cookie", cookie);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);

            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);
            Toast.makeText(getApplicationContext(), "Download Started", Toast.LENGTH_SHORT).show();
        }
    }

    public void downloadpdf() {

        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + chiled);
        if (pdfFile.exists()) //Checking if the file exists or not
        {
            pdfopen();

        } else {
            ProgressDialog pd = new ProgressDialog(this);
            pd.setTitle("Download");
            pd.setMessage("One Time Downloading...");
            pd.setCancelable(false);
            pd.show();

            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            PRDownloader.download(url, file.getPath(), URLUtil.guessFileName(url, null, null))
                    .build()
                    .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                        @Override
                        public void onStartOrResume() {

                        }
                    })
                    .setOnPauseListener(new OnPauseListener() {
                        @Override
                        public void onPause() {

                        }
                    })
                    .setOnCancelListener(new OnCancelListener() {
                        @Override
                        public void onCancel() {

                        }
                    })
                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(Progress progress) {
                            long per = progress.currentBytes * 100 / progress.totalBytes;
                            pd.setMessage("Downloading:" + per);
                        }
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            pd.dismiss();
                            fileexits();
                            Toast.makeText(getApplicationContext(), "Download Complete", Toast.LENGTH_SHORT);
                        }

                        @Override
                        public void onError(Error error) {
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT);
                        }


                    });
        }
    }

    private void dilog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Check Your Inter Connection")
                .setTitle("No Internet")
                .setCancelable(false)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        intentcheck();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void intentcheck() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.
                getActiveNetworkInfo().isConnected()) {
            downloadpdf();
//            downlaod();
        } else {
            dilog();
        }
    }

    private void pdfopen() {
        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + chiled);
        if (pdfFile.exists()) //Checking if the file exists or not
        {
            Uri path = Uri.fromFile(pdfFile);
            Intent objIntent = new Intent(Intent.ACTION_VIEW);
            objIntent.setDataAndType(path, "application/pdf");
            objIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(objIntent);//Starting the pdf viewer
        } else {

            Toast.makeText(getApplicationContext(), "The file not exists! ", Toast.LENGTH_SHORT).show();

        }
    }

    private void fileexits() {
        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + chiled);
        if (pdfFile.exists()) //Checking if the file exists or not
        {
            download.setText("Open");
            view.setVisibility(View.GONE);
        } else {

        }
    }

}