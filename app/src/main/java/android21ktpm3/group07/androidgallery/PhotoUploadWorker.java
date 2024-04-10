package android21ktpm3.group07.androidgallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class PhotoUploadWorker extends Worker {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final String TAG = "PhotoUploadWorker";
    private final ImageLabeler labeler;
    private int count = 0;
    public PhotoUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        // Or, to set the minimum confidence required:
        ImageLabelerOptions options =
                new ImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.5f)
                        .build();
        labeler = ImageLabeling.getClient(options);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        count=0;
        uploadImages(user);
        return Result.success();
    }

    //TODO: Create a service to upload images to Firebase Storage
    private void uploadImages(FirebaseUser user){
        if (user == null) return;

        // Load images from local storage
        PhotoRepository photoRepository = new PhotoRepository(getApplicationContext());
        List<Photo> imageUrls = photoRepository.GetAllPhotos(); //TODO: Load images from local storage

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        // Create file metadata including the content type

        for(Photo photo : imageUrls){
            // Load image from local storage
            Uri file = Uri.fromFile(new File(photo.getPath()));
            // Upload file to Firebase Storage
            StorageReference imageRef = storageRef.child("user/"+user.getUid()+"/"+file.getLastPathSegment());
            UploadTask uploadTask = imageRef.putFile(file);
            uploadTask.addOnFailureListener(e -> {
                Log.e(TAG, "Error when upload your images", e);
                int errorCode = ((StorageException) e).getErrorCode();
                String errorMessage = e.getMessage();
            }).addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Your images stored successfully!");
//                binding.progressBar.setVisibility(View.GONE);
            }).addOnProgressListener(taskSnapshot -> {
//                binding.progressBar.setVisibility(View.VISIBLE);
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                binding.progressBar.setProgress((int) progress);
                Log.d(TAG, "Upload is " + progress + "% done");
            }).addOnPausedListener(taskSnapshot -> {
                Log.d(TAG, "Upload is paused");
//                binding.progressBar.setVisibility(View.GONE);
            });


            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    return null;
                }
                return imageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Label images using ML Kit
                    // Load image from local storage to Bitmap
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(file)
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    InputImage image = InputImage.fromBitmap(resource, 0);
                                    labeler.process(image).addOnSuccessListener(imageLabels -> {
                                        List <String> labels = new ArrayList<>();
                                        for (ImageLabel label : imageLabels) {
                                            labels.add(label.getText());
                                        }
                                        // Save image data to Firestore
                                        SaveImageDataToFirestore(task.getResult(), labels, user);

                                    }).addOnFailureListener(e -> Log.e(TAG, "Error when label your images", e));
                                }
                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                }
                            });
                }
            });
        }
    }

    private void SaveImageDataToFirestore(Uri downloadUri, List<String> labels, FirebaseUser user){
        // Save image data to Firestore
        Map<String, Object> data = new HashMap<>();

        data.put("labels", labels);
        data.put("url", downloadUri.toString());

        String userId = Objects.requireNonNull(user).getUid();

        CollectionReference images = db.collection("users")
                .document(userId)
                .collection("images");

        images.add(data).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error adding document", e);
        });
    }
}
