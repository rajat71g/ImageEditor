package com.example.photoeditorapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;

import androidx.databinding.DataBindingUtil;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.photoeditorapp.databinding.ActivityMainBinding;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int PICK_IMAGE = 1;
    Bitmap mBitmapImage;
    String mImageName;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    Button OpenBtn, SaveBtn, InfoBtn, VerticalBtn, HorizontalBtn, CropBtn;
    ImageView mImageView;
    private Uri mImageUri;

    private MainActivityViewModel mViewModel;
    private ActivityMainBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageName = "default";
        mBitmapImage = null;
        mImageUri = null;
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(MainActivityViewModel.class);
        mBinding.setViewModel(mViewModel);
        initViews();
        initClickListener();
        initObservers();
        checkPermissionForApp();
    }

    private void initObservers() {
        mViewModel.getImageBitmap().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap bitmap) {
                mBinding.imageView.setImageBitmap(bitmap);
            }
        });
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
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();

                } else {
                    // permission denied
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initClickListener() {
        mBinding.open.setOnClickListener(this);
        mBinding.save.setOnClickListener(this);
        mBinding.info.setOnClickListener(this);
        mBinding.crop.setOnClickListener(this);
        mBinding.verticalFlip.setOnClickListener(this);
        mBinding.horizontalFlip.setOnClickListener(this);
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
        matrix.postScale(xFlip ? -1 : 1, yFlip ? -1 : 1, mViewModel.getImageBitmap().getValue().getWidth() / 2f, mViewModel.getImageBitmap().getValue().getHeight() / 2f);
        mImageView.setImageBitmap(Bitmap.createBitmap(mViewModel.getImageBitmap().getValue(), 0, 0, mViewModel.getImageBitmap().getValue().getWidth(), mViewModel.getImageBitmap().getValue().getHeight(), matrix, true));
        BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
        mViewModel.onImageBitmapChange(drawable.getBitmap());
    }

    private void cropImage() {
        CropImage.activity(mImageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    private void showInfo() {
        if(mViewModel.mImageUri.getValue()!=null) {
            try {
                InputStream stream = getContentResolver().openInputStream(mViewModel.mImageUri.getValue());
                ExifInterface exif = new ExifInterface(stream);

                StringBuilder sb = new StringBuilder();
                sb.append("Width: ").append(exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)).append("\n");
                sb.append("Length: ").append(exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)).append("\n");
                sb.append("DateTime: ").append(exif.getAttribute(ExifInterface.TAG_DATETIME)).append("\n");
                sb.append("Orientation: ").append(exif.getAttribute(ExifInterface.TAG_ORIENTATION)).append("\n");

                Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();

            } catch (Exception e) {

            }
        } else {
            Toast.makeText(this, "Please Select an image", Toast.LENGTH_LONG).show();
        }
    }

    private void saveImage() {
        Toast.makeText(this, "Image Saved", Toast.LENGTH_LONG).show();
        MediaStore.Images.Media.insertImage(getContentResolver(), mViewModel.getImageBitmap().getValue(), mViewModel.getImageInfoObserver().get() , "None");

    }

    private void openImageBrowser() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            mImageUri = data.getData();
            try {
                mViewModel.onImageBitmapChange(MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri));
                mViewModel.onImageInfoChange(mImageUri.getPath());
                mViewModel.mImageUri.setValue(mImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mImageView.setImageURI(mImageUri);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result != null) {
                mImageView.setImageURI(result.getUri());
                Toast.makeText(this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
