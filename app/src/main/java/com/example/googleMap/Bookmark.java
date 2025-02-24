package com.example.googleMap;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.File;
import java.io.IOException;

@Entity(tableName = "bookmarks")
public class Bookmark{
    @PrimaryKey(autoGenerate = true)
    public Long id;
    public String placeId;
    public String name;
    public String address;
    public Double latitude;
    public Double longitude;
    public String phone;
    public String note;

    public Bookmark(Long id, String placeId, String name, String address, Double latitude, Double longitude, String phone, String note) {
        this.id = id;
        this.placeId = placeId;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setImage(Bitmap image, Context context) {
        if (id != null) {
            ImageUtils.saveBitmapToFile(context, image, generateFileName(id));
        }
    }

    public Bitmap getImage(Context context) {
        if (id != null) {
            return ImageUtils.loadBitmapFromFile(context, generateFileName(id));
        }
        return null;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public static String generateFileName(Long id) {
        return "bookmark_" + id + ".png";
    }
    public File createImageFile(Context context) throws IOException {
        return ImageUtils.createUniqueImageFile(context);
    }
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getPhone() {
        return phone;
    }

    public String getNote() {
        return note;
    }
}
