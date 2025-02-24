package com.example.googleMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class ImageUtils {
    public static void saveBitmapToFile(Context context, Bitmap bitmap, String filename) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes = stream.toByteArray();
        saveBytesTofile(context, bytes, filename);
    }

    public static void saveBytesTofile(Context context, byte[] byteArray, String filename) {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fileOutputStream.write(byteArray);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap loadBitmapFromFile(Context context, String filename) {
        String filePath = new File(context.getFilesDir(), filename).getAbsolutePath();
        return BitmapFactory.decodeFile(filePath);
    }

    public static File createUniqueImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = "PlaceBook_" + timeStamp + "_";
        File fileDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName, ".jpg", fileDir);
    }
}
