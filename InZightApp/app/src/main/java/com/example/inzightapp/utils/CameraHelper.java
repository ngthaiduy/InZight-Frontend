package com.example.inzightapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class để xử lý camera và chọn hình ảnh
 */
public class CameraHelper {
    
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int REQUEST_STORAGE_PERMISSION = 1002;
    
    private Fragment fragment;
    private Activity activity;
    private Context context;
    private File photoFile;
    
    public interface ImageCaptureCallback {
        void onImageCaptured(Bitmap bitmap);
        void onError(String error);
    }
    
    public CameraHelper(Fragment fragment) {
        this.fragment = fragment;
        this.context = fragment.requireContext();
        this.activity = fragment.requireActivity();
    }
    
    public CameraHelper(Activity activity) {
        this.activity = activity;
        this.context = activity;
    }
    
    /**
     * Kiểm tra quyền camera
     */
    public boolean hasCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    
    /**
     * Yêu cầu quyền camera
     */
    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (fragment != null) {
                fragment.requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION
                );
            } else if (activity != null) {
                ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION
                );
            }
        }
    }
    
    /**
     * Tạo Intent để chụp ảnh bằng camera
     */
    public Intent createTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".fileprovider",
                        photoFile
                    );
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                }
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
        }
        
        return takePictureIntent;
    }
    
    /**
     * Tạo file để lưu ảnh
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(null);
        File image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        );
        return image;
    }
    
    /**
     * Lấy file ảnh đã chụp
     */
    public File getPhotoFile() {
        return photoFile;
    }
    
    /**
     * Chuyển file ảnh thành Bitmap
     */
    public Bitmap getBitmapFromFile(File file) {
        try {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(file));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Chuyển Uri thành Bitmap
     */
    public Bitmap getBitmapFromUri(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Tạo ActivityResultLauncher cho camera
     */
    public ActivityResultLauncher<Intent> createCameraLauncher(ImageCaptureCallback callback) {
        if (fragment != null) {
            return fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bitmap bitmap = null;
                        if (photoFile != null && photoFile.exists()) {
                            bitmap = getBitmapFromFile(photoFile);
                        }
                        
                        // Fallback: lấy ảnh từ intent result nếu không có file
                        if (bitmap == null && result.getData() != null && result.getData().getExtras() != null) {
                            bitmap = (Bitmap) result.getData().getExtras().get("data");
                        }
                        
                        if (bitmap != null) {
                            callback.onImageCaptured(bitmap);
                        } else {
                            callback.onError("Không thể đọc ảnh");
                        }
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        callback.onError("Chụp ảnh bị hủy");
                    } else {
                        callback.onError("Chụp ảnh thất bại");
                    }
                }
            );
        }
        return null;
    }
    
    /**
     * Tạo ActivityResultLauncher cho chọn ảnh từ gallery
     */
    public ActivityResultLauncher<String> createImagePickerLauncher(ImageCaptureCallback callback) {
        if (fragment != null) {
            return fragment.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Bitmap bitmap = getBitmapFromUri(uri);
                        if (bitmap != null) {
                            callback.onImageCaptured(bitmap);
                        } else {
                            callback.onError("Không thể đọc ảnh từ gallery");
                        }
                    } else {
                        callback.onError("Không có ảnh được chọn");
                    }
                }
            );
        }
        return null;
    }
}

