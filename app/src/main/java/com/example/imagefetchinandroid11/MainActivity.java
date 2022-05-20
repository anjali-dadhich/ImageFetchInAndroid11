package com.example.imagefetchinandroid11;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    Button imageButton;
    ImageView fetchImage;
    ActivityResultLauncher<Intent> someActivityResultLauncher;
    int requestCodeForResult = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageButton = findViewById(R.id.imageButton);
        fetchImage = findViewById(R.id.fetchImage);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPermissionGranted()) {
                    fetchImageFromGallery();
                } else {
                    takePermission();
                }
            }
        });

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK){
                            Intent data = result.getData();
                            if (requestCodeForResult == 100) {
                                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                                    if (Environment.isExternalStorageManager()) {
                                        fetchImageFromGallery();
                                    } else {
                                        takePermission();
                                    }
                                }
                            } else if (requestCodeForResult == 102){
                                if (data != null) {
                                    Uri uri = data.getData();
                                    if (uri != null) {
                                        fetchImage.setImageURI(uri);
                                    }
                                }
                            }

                        }
                    }
                }
        );
    }

    private boolean isPermissionGranted(){
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R){
            return Environment.isExternalStorageManager();
        } else {
            int readExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            return readExternalStorage == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void takePermission(){
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R){
            requestCodeForResult = 100;
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category,DEFAULT");
                intent.setData(Uri.parse(String.format("package%s", getApplicationContext().getPackageName())));
               // startActivityForResult(intent, 100);
                someActivityResultLauncher.launch(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                //startActivityForResult(intent, 100);
                someActivityResultLauncher.launch(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
        }
    }

    private void fetchImageFromGallery() {
        requestCodeForResult = 102;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //startActivityForResult(intent,102);
        someActivityResultLauncher.launch(intent);
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == 100){
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()){
                        fetchImageFromGallery();
                    } else {
                        takePermission();
                    }
                }
            } else if (requestCode == 102) {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        fetchImage.setImageURI(uri);
                    }
                }
            }
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0){
            if (requestCode == 101){
                boolean readExternalStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (readExternalStorage){
                    fetchImageFromGallery();
                } else {
                    takePermission();
                }
            }
        }
    }
}