package android21ktpm3.group07.androidgallery.repositories;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
import android21ktpm3.group07.androidgallery.models.remote.ImageDocument;
import android21ktpm3.group07.androidgallery.models.remote.PhotoDetails;

public class PhotoRepository {
    private final String TAG = this.getClass().getSimpleName();
    private final Context context;
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

    public void setFirebaseUser(FirebaseUser user) {
        Log.d(TAG, "setFirebaseUser");
        this.user = user;
    }


    /**
     * @throws IllegalStateException when user is not logged in to Firebase
     */
    public List<PhotoDetails> getAllRemotePhotos() throws
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
     * @throws IllegalStateException when user is not logged in to Firebase
     */
    @Nullable
    public ImageDocumentReponse getImageDocument() throws
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
            return new ImageDocumentReponse(
                    documentSnapshot.toObject(ImageDocument.class),
                    documentSnapshot.getReference()
            );
        }

        return null;
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

    public class ImageDocumentReponse {
        public ImageDocument imageDocument;
        public DocumentReference documentRef;

        public ImageDocumentReponse(ImageDocument imageDocument,
                                    DocumentReference documentReference) {
            this.imageDocument = imageDocument;
            this.documentRef = documentReference;
        }
    }
}
