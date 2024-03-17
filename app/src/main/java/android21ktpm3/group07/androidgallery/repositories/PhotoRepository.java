package android21ktpm3.group07.androidgallery.repositories;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import android21ktpm3.group07.androidgallery.models.Photo;

public class PhotoRepository {
    private final Context context;

    public PhotoRepository(Context context) {
        this.context = context;
    }

    public LinkedHashSet<String> GetFolders() {
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        String[] projection = {
                MediaStore.Images.ImageColumns.DATA,
        };
        String sort = String.format("%s DESC", MediaStore.Images.ImageColumns._ID);

        LinkedHashSet<String> paths = new LinkedHashSet<>();

        try (Cursor cursor = context.getContentResolver().query(collection, projection, null, null, sort)) {
            if (cursor.moveToFirst()) {
                int PathColumnIdx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                do {
                    String path = cursor.getString(PathColumnIdx);
                    if (path == null) continue;

                    paths.add(Paths.get(path).getParent().toString());
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PhotoRepository", e.toString());
        }

        return paths;
    }

    public ArrayList<Photo> GetAllPhotos() {
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        String[] projection = new String[] {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED,
        };

        ArrayList<Photo> photos = new ArrayList<>();

        try (Cursor cursor = context.getContentResolver().query(collection, projection, null, null, null)) {
            if (cursor.moveToFirst()) {
                int PathColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int NameColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int DateColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);
                do {
                    String path = cursor.getString(PathColumnIdx);
                    String name = cursor.getString(NameColumnIdx);
                    long date = cursor.getLong(DateColumnIdx);
                    if (path == null) continue;
                    photos.add(new Photo(path, name, date * 1000));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PhotoRepository", e.toString());
        }
        return photos;
    }
}
