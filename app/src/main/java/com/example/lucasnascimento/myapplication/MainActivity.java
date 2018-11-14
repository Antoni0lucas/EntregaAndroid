package com.example.lucasnascimento.myapplication;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private BindedService bindedService;
    private boolean mBound = false;

    private TextView downloadStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23)
        {
            if (checkPermission()) {
            } else {
                requestPermission();
            }
        }
        else
        {

        }

        downloadStatus = findViewById(R.id.download_status);
        Button btnDownload = findViewById(R.id.download);
        btnDownload.setOnClickListener(onDownloadListener());


        Intent i = new Intent(this, InternetService.class);
        startService(i);
        InternetService.mainActivity = this;

    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.e("value", "Permission Granted, Now you can use local drive .");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    private View.OnClickListener onDownloadListener() {
        return new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.putExtra(DownloadService.FILENAME, "paisagem.jpg");
                intent.putExtra(DownloadService.URL, "https://www.urbanarts.com.br/imagens/produtos/111155/0/Detalhes/paisagem-abstrata-8.jpg");
                startService(intent);
                downloadStatus.setText("Download in progress");
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(DownloadService.NOTIFICATION));
    }

    @Override

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt(DownloadService.RESULT);
                if (resultCode == RESULT_OK) {
                    Toast.makeText(MainActivity.this, "Download completed!", Toast.LENGTH_LONG).show();
                    downloadStatus.setText("Download completed");
                } else {
                    Toast.makeText(MainActivity.this, "Download Error", Toast.LENGTH_LONG).show();
                    downloadStatus.setText("Download Error");
                }
            }
        }
    };


    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected( ComponentName className,
                                        IBinder service) {

            BindedService.LocalBinder binder = (BindedService.LocalBinder) service;
            bindedService = binder.getService();
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

}
