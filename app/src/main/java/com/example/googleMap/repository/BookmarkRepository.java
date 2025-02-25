package com.example.googleMap.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.googleMap.db.BookmarkDatabase;
import com.example.googleMap.model.Bookmark;
import com.example.googleMap.db.BookmarkDao;

import java.util.List;

public class BookmarkRepository {
    private final BookmarkDao bookmarkDao;

    public BookmarkRepository(Application application) {
        BookmarkDatabase db = BookmarkDatabase.getInstance(application);
        bookmarkDao = db.bookmarkDao();
    }

    public Long addBookmark(Bookmark bookmark) {
        return bookmarkDao.insertBookmark(bookmark);
    }
//    public Bookmark createBookmark() {
//        return new Bookmark();
//    }
    public LiveData<List<Bookmark>> allBookmarks() {
        return bookmarkDao.loadAll();
    }
    public LiveData<Bookmark> getLiveBookmark(Long bookmarkId){
        return bookmarkDao.loadLiveBookmark(bookmarkId);
    }

    public void updateBookmark(Bookmark bookmark){
        bookmarkDao.updateBookmark(bookmark)
        ;
    }
    public Bookmark getBookmark(Long bookmarkId){
        return bookmarkDao.loadBookmark(bookmarkId);
    }
}
