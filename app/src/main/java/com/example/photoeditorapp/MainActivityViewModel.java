package com.example.photoeditorapp;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {

    private final ObservableField<String> mImageInfo = new ObservableField<>();
    private final MutableLiveData<Bitmap> mImageBitmap = new MutableLiveData<>();
    public final MutableLiveData<Uri> mImageUri = new MutableLiveData<>();

    public void onImageInfoChange(String imageInfo) {
        mImageInfo.set(imageInfo);
    }


    public void onImageBitmapChange(Bitmap bitmap) {
        mImageBitmap.setValue(bitmap);
    }


    public LiveData<Bitmap> getImageBitmap() {
        return mImageBitmap;
    }

    public ObservableField<String> getImageInfoObserver() {
        return mImageInfo;
    }
}
