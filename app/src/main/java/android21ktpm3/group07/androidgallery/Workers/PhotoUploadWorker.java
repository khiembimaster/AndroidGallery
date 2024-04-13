package android21ktpm3.group07.androidgallery.Workers;

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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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
    private final FirebaseFirestore db;
    private final FirebaseUser user;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final String TAG = "PhotoUploadWorker";
    private static final String TOTAL_COUNT_KEY = "total_count";
    private final ImageLabeler labeler;
    private long count = 0;
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


    }

    @NonNull
    @Override
    public Result doWork() {
        Query query = db.collection("users").document(user.getUid())
                .collection("images")
                .whereEqualTo("status", "pending");

        AggregateQuery aggregateQuery = query.count();
        Task<QuerySnapshot> queryTask = query.get();

        aggregateQuery.get(AggregateSource.SERVER).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Count fetched successfully
                AggregateQuerySnapshot snapshot = task.getResult();
                count = snapshot.getCount();
                setProgressAsync(new Data.Builder()
                        .putLong(TOTAL_COUNT_KEY, count)
                        .build());
                Log.d(TAG, "Count: " + snapshot.getCount());
            } else {
                Log.d(TAG, "Count failed: ", task.getException());
            }
        });

        Task<List<Task<?>>> allTasks = queryTask.continueWithTask(task -> {
            if(task.isSuccessful()){
                List<Task<Void>> tasks = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Thread.sleep(1000);
                    Log.d(TAG, document.getId() + " => " + document.getData());
                    Map<String, Object> data = document.getData();
                    String path = (String)data.get("path");
                    assert path != null;
                    Uri file = Uri.fromFile(new File(path));
                    labelImage(document, file);
                    Task<Uri> uriTask = uploadImage(file);
                    uriTask.addOnSuccessListener(uri ->{
                        document.getReference().update("url", uri.toString());
                    });

                     tasks.add(Tasks.whenAll(uriTask).addOnSuccessListener(aVoid -> {
                        document.getReference().update("status", "completed");
                        PhotoUploadWorker.this.setProgressAsync(new Data.Builder()
                                .putLong(TOTAL_COUNT_KEY, --count)
                                .build());
                    }));
                }
                return Tasks.whenAllComplete(tasks);
            }else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
            return null;
        });

        allTasks.addOnSuccessListener(aVoid -> {
            Log.d(TAG, "All tasks completed successfully");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error when upload your images", e);
        });

        try {
            Tasks.await(allTasks);  // This will block until all upload tasks have completed
            Log.d(TAG, "All tasks completed successfully");
            return Result.success();
        } catch (ExecutionException e) {
            return Result.failure();
        } catch (InterruptedException e) {
            return Result.retry();
        }
    }

    //TODO: Create a service to upload images to Firebase Storage
    private Task<Uri> uploadImage(Uri file){
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        // Create file metadata including the content type

        // Upload file to Firebase Storage
        StorageReference imageRef = storageRef.child("user/"+user.getUid()+"/"+file.getLastPathSegment());
        UploadTask uploadTask = imageRef.putFile(file);
        uploadTask.addOnFailureListener(e -> {
            Log.e(TAG, "Error when upload your images", e);
            int errorCode = ((StorageException) e).getErrorCode();
            String errorMessage = e.getMessage();
        }).addOnSuccessListener(taskSnapshot -> {
            Log.d(TAG, "Your images stored successfully!");
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            Log.d(TAG, "Upload is " + progress + "% done");
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

    private void labelImage(QueryDocumentSnapshot document, Uri file) throws ExecutionException, InterruptedException {
        Glide.with(getApplicationContext())
            .asBitmap()
            .load(file)
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    // do something with the resource
                    InputImage image = InputImage.fromBitmap(resource, 0);
                    labeler.process(image)
                        .addOnSuccessListener(labels -> {
                            List<String> tags = new ArrayList<>();
                            for (ImageLabel label : labels) {
                                String text = label.getText();
                                float confidence = label.getConfidence();
                                tags.add(text);
                            }
                            document.getReference().update("tags", tags);
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Error when label your images", e);
                        });
                }
                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    // remove the resource
                }
            });
    }
}
