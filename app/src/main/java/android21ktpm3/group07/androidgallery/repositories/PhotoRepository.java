package android21ktpm3.group07.androidgallery.repositories;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import android21ktpm3.group07.androidgallery.models.Album;
import android21ktpm3.group07.androidgallery.models.Photo;

public class PhotoRepository {
    private final Context context;

    public PhotoRepository(Context context) {
        this.context = context;
    }

    public ArrayList<Album> GetAlbums() {
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        String[] projection = new String[] {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_MODIFIED,
        };

        HashMap<String, Album> albumsMap = new LinkedHashMap<>();

        try (Cursor cursor = context.getContentResolver().query(
                collection,
                projection,
                null,
                null,
                null)) {
            if (cursor.moveToFirst()) {
                int PathColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int DateColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);
                do {
                    String filePath = cursor.getString(PathColumnIdx);
                    long fileDate = cursor.getLong(DateColumnIdx) * 1000;

                    File folder = new File(filePath).getParentFile();

                    Album album = albumsMap.get(folder.getPath());
                    if (album != null) {
                        album.setSize(album.getSize() + 1);
                        if (album.getLastModifiedDate() < fileDate) {
                            album.setLastModifiedDate(fileDate);
                            album.setCoverPhotoPath(filePath);
                        }
                    } else {
                        albumsMap.put(folder.getPath(), new Album(
                                folder.getName(),
                                folder.getPath(),
                                filePath,
                                fileDate
                        ));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PhotoRepository", e.toString());
        }

        return albumsMap.values().stream()
                .sorted(Comparator.comparingLong(Album::getLastModifiedDate).reversed())
                .collect(Collectors.toCollection(ArrayList::new));
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
