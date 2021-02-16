package com.example.photoeditorapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int PICK_IMAGE = 1;
    Bitmap mBitmapImage;
    String mImageName;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    Button OpenBtn, SaveBtn, InfoBtn, VerticalBtn, HorizontalBtn, CropBtn;
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageName = "default";
        initViews();
        initClickListener();
        checkPermissionForApp();
    }

    private void checkPermissionForApp() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void initClickListener() {
        OpenBtn.setOnClickListener(this);
        SaveBtn.setOnClickListener(this);
        InfoBtn.setOnClickListener(this);
        CropBtn.setOnClickListener(this);
        VerticalBtn.setOnClickListener(this);
        HorizontalBtn.setOnClickListener(this);
    }

    private void initViews() {
        mImageView = findViewById(R.id.imageView);
        OpenBtn = findViewById(R.id.open);
        SaveBtn = findViewById(R.id.save);
        InfoBtn = findViewById(R.id.info);
        CropBtn = findViewById(R.id.crop);
        HorizontalBtn = findViewById(R.id.horizontalFlip);
        VerticalBtn = findViewById(R.id.verticalFlip);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open:
                openImageBrowser();
                break;
            case R.id.save:
                saveImage();
                break;
            case R.id.info:
                showInfo();
                break;
            case R.id.verticalFlip:
                flip(true, false);
                break;
            case R.id.horizontalFlip:
                flip(false, true);
                break;
            case R.id.crop:
                cropImage();
                break;
            default:
                Log.d("PhotoEditor", "No valid Option Selected.");
                break;
        }
    }

    private void flip(boolean xFlip, boolean yFlip) {
        Matrix matrix = new Matrix();
        matrix.postScale(xFlip ? -1 : 1, yFlip ? -1 : 1, mBitmapImage.getWidth() / 2f, mBitmapImage.getHeight() / 2f);
        mImageView.setImageBitmap(Bitmap.createBitmap(mBitmapImage, 0, 0, mBitmapImage.getWidth(), mBitmapImage.getHeight(), matrix, true));
        BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
        mBitmapImage = drawable.getBitmap();
    }

    private void cropImage() {
    }

    private void showInfo() {
    }

    private void saveImage() {
        Toast.makeText(this, "Image Saved", Toast.LENGTH_LONG).show();
        MediaStore.Images.Media.insertImage(getContentResolver(), mBitmapImage, mImageName , "None");

    }

    private void openImageBrowser() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery,PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            Uri imageUri = data.getData();
            try {
                mBitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                List<String> pathSegments = imageUri.getPathSegments();
                mImageName = pathSegments.get(pathSegments.size() - 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mImageView.setImageURI(imageUri);
        }
    }
}