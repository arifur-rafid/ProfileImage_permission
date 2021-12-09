package com.example.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    Button selectBtn;
    ImageView profileImage;
    private final int GALLERY = 1;
    private String perms[] = {Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectBtn = findViewById(R.id.selectBtn);
        profileImage = findViewById(R.id.profileImage);
        selectButtonOnClickListener();

        Picasso picasso = new Picasso.Builder(this).build();
        picasso.setLoggingEnabled(true);
        //picasso.load("file:///storage/emulated/0/logo3test.jpg").into(profileImage);
        showProfileImage(Uri.parse(getImagePath()));
    }

    private void selectButtonOnClickListener() {
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Permission tutorial https://www.youtube.com/watch?v=lYWLI_2kzM4
                checkPermission();
            }
        });
    }

    private void goToGallary() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MainActivity.this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                try {
                    Uri contentURI = data.getData();
                    saveImagePath(getRealPathFromURI(contentURI));
                    showProfileImage(contentURI);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showProfileImage(Uri uri) {
        Picasso.get().load(uri).error(R.drawable.ic_launcher_background).into(profileImage);
    }

    private void saveImagePath(String path) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("profileImagePath", "file:///" + path);
        //System.out.println("***set " + "file:///" + path);
        editor.apply();
    }

    private String getImagePath() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String imageUriString = preferences.getString("profileImagePath", "");
        //System.out.println("***get " + imageUriString);
        return imageUriString;
    }

    public String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void checkPermission() {
        if (EasyPermissions.hasPermissions(MainActivity.this, perms)) {
            // when permission already granted
            goToGallary();
        } else {
            // Once user press the deny button it will notify user why this permission is needed through a dialogbox
            EasyPermissions.requestPermissions(MainActivity.this, "This Application need this permission to show photo from your gallery", GALLERY, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case GALLERY:
                goToGallary();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            {
                // if user select do not show button. app will take him to app permission page
                new AppSettingsDialog.Builder(this).build().show();
            }
        } else {
            // if user select deny button
            Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
        }
    }
}
