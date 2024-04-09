package android21ktpm3.group07.androidgallery.repositories;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import android21ktpm3.group07.androidgallery.models.Album;
import android21ktpm3.group07.androidgallery.models.Photo;

public class PhotoRepository {
    private final Context context;
    private final ContentResolver contentResolver;

    public PhotoRepository(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
        this.context = null;
    }


    public PhotoRepository(Context context) {

        this.context = context;
        this.contentResolver = null;
    }

    public ArrayList<Album> GetAlbums() {
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        String[] projection = new String[]{
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.BUCKET_ID

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
                int BucketIDColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                do {
                    String filePath = cursor.getString(PathColumnIdx);
                    long BucketID = cursor.getLong(BucketIDColumnIdx);
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
                                fileDate,
                                BucketID
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

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DESCRIPTION

        };

        ArrayList<Photo> photos = new ArrayList<>();

        try (Cursor cursor = context.getContentResolver().query(collection, projection, null,
                null, null)) {
            if (cursor.moveToFirst()) {
                int PathColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int NameColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int ModifiedDateColumnIdx =
                        cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);
                int TakenDateColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                int fileSizeColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int tagsColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION);
                do {
                    String path = cursor.getString(PathColumnIdx);
                    String name = cursor.getString(NameColumnIdx);
                    long modifiedDate = cursor.getLong(ModifiedDateColumnIdx) * 1000; //  s to ms
                    long takenDate = cursor.getLong(TakenDateColumnIdx);
                    String tags = cursor.getString(tagsColumnIdx);
                    double fileSize = cursor.getDouble(fileSizeColumnIdx);
                    if (path == null) continue;
                    photos.add(new Photo(path, name, modifiedDate, takenDate, tags, fileSize));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PhotoRepository", e.toString());
        }

        return photos;
    }

    public ArrayList<Photo> getPhotosInAlbum(long albumBucketID) {
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DESCRIPTION
        };

        ArrayList<Photo> photos = new ArrayList<>();

        try (Cursor cursor = context.getContentResolver().query(
                collection,
                projection,
                MediaStore.Images.Media.BUCKET_ID + " = ?",
                new String[]{String.valueOf(albumBucketID)},
                null)) {
            if (cursor.moveToFirst()) {
                int PathColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int NameColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int modifiedDateColumnIdx =
                        cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);
                int takenDateColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                int fileSizeColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int tagsColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION);
                do {
                    String path = cursor.getString(PathColumnIdx);
                    String name = cursor.getString(NameColumnIdx);
                    long modifiedDate = cursor.getLong(modifiedDateColumnIdx) * 1000; /// s to ms
                    long takenDate = cursor.getLong(takenDateColumnIdx);
                    String tags = cursor.getString(tagsColumnIdx);
                    double fileSize = cursor.getDouble(fileSizeColumnIdx);
                    if (path == null) continue;
                    photos.add(new Photo(path, name, modifiedDate, takenDate, tags, fileSize));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PhotoRepository", e.toString());
        }
        return photos;
    }

    public void updatePhoto(String photoPath, String newTags, long newModifiedDate,
                            double newFileSize) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DESCRIPTION, newTags);
        values.put(MediaStore.Images.Media.DATE_MODIFIED, newModifiedDate);
        values.put(MediaStore.Images.Media.SIZE, newFileSize);

        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = new String[]{photoPath};

        contentResolver.update(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values, selection,
                selectionArgs);
    }
}
