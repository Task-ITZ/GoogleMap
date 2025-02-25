package com.example.googleMap.ui;

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
import android.util.Log;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.googleMap.R;
import com.example.googleMap.databinding.ActivityDetailBinding;
import com.example.googleMap.model.Bookmark;
import com.example.googleMap.repository.BookmarkRepository;
import com.example.googleMap.util.ImageUtils;
import com.example.googleMap.viewmodel.BookmarkViewModel;

import java.io.File;
import java.io.IOException;


public class DetailActivity extends AppCompatActivity{

    private ActivityDetailBinding binding;
    private BookmarkViewModel bookmarkViewModel;
    BookmarkViewModel.BookmarkView bookmarkView;
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bookmarkViewModel = new ViewModelProvider(this).get(BookmarkViewModel.class);

        bookmarkViewModel.getBookmark(bookmarkId).observe(this, (it)-> {
            bookmarkView = it;
            binding.setViewModel(it);
            populateImageView();
        });

    }

    private void populateImageView() {
        if (bookmarkView != null) {
            Bitmap placeImage = bookmarkView.getImage(this);
            if (placeImage != null) {
                binding.imageview.setImageBitmap(placeImage);
            }
        }
        binding.imageview.setOnClickListener(v->{
            showImagePickerDialog();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_tick, menu);
        return true;
    }

    private void saveChange() {
        String name = binding.edtName.getText().toString();
        if (name.isEmpty()) {
            return;
        }
        if (bookmarkView != null) {
            bookmarkView.name = binding.edtName.getText().toString();
            bookmarkView.notes = binding.edtNote.getText().toString();
            bookmarkView.address = binding.edtAddress.getText().toString();
            bookmarkView.phone = binding.edtPhone.getText().toString();
            bookmarkViewModel.updateBookmark(bookmarkView);
        }
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_tick) {
            saveChange();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                photoFile = ImageUtils.createUniqueImageFile(this);
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
                    updateImage(bitmap);
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    updateImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateImage(Bitmap image) {
        if (bookmarkView != null) {
            binding.imageview.setImageBitmap(image);
            bookmarkView.setImage(this,image);
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