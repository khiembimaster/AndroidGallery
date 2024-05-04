package android21ktpm3.group07.androidgallery.Workers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.hilt.work.HiltWorker;
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
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class PhotoSyncWorker extends Worker {
    private static final String TAG = PhotoSyncWorker.class.getSimpleName();
    private static final String TOTAL_COUNT_KEY = "total_count";
    private static final float LABEL_CONFIDENT_THRESHOLD = 0.5f;

    private final ImageLabeler imageLabeler;
    private final FirebaseUser user;
    private final DocumentReference userDocRef;
    private final StorageReference storageRef;
    private final PhotoRepository photoRepository;

    private int numImagesToUpload;
    private int numImagesUploaded;

    @AssistedInject
    public PhotoSyncWorker(
            @Assisted @NonNull Context appContext,
            @Assisted @NonNull WorkerParameters workerParams,
            PhotoRepository photoRepository
    ) {
        super(appContext, workerParams);

        this.photoRepository = photoRepository;

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
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            List<Photo> localPhotos = photoRepository.getAllLocalPhotosDirectly();

            boolean test = false;

            CollectionReference imagesCollRef = userDocRef.collection("images");
            DocumentReference imageDocumentRef;
            ImageDocument imageDocument;
            ArrayList<Photo> photosToUpload = new ArrayList<>();
            ArrayList<PhotoDetails> photosToDownload = new ArrayList<>();

            PhotoRepository.ImageDocumentResponse response = photoRepository.getImageDocument();

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
                    if (remotePhoto == null) {
                        photosToUpload.add(localPhoto);
                    } else {
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

            Log.d(TAG, "Photos to upload: " + photosToUpload.size());
            Log.d(TAG, "Photos to download: " + photosToDownload.size());


            // Downloading
            List<CompletableFuture<Void>> downloadingFutures = new ArrayList<>();
            for (PhotoDetails photo : photosToDownload) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                downloadImage(photo, future);
                downloadingFutures.add(future);
            }

            // Uploading
            numImagesToUpload = photosToUpload.size();
            setProgressAsync(new Data.Builder()
                    .putLong(TOTAL_COUNT_KEY, numImagesToUpload)
                    .build());

            long curTime = System.currentTimeMillis();

            ArrayList<Task<Uri>> uploadingTasks = new ArrayList<>();
            for (Photo photo : photosToUpload) {
                String remoteName = curTime + "-" + photo.getName();

                Task<Uri> task = uploadImage(
                        Uri.fromFile(new File(photo.getPath())),
                        remoteName
                );
                task.addOnSuccessListener(uri -> {
                    imageDocument.photos.add(new PhotoDetails(
                            photo.getPath(),
                            uri.toString(),
                            new Date(photo.getModifiedDate())
                    ));
                    PhotoSyncWorker.this.setProgressAsync(new Data.Builder()
                            .putLong(TOTAL_COUNT_KEY, --numImagesToUpload)
                            .build());
                    ++numImagesUploaded;
                });

                uploadingTasks.add(task);
            }

            // TODO: process when some uploading task fails
            Task<Void> finishedUploadingTask = Tasks.whenAll(uploadingTasks)
                    .addOnSuccessListener(aVoid -> {
                        // Switch all the time to using server time
                        if (imageDocumentRef != null) {
                            imageDocumentRef.set(imageDocument).addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Finished uploading images: "
                                        + numImagesUploaded + "/" + photosToUpload.size());
                            });
                        } else {
                            imagesCollRef.add(imageDocument).addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Finished uploading images: "
                                        + numImagesUploaded + "/" + photosToUpload.size());
                            });
                        }
                    });


            // Labelling
            List<CompletableFuture<Void>> labellingFutures = new ArrayList<>();
            Task<Void> labellingTask = finishedUploadingTask.continueWith(task -> {
                if (!task.isSuccessful()) return null;

                for (Photo photo : photosToUpload) {
                    CompletableFuture<Void> future = new CompletableFuture<>();
                    labelImage(Uri.fromFile(new File(photo.getPath())), future);
                    labellingFutures.add(future);
                }

                return null;
            });

            // We have to do this outside because the firebase task listener is
            // executed in the main thread (where it is initialized)
            Tasks.await(labellingTask);
            CompletableFuture.allOf(labellingFutures.toArray(new CompletableFuture[0]))
                    .join();
            Log.d(TAG, "Finished labelling photos");

            CompletableFuture.allOf(downloadingFutures.toArray(new CompletableFuture[0]))
                    .join();
            Log.d(TAG, "Finished downloading photos");
        } catch (ExecutionException | InterruptedException e) {
            Log.d(TAG, "Error when syncing photos", e);
            return Result.failure();
        }

        return Result.success();
    }

    private Task<Uri> uploadImage(Uri file, String name) {
        StorageReference imageRef = storageRef
                .child(name);

        UploadTask uploadTask = imageRef.putFile(file);
        uploadTask.addOnFailureListener(e -> {
            Log.e(TAG, "Error when upload your images", e);
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

    private void downloadImage(PhotoDetails photo, CompletableFuture<Void> future) {
        File localFile = getFileOnLocal(photo.localPath);
        if (localFile == null) {
            Log.e(TAG, "Cannot create local file for " + photo.localPath);
            return;
        }

        StorageReference imageRef =
                FirebaseStorage.getInstance().getReferenceFromUrl(photo.remoteUrl);

        imageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            Log.d(TAG, "Downloaded " + photo.localPath);
            MediaScannerConnection.scanFile(
                    getApplicationContext(),
                    new String[]{localFile.getPath()},
                    null,
                    (path, uri) -> {
                        Log.i(TAG, "Scanned " + path + ":");
                        Log.i(TAG, "-> uri=" + uri);
                    });
            future.complete(null);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error when downloading " + photo.localPath, e);
            future.completeExceptionally(e);
        });
    }

    private File getFileOnLocal(String localPath) {
        File file = new File(localPath);

        File folder = file.getParentFile();
        if (folder == null) {
            return null;
        }

        if (!folder.exists()) {
            if (!folder.mkdirs()) return null;
        }

        return file;
    }

    private File createUniqueFolder(String path) {
        File folder = new File(path);
        String name = folder.getName();
        String parent = folder.getParent();

        // Loop to find a unique folder name
        int counter = 1;
        while (folder.exists()) {
            String newName = name + " (" + counter + ")";
            folder = new File(parent, newName);
            ++counter;
        }

        // Try creating the folder
        if (folder.mkdirs()) {
            return folder;
        } else {
            return null;
        }
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
