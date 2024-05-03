package android21ktpm3.group07.androidgallery.repositories;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.room.Room;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import android21ktpm3.group07.androidgallery.models.Album;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.models.remote.PhotoDetails;
import android21ktpm3.group07.androidgallery.ui.photos.LikedPhoto;
import android21ktpm3.group07.androidgallery.ui.photos.LikedPhotosDatabase;

public class PhotoRepository {
    private final String TAG = this.getClass().getSimpleName();
    private final Context context;
    private long favouriteID;
    private LikedPhotosDatabase database;

    private final ContentResolver contentResolver;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    @Nullable
    private FirebaseUser user;

    public PhotoRepository(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
        this.context = null;
    }



    public PhotoRepository(Context context) {

        this.context = context;
     //   database = Room.databaseBuilder(context, LikedPhotosDatabase.class, "liked-photos-db").build();
        this.contentResolver = null;
    }
    public PhotoRepository(Context context, LikedPhotosDatabase database) {
        this.database = database;
        this.context = context;
        //   database = Room.databaseBuilder(context, LikedPhotosDatabase.class, "liked-photos-db").build();
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
        database = Room.databaseBuilder(context, LikedPhotosDatabase.class, "liked_photos.db")
                .allowMainThreadQueries() // Only for demonstration. In a real app, perform database operations in background threads.
                .build();

        List<LikedPhoto> likedPhotos = database.likedPhotosDao().getAll();

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
                    boolean isInscreaseSize = true;
                    if (filePath.startsWith("/storage/emulated/0/Pictures/Favorites")) {
                        Album album = albumsMap.get(folder.getPath());
                        if (album != null && isInscreaseSize) {
                            album.setSize(likedPhotos.size());
                           // album.setFavouriteAlbumID(BucketID);
                            isInscreaseSize = false;
                        } else {
                            albumsMap.put(folder.getPath(), new Album(
                                    folder.getName(),
                                    folder.getPath(),
                                    filePath,
                                    fileDate,
                                    -1
                            ));
                        }
                    } else {
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
                MediaStore.Images.Media.DESCRIPTION,
                MediaStore.Images.Media.IS_FAVORITE


        };

        ArrayList<Photo> photos = new ArrayList<>();

        try (Cursor cursor = context.getContentResolver().query(collection, projection, null,
                null, null)) {
            if (cursor.moveToFirst()) {
                int IDColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                int PathColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int NameColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int ModifiedDateColumnIdx =
                        cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);
                int TakenDateColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                int fileSizeColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int tagsColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION);
                int favouriteIdx = cursor.getColumnIndex(MediaStore.Images.Media.IS_FAVORITE);

                do {
                    long id = cursor.getLong(IDColumnIdx);
                    String path = cursor.getString(PathColumnIdx);
                    String name = cursor.getString(NameColumnIdx);
                    long modifiedDate = cursor.getLong(ModifiedDateColumnIdx) * 1000; //  s to ms
                    long takenDate = cursor.getLong(TakenDateColumnIdx);
                    String tags = cursor.getString(tagsColumnIdx);
                    double fileSize = cursor.getDouble(fileSizeColumnIdx);
                    String isFavourite = cursor.getString(favouriteIdx);

                    if (path == null) continue;
                    photos.add(new Photo(id,path, name, modifiedDate, takenDate, tags, fileSize, isFavourite));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("PhotoRepository", e.toString());
        }

        return photos;
    }

    public ArrayList<Photo> getPhotosInAlbum(long albumBucketID) {
        ArrayList<Photo> photos = new ArrayList<>();


        if (albumBucketID == -1){
            database = Room.databaseBuilder(context, LikedPhotosDatabase.class, "liked_photos.db")
                    .allowMainThreadQueries() // Only for demonstration. In a real app, perform database operations in background threads.
                    .build();

            List<LikedPhoto> likedPhotos = database.likedPhotosDao().getAll();
            for (LikedPhoto likedPhoto : likedPhotos) {
                photos.add(new Photo(likedPhoto.getPhotoUrl()));
            }

            return photos;

        }

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
                MediaStore.Images.Media.DESCRIPTION,
                MediaStore.Images.Media.IS_FAVORITE

        };

       // ArrayList<Photo> photos = new ArrayList<>();

        try (Cursor cursor = context.getContentResolver().query(
                collection,
                projection,
                MediaStore.Images.Media.BUCKET_ID + " = ?",
                new String[]{String.valueOf(albumBucketID)},
                null)) {
            if (cursor.moveToFirst()) {
                int IDColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media._ID);

                int PathColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int NameColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int modifiedDateColumnIdx =
                        cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED);
                int takenDateColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                int fileSizeColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int favouriteIdx = cursor.getColumnIndex(MediaStore.Images.Media.IS_FAVORITE);

                int tagsColumnIdx = cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION);
                do {
                    long id = cursor.getLong(IDColumnIdx);
                    String path = cursor.getString(PathColumnIdx);
                    String name = cursor.getString(NameColumnIdx);
                    long modifiedDate = cursor.getLong(modifiedDateColumnIdx) * 1000; /// s to ms
                    long takenDate = cursor.getLong(takenDateColumnIdx);
                    String tags = cursor.getString(tagsColumnIdx);
                    double fileSize = cursor.getDouble(fileSizeColumnIdx);
                    String isFavourite = cursor.getString(favouriteIdx);

                    if (path == null) continue;
                    photos.add(new Photo(id,path, name, modifiedDate, takenDate, tags, fileSize,  isFavourite));
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


    public void setFirebaseUser(FirebaseUser user) {
        Log.d(TAG, "setFirebaseUser");
        this.user = user;
    }

    public ArrayList<PhotoDetails> getAllRemotePhotos() {
        if (user == null) {
            Log.d(TAG, "User is null");
            return new ArrayList<>();
        }

        Task<QuerySnapshot> fetchTask = db.collection("users")
                .document(user.getUid())
                .collection("images")
                .get();

        try {
            ArrayList<PhotoDetails> result = new ArrayList<>();

            QuerySnapshot querySnapshot = Tasks.await(fetchTask);
            for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
                result.add(documentSnapshot.toObject(PhotoDetails.class));
            }

            return result;

            // if (querySnapshot.exists()) {
            //     UserDocument userDocument = documentSnapshot.toObject(UserDocument.class);
            //     if (userDocument != null) {
            //         Log.d(TAG, "User document: " + userDocument.photos.size());
            //
            //         return userDocument.photos;
            //     }
            //
            // }
        } catch (ExecutionException | InterruptedException e) {
            Log.d(TAG, "Error getting remote photos", e);
        }

        return new ArrayList<>();
    }


    /**
     * Delete photos from the device
     *
     * @param photos List of photos to delete
     * @return List of photos that were successfully deleted
     */
    public List<Photo> deletePhotos(List<Photo> photos) {
        List<Photo> deletedPhotos = new ArrayList<>();
        for (Photo photo : photos) {
            Uri uri = Uri.fromFile(new File(photo.getPath()));
            try {
                context.getContentResolver().delete(
                        uri, null, null
                );
                deletedPhotos.add(photo);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting photo", e);
            }
        }

        return deletedPhotos;
    }

    public void test() {
        if (user == null) return;

        ArrayList<Photo> photos = GetAllPhotos();


        int count = 0;
        for (Photo photo : photos) {
            if (count == 2) break;

            Uri file = Uri.fromFile(new File(photo.getPath()));
            String fileName = file.getLastPathSegment();

            StorageReference imageRef = storage.getReference()
                    .child("user")
                    .child(user.getUid())
                    .child(System.currentTimeMillis() + "-" + fileName);

            UploadTask uploadTask = imageRef.putFile(file);
            uploadTask
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(TAG, "Image uploaded: " + fileName);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error uploading image: " + fileName, e);
                    }).addOnPausedListener(taskSnapshot -> {
                        Log.d(TAG, "Upload is paused: " + fileName);
                    });

            Task<Uri> getUrlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful())
                    throw Objects.requireNonNull(task.getException());

                return imageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Map<String, String> data = new HashMap<>();
                    data.put("local", photo.getPath());
                    data.put("remote", downloadUri.toString());

                    db.collection("users").document(user.getUid())
                            .update("images", FieldValue.arrayUnion(data))
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error storing image URL: " + fileName, e);
                            });
                } else {
                    Log.e(TAG, "Error getting image URL: " + fileName, task.getException());
                }
            });

            count += 1;
        }
    }
}
