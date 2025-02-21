package com.example.googleMap;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Bookmark.class}, version = 1, exportSchema = false)
public abstract class BookmarkDatabase extends RoomDatabase {

    public abstract BookmarkDao bookmarkDao();

    private static BookmarkDatabase instance;
    public  static synchronized BookmarkDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(), BookmarkDatabase.class, "bookmark_db")
                    .allowMainThreadQueries()
                    .addCallback(new Callback() {
                        @Override
                        public void onOpen(@NonNull SupportSQLiteDatabase db) {
                            super.onOpen(db);
                            db.execSQL("PRAGMA foreign_keys=ON;");
                        }
                    })
                    .build();
        }
        return instance;
    }
}

