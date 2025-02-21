package com.example.googleMap;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.googleMap.databinding.ActivityDetailBinding;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private BookMarkViewModel bookmarkViewModel;
    private BookmarkRepository bookmarkRepository;
    private Bookmark bookmark;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        Long bookmarkId = getIntent().getLongExtra("mark_id", -1);

        bookmarkRepository = new BookmarkRepository(getApplication());
        bookmarkRepository.getBookmark(bookmarkId);

        bookmarkViewModel = new BookMarkViewModel();
        binding.setViewModel(bookmarkViewModel);
        binding.setLifecycleOwner(this);
        bookmark = bookmarkRepository.getBookmark(bookmarkId);

        bookmarkViewModel.setName(bookmark.getName());
        bookmarkViewModel.setNote(bookmark.getNote());
        bookmarkViewModel.setAddress(bookmark.getAddress());
        bookmarkViewModel.setPhoneNumber(bookmark.getPhone());
        bookmarkViewModel.setImage(bookmark.getImage(this));

    }
    @Override
    public void onBackPressed() {
        bookmark.setName(bookmarkViewModel.getName());
        bookmark.setNote(bookmarkViewModel.getNote());
        bookmark.setAddress(bookmarkViewModel.getAddress());
        bookmark.setPhone(bookmarkViewModel.getPhoneNumber());

        bookmarkRepository.updateBookmark(bookmark);
        setResult(RESULT_OK);

        super.onBackPressed();
    }
}