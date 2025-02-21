package com.example.googleMap;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BookmarkDao {
    @Insert
    Long insertBookmark(Bookmark bookmark);

    @Query("SELECT * FROM bookmarks")
    LiveData<List<Bookmark>> loadAll();

    @Query("SELECT * FROM bookmarks WHERE id = :bookmarkId")
    LiveData<Bookmark> loadLiveBookmark(Long bookmarkId);

    @Update
    void updateBookmark(Bookmark bookmark);

    @Query("SELECT * FROM bookmarks WHERE id = :bookmarkId")
    Bookmark loadBookmark(Long bookmarkId);

    @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
    void deleteBookmark(Long bookmarkId);

    @Query("DELETE FROM bookmarks")
    void deleteAllBookmarks();

    @Query("SELECT * FROM bookmarks WHERE name LIKE '%' || :name || '%'")
    LiveData<List<Bookmark>> findBookmarksByTitle(String name);

    @Query("SELECT COUNT(*) FROM bookmarks WHERE id = :bookmarkId")
    int doesBookmarkExist(Long bookmarkId);
}

