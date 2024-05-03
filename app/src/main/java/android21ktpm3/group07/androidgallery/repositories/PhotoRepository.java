package android21ktpm3.group07.androidgallery.repositories;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import android21ktpm3.group07.androidgallery.helpers.IntentSenderLauncher;
import android21ktpm3.group07.androidgallery.models.Album;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.models.remote.ImageDocument;
import android21ktpm3.group07.androidgallery.models.remote.PhotoDetails;
import android21ktpm3.group07.androidgallery.services.JobSchedulerService;
import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Remember to call destruct() when done using the repository
 */
@Singleton
public class PhotoRepository {
    private final String TAG = this.getClass().getSimpleName();
    private final Context context;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final MediaStoreChangedBroadcastReceiver mediaStoreChangedBroadcastReceiver;

    private final List<GetLocalPhotosCallback> getAllLocalPhotosCallbacks = new ArrayList<>();
    private final List<GetRemotePhotosCallback> getAllRemotePhotosCallbacks = new ArrayList<>();
    private final List<GetLocalPhotosCallback> getPhotosInAlbumCallbacks = new ArrayList<>();
    private final List<MediaChangedCallback> mediaChangedCallbacks = new ArrayList<>();

    @Nullable
    private FirebaseUser user;

    @Inject
    public PhotoRepository(@ApplicationContext Context context) {
        this.context = context;
        this.mediaStoreChangedBroadcastReceiver = new MediaStoreChangedBroadcastReceiver();

        LocalBroadcastManager.getInstance(context).registerReceiver(
                mediaStoreChangedBroadcastReceiver,
                new IntentFilter(JobSchedulerService.ACTION_MEDIA_STORE_CHANGED)
        );
    }

    public void destruct() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mediaStoreChangedBroadcastReceiver);
    }

    public void addGetAllLocalPhotosCallback(@NonNull GetLocalPhotosCallback callback) {
        getAllLocalPhotosCallbacks.add(callback);
    }

    public void removeGetAllLocalPhotosCallback(@NonNull GetLocalPhotosCallback callback) {
        getAllLocalPhotosCallbacks.remove(callback);
    }

    public void addGetAllRemotePhotosCallback(@NonNull GetRemotePhotosCallback callback) {
        getAllRemotePhotosCallbacks.add(callback);
    }

    public void removeGetAllRemotePhotosCallback(@NonNull GetRemotePhotosCallback callback) {
        getAllRemotePhotosCallbacks.remove(callback);
    }

    public void addGetPhotosInAlbumCallback(@NonNull GetLocalPhotosCallback callback) {
        getPhotosInAlbumCallbacks.add(callback);
    }

    public void removeGetPhotosInAlbumCallback(@NonNull GetLocalPhotosCallback callback) {
        getPhotosInAlbumCallbacks.remove(callback);
    }

    public void addMediaChangedCallback(@NonNull MediaChangedCallback callback) {
        mediaChangedCallbacks.add(callback);
    }

    public void removeMediaChangedCallback(@NonNull MediaChangedCallback callback) {
        mediaChangedCallbacks.remove(callback);
    }

    public ArrayList<Album> getAlbums() {
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

        try (
                Cursor cursor = context.getContentResolver().query(
                        collection,
                        projection,
                        null,
                        null,
                        null)
        ) {
            if (cursor != null && cursor.moveToFirst()) {
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

    private ArrayList<Photo> getLocalPhotos(Long albumBucketID) {
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

        String selection = null;
        String[] selectionArgs = null;
        if (albumBucketID != null) {
            selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
            selectionArgs = new String[]{String.valueOf(albumBucketID)};
        }

        ArrayList<Photo> photos = new ArrayList<>();
        try (Cursor cursor = context.getContentResolver().query(
                collection,
                projection,
                selection,
                selectionArgs,
                null)
        ) {
            if (cursor != null && cursor.moveToFirst()) {
                int idColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                int PathColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int NameColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int ModifiedDateColumnIdx =
                        cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);
                int TakenDateColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                int fileSizeColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int tagsColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION);

                do {
                    long id = cursor.getLong(idColumnIdx);
                    String path = cursor.getString(PathColumnIdx);
                    String name = cursor.getString(NameColumnIdx);
                    long modifiedDate = cursor.getLong(ModifiedDateColumnIdx) * 1000; //  s to ms
                    long takenDate = cursor.getLong(TakenDateColumnIdx);
                    String tags = cursor.getString(tagsColumnIdx);
                    double fileSize = cursor.getDouble(fileSizeColumnIdx);

                    Uri contentUri = ContentUris.withAppendedId(collection, id);

                    photos.add(new Photo(
                            path, name, modifiedDate, takenDate, tags, fileSize, contentUri
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PhotoRepository", e.toString());
        }

        return photos;
    }

    /**
     * Get all photos from the device
     * <p>
     * Callback: {@link #addGetAllLocalPhotosCallback(GetLocalPhotosCallback)}
     */
    public void getAllLocalPhotos() {
        ArrayList<Photo> result = getLocalPhotos(null);
        for (GetLocalPhotosCallback callback : getAllLocalPhotosCallbacks) {
            callback.onCompleted(result);
        }
    }

    /**
     * Get all photos from the device directly without using callback
     */
    public List<Photo> getAllLocalPhotosDirectly() {
        return getLocalPhotos(null);
    }

    public void getPhotosInAlbum(long albumBucketID) {
        ArrayList<Photo> result = getLocalPhotos(albumBucketID);
        for (GetLocalPhotosCallback callback : getPhotosInAlbumCallbacks) {
            callback.onCompleted(result);
        }
    }

    public ArrayList<Photo> getPhotosInAlbumDirectly(long albumBucketID) {
        return getLocalPhotos(albumBucketID);
    }

    public void updatePhoto(String photoPath, String newTags, long newModifiedDate,
                            double newFileSize) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DESCRIPTION, newTags);
        values.put(MediaStore.Images.Media.DATE_MODIFIED, newModifiedDate);
        values.put(MediaStore.Images.Media.SIZE, newFileSize);

        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = new String[]{photoPath};

        context.getContentResolver().update(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values,
                selection,
                selectionArgs);
    }

    public void setFirebaseUser(FirebaseUser user) {
        Log.d(TAG, "setFirebaseUser");
        this.user = user;
    }


    /**
     * @throws IllegalStateException when user is not logged in to Firebase
     */
    public List<PhotoDetails> getAllRemotePhotosDirectly() throws
            ExecutionException, InterruptedException {
        if (user == null) {
            Log.d(TAG, "User is null");
            throw new IllegalStateException("User is null");
        }

        Task<QuerySnapshot> fetchTask = db.collection("users")
                .document(user.getUid())
                .collection("images")
                .get();

        QuerySnapshot querySnapshot = Tasks.await(fetchTask);
        for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
            ImageDocument imageDocument = documentSnapshot.toObject(ImageDocument.class);
            return imageDocument.photos; // only 1 doc per collection atm
        }

        return new ArrayList<>();
    }


    /**
     * Get all photos from remote storage
     * <p>
     * Callback: {@link #addGetAllRemotePhotosCallback(GetRemotePhotosCallback)}
     */
    public void getAllRemotePhotos() {
        try {
            List<PhotoDetails> result = getAllRemotePhotosDirectly();

            for (GetRemotePhotosCallback callback : getAllRemotePhotosCallbacks) {
                callback.onCompleted(result);
            }
        } catch (ExecutionException | InterruptedException | IllegalStateException e) {
            for (GetRemotePhotosCallback callback : getAllRemotePhotosCallbacks) {
                callback.onFailed(e);
            }
        }
    }

    /**
     * @throws IllegalStateException when user is not logged in to Firebase
     */
    @Nullable
    public ImageDocumentResponse getImageDocument() throws
            ExecutionException, InterruptedException {
        if (user == null) {
            Log.d(TAG, "User is null");
            throw new IllegalStateException("User is null");
        }

        Task<QuerySnapshot> fetchTask = db.collection("users")
                .document(user.getUid())
                .collection("images")
                .get();

        QuerySnapshot querySnapshot = Tasks.await(fetchTask);
        for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
            // only 1 document per collection atm
            return new ImageDocumentResponse(
                    documentSnapshot.toObject(ImageDocument.class),
                    documentSnapshot.getReference()
            );
        }

        return null;
    }


    /**
     * Delete photos from the device
     *
     * @param photos   List of photos to delete
     * @param launcher IntentSenderLauncher to handle the delete request on Android R
     *                 and above
     * @param callback Callback to handle the result of the delete operation
     */
    @SuppressLint("NewApi")
    public void deleteLocalPhotos(List<Photo> photos, IntentSenderLauncher launcher,
                                  DeletePhotosCallback callback) {
        // createDeleteRequest is only available on Android R and above but we can use in on Q
        // since we opt in to the new storage model by setting
        // android:requestLegacyExternalStorage to false in the manifest
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PendingIntent pendingIntent = MediaStore.createDeleteRequest(
                    context.getContentResolver(),
                    photos.stream().map(Photo::getContentUri).collect(Collectors.toList())
            );
            IntentSenderRequest request =
                    new IntentSenderRequest.Builder(pendingIntent.getIntentSender())
                            .setFillInIntent(null)
                            .build();

            launcher.launch(request, new IntentSenderLauncher.IntentSenderResultCallback() {
                @Override
                public void onOK() {
                    if (callback != null) {
                        callback.onSucceed();
                    }
                }

                @Override
                public void onCanceled() {
                    if (callback != null) {
                        callback.onFailed(photos);
                    }
                }
            });
        } else {
            List<Photo> deletedPhotos = new ArrayList<>();
            for (Photo photo : photos) {
                int result = context.getContentResolver().delete(
                        photo.getContentUri(),
                        null,
                        null
                );

                if (result != 0) {
                    deletedPhotos.add(photo);
                }
            }

            if (callback != null) {
                if (deletedPhotos.size() == photos.size()) {
                    callback.onSucceed();
                } else {
                    callback.onFailed(deletedPhotos);
                }
            }
        }
    }

    public void createAlbum(String name) {
        // File folder = new File(Environment.DIRECTORY_PICTURES, name);
        //
        // if (folder.exists()) {
        //     return;
        // }
        //
        // if (folder.mkdirs()) {
        //     return folder;
        // } else {
        //     return null;
        // }
    }

    public void updateFileInMediaStore(String filePath) {
        MediaScannerConnection.scanFile(
                context,
                new String[]{filePath},
                null,
                (path, uri) -> {
                    Log.d(TAG + "::Storage", "Scanned " + path + ":");
                    Log.d(TAG + "::Storage", "-> uri=" + uri);
                });
    }

    @SuppressLint("NewApi")
    public void movePhotosToFolder(List<Photo> photos, String folder,
                                   IntentSenderLauncher launcher) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PendingIntent pendingIntent = MediaStore.createWriteRequest(
                    context.getContentResolver(),
                    photos.stream().map(Photo::getContentUri).collect(Collectors.toList())
            );
            IntentSenderRequest request =
                    new IntentSenderRequest.Builder(pendingIntent.getIntentSender())
                            .setFillInIntent(null)
                            .build();

            launcher.launch(request, new IntentSenderLauncher
                    .IntentSenderResultCallback() {
                @Override
                public void onOK() {
                    for (Photo photo : photos) {
                        movePhotoToFolder(photo.getContentUri(), photo.getName(), folder);
                    }
                }

                @Override
                public void onCanceled() {
                    Log.d(TAG, "onCanceled: ");
                }
            });
        } else {
            for (Photo photo : photos) {
                movePhotoToFolder(photo.getContentUri(), photo.getName(), folder);
            }
        }
    }

    public void copyPhotosToFolder(List<Uri> uris, String folder) {
        for (Uri uri : uris) {
            String path = getPathFromUri(uri);
            if (path != null) {
                String name = Paths.get(path).getFileName().toString();
                copyPhotoToFolder(uri, name, folder);
            }
        }
    }

    private boolean movePhotoToFolder(Uri uri, String name, String folder) {
        Path path = Paths.get(folder, name);
        try (
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                OutputStream out = Files.newOutputStream(path)
        ) {
            if (inputStream == null) {
                return false;
            }

            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();

            updateFileInMediaStore(path.toString());
        } catch (IOException e) {
            Log.e(TAG, "Failed to move file", e);
            return false;
        }

        int result = context.getContentResolver().delete(
                uri,
                null,
                null
        );

        return result != 0;
    }

    private boolean copyPhotoToFolder(Uri uri, String name, String folder) {
        Path path = Paths.get(folder, name);
        try (
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                OutputStream out = Files.newOutputStream(path)
        ) {
            if (inputStream == null) {
                return false;
            }

            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();

            updateFileInMediaStore(path.toString());
        } catch (IOException e) {
            Log.e(TAG, "Failed to move file", e);
            return false;
        }

        return true;
    }

    @Nullable
    private String getPathFromUri(Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{MediaStore.Images.Media.DATA},
                null,
                null,
                null)
        ) {
            if (cursor != null && cursor.moveToFirst()) {
                int PathColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                String text = cursor.getString(PathColumnIdx);
                return text;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to query MediaStore", e);
        }

        return null;
    }

    public interface GetLocalPhotosCallback {
        void onCompleted(List<Photo> photos);
    }

    public interface GetRemotePhotosCallback {
        void onCompleted(List<PhotoDetails> photos);

        void onFailed(Exception e);
    }

    public interface MediaChangedCallback {
        void onAdded(Photo photo, long AlbumBucketID);

        void onDeleted(Uri uri);
    }

    public interface DeletePhotosCallback {
        void onSucceed();

        void onFailed(List<Photo> deletedPhotos);
    }

    public static class ImageDocumentResponse {
        public ImageDocument imageDocument;
        public DocumentReference documentRef;

        public ImageDocumentResponse(ImageDocument imageDocument,
                                     DocumentReference documentReference) {
            this.imageDocument = imageDocument;
            this.documentRef = documentReference;
        }
    }

    private class MediaStoreChangedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String uriStr = intent.getStringExtra("uri");
            if (uriStr != null) {
                Uri uri = Uri.parse(uriStr);

                String[] projection = new String[]{
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_MODIFIED,
                        MediaStore.Images.Media.DATE_TAKEN,
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.DESCRIPTION,
                        MediaStore.Images.Media.BUCKET_ID
                };

                try (Cursor cursor = context.getContentResolver().query(
                        uri,
                        projection,
                        null,
                        null,
                        null)
                ) {
                    if (cursor != null && cursor.moveToFirst()) {
                        Log.d(TAG, "MediaStore added: " + uri);

                        int PathColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media
                                .DATA);
                        int NameColumnIdx =
                                cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                        int ModifiedDateColumnIdx =
                                cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);
                        int TakenDateColumnIdx =
                                cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                        int fileSizeColumnIdx = cursor.getColumnIndex(MediaStore.Images
                                .Media.SIZE);
                        int tagsColumnIdx =
                                cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION);
                        int bucketIdIdx = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);

                        String path = cursor.getString(PathColumnIdx);
                        String name = cursor.getString(NameColumnIdx);
                        long modifiedDate = cursor.getLong(ModifiedDateColumnIdx) * 1000;
                        long takenDate = cursor.getLong(TakenDateColumnIdx);
                        String tags = cursor.getString(tagsColumnIdx);
                        double fileSize = cursor.getDouble(fileSizeColumnIdx);
                        long bucketId = cursor.getLong(bucketIdIdx);

                        for (MediaChangedCallback callback : mediaChangedCallbacks) {
                            callback.onAdded(
                                    new Photo(
                                            path, name, modifiedDate, takenDate, tags, fileSize, uri
                                    ),
                                    bucketId
                            );
                        }
                    } else {
                        Log.d(TAG, "MediaStore deleted: " + uri);

                        for (MediaChangedCallback callback : mediaChangedCallbacks) {
                            callback.onDeleted(uri);
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to query MediaStore", e);
                }
            }
        }
    }
}
