package com.example.googleMap;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {
    public static void saveBitmapToFile(Context context, Bitmap bitmap, String fileName) {
        File directory = new File(context.getFilesDir(), "bookmark_images");
        if (!directory.exists()) {
            directory.mkdir();
        }
        File file = new File(directory, fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
