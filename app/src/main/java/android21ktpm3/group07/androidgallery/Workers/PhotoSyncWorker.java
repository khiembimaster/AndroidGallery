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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.models.remote.ImageDocument;
import android21ktpm3.group07.androidgallery.models.remote.PhotoDetails;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class PhotoSyncWorker extends Worker {
    private static final String TAG = PhotoSyncWorker.class.getSimpleName();
    private static final String TOTAL_COUNT_KEY = "total_count";
    private static final float LABEL_CONFIDENT_THRESHOLD = 0.5f;

    private final ImageLabeler imageLabeler;
    private final FirebaseUser user;
    private final DocumentReference userDocRef;
    private final StorageReference storageRef;
    private final PhotoRepository photoRepository;

    private int numImagesUploaded;

    public PhotoSyncWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);

        ImageLabelerOptions options =
                new ImageLabelerOptions.Builder()
                        .setConfidenceThreshold(LABEL_CONFIDENT_THRESHOLD)
                        .build();
        imageLabeler = ImageLabeling.getClient(options);


        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User is not logged in");
        }

        userDocRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid());
        storageRef = FirebaseStorage.getInstance().getReference()
                .child("user")
                .child(user.getUid());

        photoRepository = new PhotoRepository(appContext);
        photoRepository.setFirebaseUser(user);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            List<Photo> localPhotos = photoRepository.GetAllPhotos();

            boolean test = true;

            CollectionReference imagesCollRef = userDocRef.collection("images");
            ImageDocument imageDocument;
            DocumentReference imageDocumentRef;
            ArrayList<Photo> photosToUpload = new ArrayList<>();
            ArrayList<PhotoDetails> photosToDownload = new ArrayList<>();

            PhotoRepository.ImageDocumentReponse response = photoRepository.getImageDocument();

            if (test && response != null) {
                Tasks.await(response.documentRef.delete());
                response = null;
            }

            if (response != null) {
                imageDocumentRef = response.documentRef;
                imageDocument = response.imageDocument;
                List<PhotoDetails> remotePhotos = imageDocument.photos;

                HashMap<String, PhotoDetailsInLocal> remotePhotosMap =
                        new HashMap<>((int) Math.ceil(remotePhotos.size() / 0.75));
                for (PhotoDetails remotePhoto : remotePhotos) {
                    remotePhotosMap.put(
                            remotePhoto.localPath,
                            new PhotoDetailsInLocal(remotePhoto)
                    );
                }

                for (Photo localPhoto : localPhotos) {
                    PhotoDetailsInLocal remotePhoto = remotePhotosMap.get(localPhoto.getPath());
                    if (remotePhoto != null) {
                        photosToUpload.add(localPhoto);
                        remotePhoto.isLocallyAvailable = true;
                    }
                }

                for (PhotoDetailsInLocal p : remotePhotosMap.values()) {
                    if (!p.isLocallyAvailable) {
                        photosToDownload.add(p.photoDetails);
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

            numImagesUploaded = photosToUpload.size();
            setProgressAsync(new Data.Builder()
                    .putLong(TOTAL_COUNT_KEY, numImagesUploaded)
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
                    PhotoSyncWorker.this.setProgressAsync(new Data.Builder()
                            .putLong(TOTAL_COUNT_KEY, --numImagesUploaded)
                            .build());
                });

                uploadingTasks.add(task);
            }

            // TODO: process when some uploading task fails
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
                            imagesCollRef.add(imageDocument).addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Finished uploading images: "
                                        + imageDocument.photos.size() + "/" + photosToUpload.size());
                            });
                        }
                    });

            List<CompletableFuture<Void>> labellingFutures = new ArrayList<>();
            Task<Void> labellingTask = finishedUploadingTask.continueWith(task -> {
                if (!task.isSuccessful()) return null;

                Log.d(TAG, Thread.currentThread().getName());
                for (Photo photo : photosToUpload) {
                    CompletableFuture<Void> future = new CompletableFuture<>();
                    labelImage(Uri.fromFile(new File(photo.getPath())), future);
                    labellingFutures.add(future);
                }

                return null;
            });

            // We have to do this outside because the firebase task listener is
            // called in the main thread (where it is initialized)
            Tasks.await(labellingTask);
            CompletableFuture.allOf(labellingFutures.toArray(new CompletableFuture[0]))
                    .join();
            Log.d(TAG, "Finished labelling photos");
        } catch (ExecutionException | InterruptedException e) {
            Log.d(TAG, "Error when syncing photos", e);
            return Result.failure();
        }

        return Result.success();
    }

    private Task<Uri> uploadImage(Uri file) {
        StorageReference imageRef = storageRef
                .child(System.currentTimeMillis() + "-" + file.getLastPathSegment());

        UploadTask uploadTask = imageRef.putFile(file);
        uploadTask.addOnFailureListener(e -> {
            Log.e(TAG, "Error when upload your images", e);
        }).addOnSuccessListener(taskSnapshot -> {
            Log.d(TAG, "Your images stored successfully!");
        }).addOnPausedListener(taskSnapshot -> {
            Log.d(TAG, "Upload is paused");
        });

        return uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                return null;
            }
            return imageRef.getDownloadUrl();
        });
    }

    private void labelImage(Uri file, CompletableFuture<Void> future) {
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(file)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<?
                            super Bitmap> transition) {
                        // do something with the resource
                        InputImage image = InputImage.fromBitmap(resource, 0);
                        imageLabeler.process(image)
                                .addOnSuccessListener(labels -> {
                                    for (ImageLabel label : labels) {
                                        String text = label.getText();

                                        Map<String, Object> data = new HashMap<>();
                                        data.put("images", FieldValue.arrayUnion(file.toString()));

                                        userDocRef.collection("tags").document(text)
                                                .set(data, SetOptions.merge());
                                    }
                                    future.complete(null);
                                }).addOnFailureListener(e -> {
                                    Log.e(TAG, "Error when labelling your images", e);
                                    future.completeExceptionally(e);
                                });
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });


    }

    private interface Callback {
        void onFailure(Exception e);

        void onSuccess();
    }

    private static class PhotoDetailsInLocal {
        PhotoDetails photoDetails;
        boolean isLocallyAvailable;

        public PhotoDetailsInLocal(PhotoDetails photoDetails) {
            this.photoDetails = photoDetails;
            this.isLocallyAvailable = false;
        }
    }
}
