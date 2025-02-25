package com.example.googleMap.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.googleMap.model.Bookmark;
import com.example.googleMap.repository.BookmarkRepository;
import com.example.googleMap.util.ImageUtils;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookmarkViewModel extends AndroidViewModel {
    private LiveData<BookmarkView> bookmarkDetailsView;
    private BookmarkRepository bookmarkRepo = new BookmarkRepository(getApplication());

    public BookmarkViewModel(Application application) {
        super(application);
    }

    public static class  BookmarkView{
        public Long id;
        public String name;
        public String phone;
        public String address;
        public String notes;

        public BookmarkView(Long id, String name, String phone, String address, String notes) {
            this.id = id;
            this.name = name;
            this.phone = phone;
            this.address = address;
            this.notes = notes;
        }

        public Bitmap getImage(Context context) {
            if (id != null) {
                return ImageUtils.loadBitmapFromFile(context, Bookmark.generateFileName(id));
            }
            return null;

        }

        public void setImage(Context context, Bitmap image) {
            if (id != null) {
                ImageUtils.saveBitmapToFile(context, image, Bookmark.generateFileName(id));
            }
        }


    }

    public BookmarkView bookmarkToBookmarkView(Bookmark bookmark){
        return new BookmarkView(bookmark.id, bookmark.name, bookmark.phone, bookmark.note, bookmark.address );
    }

    public Bookmark bookmarkViewToBookmark(BookmarkView bookmarkView){
        Bookmark bookmark = null;
        if(bookmarkView.id != null){
            bookmark = bookmarkRepo.getBookmark(bookmarkView.id);
        }
        if(bookmark != null){
            bookmark.id = bookmarkView.id;
            bookmark.name = bookmarkView.name;
            bookmark.phone = bookmarkView.phone;
            bookmark.address = bookmarkView.address;
            bookmark.note = bookmarkView.notes;
        }
        return bookmark;
    }

    public void updateBookmark(BookmarkView bookmarkView) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
                    Bookmark bookmark = bookmarkViewToBookmark(bookmarkView);
                    if (bookmark != null ) {
                        bookmarkRepo.updateBookmark(bookmark);
                    }
                    executor.shutdown();
                }
        );
    }

    private void mapBookmarkToBookmarkView(Long bookmarkId) {
        LiveData<Bookmark> bookmark = bookmarkRepo.getLiveBookmark(bookmarkId);
        bookmarkDetailsView = Transformations.map(bookmark, (repoBookmark) -> {
            return bookmarkToBookmarkView(repoBookmark);
        });
    }

    public LiveData<BookmarkView> getBookmark(Long bookmarkId) {
        if (bookmarkDetailsView == null) {
            mapBookmarkToBookmarkView(bookmarkId);
        }
        return bookmarkDetailsView;
    }

}
