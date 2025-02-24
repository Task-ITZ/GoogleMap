package com.example.googleMap;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import com.example.googleMap.databinding.ActivityDetailBinding;

import java.io.File;
import java.io.IOException;


public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private BookMarkViewModel bookmarkViewModel;
    private BookmarkRepository bookmarkRepository;
    private Bookmark bookmark;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 101;

    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        Long bookmarkId = getIntent().getLongExtra("mark_id", -1);

        bookmarkRepository = new BookmarkRepository(getApplication());

        bookmarkViewModel = new BookMarkViewModel();
        binding.setViewModel(bookmarkViewModel);
        binding.setLifecycleOwner(this);
        bookmark = bookmarkRepository.getBookmark(bookmarkId);

        bookmarkViewModel.setName(bookmark.getName());
        bookmarkViewModel.setNote(bookmark.getNote());
        bookmarkViewModel.setAddress(bookmark.getAddress());
        bookmarkViewModel.setPhoneNumber(bookmark.getPhone());
        bookmarkViewModel.setImage(bookmark.getImage(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_tick, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        bookmark.setName(bookmarkViewModel.getName());
        bookmark.setNote(bookmarkViewModel.getNote());
        bookmark.setAddress(bookmarkViewModel.getAddress());
        bookmark.setPhone(bookmarkViewModel.getPhoneNumber());
        bookmark.setImage(bookmarkViewModel.getImage(), this);

        bookmarkRepository.updateBookmark(bookmark);
        setResult(RESULT_OK);
        finish();
        return true;
    }

    public void onImageClick(View view) {
        showImagePickerDialog();
    }

    private void showImagePickerDialog() {
        String[] options = {"Chụp ảnh", "Chọn ảnh từ thư viện", "Hủy"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ảnh")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        dispatchTakePictureIntent();
                    } else if (which == 1) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(intent, REQUEST_IMAGE_PICK);
                    } else {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            photoFile = null;
            try {
                photoFile = bookmark.createImageFile(this);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.googleMap.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để mở máy ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (photoFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                    bitmap = rotateImageIfRequired(bitmap, photoFile.getAbsolutePath());
                    bitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true);
                    bookmarkViewModel.setImage(bitmap);
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    bookmarkViewModel.setImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap rotateImageIfRequired(Bitmap img, String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return img;
            }

            return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
            return img;
        }
    }

}