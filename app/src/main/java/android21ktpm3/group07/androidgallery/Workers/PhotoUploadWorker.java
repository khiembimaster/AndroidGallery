package android21ktpm3.group07.androidgallery.Workers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.models.remote.ImageDocument;
import android21ktpm3.group07.androidgallery.models.remote.PhotoDetails;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class PhotoUploadWorker extends Worker {
    private final FirebaseFirestore db;
    private final FirebaseUser user;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final String TAG = "PhotoUploadWorker";
    private static final String TOTAL_COUNT_KEY = "total_count";
    private final ImageLabeler labeler;
    private long count = 0;

    private final DocumentReference userRef;

    public PhotoUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        // Or, to set the minimum confidence required:
        ImageLabelerOptions options =
                new ImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.5f)
                        .build();
        labeler = ImageLabeling.getClient(options);
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            throw new IllegalStateException("User is not logged in");
        }
        userRef = db.collection("users").document(user.getUid());
    }

    @NonNull
    @Override
    public Result doWork() {
        PhotoRepository photoRepository = new PhotoRepository(getApplicationContext());
        photoRepository.setFirebaseUser(user);
        List<Photo> localPhotos = photoRepository.GetAllPhotos();

        CollectionReference userImagesRef = userRef.collection("images");

        boolean test = true;

        try {
            ImageDocument imageDocument;
            DocumentReference imageDocumentRef;
            ArrayList<Photo> photosToUpload = new ArrayList<>();

            PhotoRepository.ImageDocumentReponse response = photoRepository.getImageDocument();

            if (test && response != null) {
                Tasks.await(response.documentRef.delete());
                response = null;
            }

            if (response != null) {
                imageDocumentRef = response.documentRef;
                imageDocument = response.imageDocument;
                List<PhotoDetails> remotePhotos = imageDocument.photos;

                HashSet<String> remotePhotosMap = new HashSet<>(
                        (int) Math.ceil(remotePhotos.size() / 0.75)
                );
                for (PhotoDetails remotePhoto : remotePhotos) {
                    remotePhotosMap.add(remotePhoto.localPath);
                }

                for (Photo localPhoto : localPhotos) {
                    if (!remotePhotosMap.contains(localPhoto.getPath())) {
                        photosToUpload.add(localPhoto);
                    }
                }
            } else {
                imageDocumentRef = null;
                imageDocument = new ImageDocument();
                photosToUpload.addAll(localPhotos);
            }

            if (photosToUpload.isEmpty()) {
                Log.d(TAG, "No photos to upload");
                return Result.success();
            }

            count = photosToUpload.size();
            setProgressAsync(new Data.Builder()
                    .putLong(TOTAL_COUNT_KEY, count)
                    .build());

            ArrayList<Task<Uri>> uploadingTasks = new ArrayList<>();
            for (Photo photo : photosToUpload) {
                Task<Uri> task = uploadImage(Uri.fromFile(new File(photo.getPath())));
                task.addOnSuccessListener(uri -> {
                    imageDocument.photos.add(new PhotoDetails(
                            photo.getPath(),
                            uri.toString(),
                            photo.getName()
                    ));
                    PhotoUploadWorker.this.setProgressAsync(new Data.Builder()
                            .putLong(TOTAL_COUNT_KEY, --count)
                            .build());
                    Log.d(TAG, "progress: " + count);
                });

                uploadingTasks.add(task);
            }

            Task<Void> finishedUploadingTask = Tasks.whenAll(uploadingTasks)
                    .addOnSuccessListener(aVoid -> {
                        // Switch all the time to using server time
                        imageDocument.updatedAt = Date.from(Instant.now());
                        if (imageDocumentRef != null) {
                            imageDocumentRef.set(imageDocument).addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Finished uploading images: "
                                        + imageDocument.photos.size() + "/" + photosToUpload.size());
                            });
                        } else {
                            userImagesRef.add(imageDocument).addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Finished uploading images: "
                                        + imageDocument.photos.size() + "/" + photosToUpload.size());
                            });
                        }
                    });

            Task<Void> labellingTask = finishedUploadingTask.continueWith(task -> {
                if (!task.isSuccessful()) return null;

                for (Photo photo : photosToUpload) {
                    labelImage(Uri.fromFile(new File(photo.getPath())));
                }

                return null;
            });
            labellingTask.addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Finished labelling images");
            });

            Tasks.await(labellingTask);

        } catch (ExecutionException e) {
            Log.e(TAG, "Failure", e);
            return Result.failure();
        } catch (InterruptedException e) {
            Log.e(TAG, "Retry", e);
            return Result.retry();
        }

        Log.d(TAG, "Finished uploading and labelling images");
        return Result.success();
    }

    // TODO: Create a service to upload images to Firebase Storage
    private Task<Uri> uploadImage(Uri file) {
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        // Create file metadata including the content type

        // Upload file to Firebase Storage
        StorageReference imageRef = storageRef.child("user")
                .child(user.getUid())
                .child(System.currentTimeMillis() + "-" + file.getLastPathSegment());
        UploadTask uploadTask = imageRef.putFile(file);
        uploadTask.addOnFailureListener(e -> {
            Log.e(TAG, "Error when upload your images", e);
        }).addOnSuccessListener(taskSnapshot -> {
            Log.d(TAG, "Your images stored successfully!");
        }).addOnPausedListener(taskSnapshot -> {
            Log.d(TAG, "Upload is paused");
        });

        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                return null;
            }
            return imageRef.getDownloadUrl();
        });
        return urlTask;
    }

    private void labelImage(Uri file) throws ExecutionException, InterruptedException {
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(file)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<?
                            super Bitmap> transition) {
                        // do something with the resource
                        InputImage image = InputImage.fromBitmap(resource, 0);
                        labeler.process(image)
                                .addOnSuccessListener(labels -> {
                                    for (ImageLabel label : labels) {
                                        String text = label.getText();
                                        float confidence = label.getConfidence();

                                        Log.d(TAG,
                                                "Label: " + text + " with confidence: " + confidence);

                                        Map<String, Object> data = new HashMap<>();
                                        data.put("images", FieldValue.arrayUnion(file.toString()));


                                        userRef.collection("tags").document(text)
                                                .set(data, SetOptions.merge());
                                    }
                                }).addOnFailureListener(e -> {
                                    Log.e(TAG, "Error when label your images", e);
                                });
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

    }
}
