package android21ktpm3.group07.androidgallery.Workers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bumptech.glide.util.Preconditions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class PrepareBackupWorker extends Worker {
    private final FirebaseFirestore db;
    private final FirebaseUser user;
    public PrepareBackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        db = FirebaseFirestore.getInstance();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    @NonNull
    @Override
    public Result doWork() {
        // Load images from local storage
        PhotoRepository photoRepository = new PhotoRepository(getApplicationContext());
        List<Photo> photoList = photoRepository.GetAllPhotos(); //TODO: Load images from local storage
        String userId = Objects.requireNonNull(user).getUid();
        CollectionReference collectionReference = db.collection("users").document(userId).collection("images");

        List<Task<DocumentReference>> tasks = new ArrayList<>();
        for (Photo photo: photoList) {
             AggregateQuery checkExistQuery = collectionReference.whereEqualTo("name", photo.getName()).count();
             Task<DocumentReference> addTask = checkExistQuery.get(AggregateSource.SERVER).continueWithTask(task -> {
                if (task.getResult().getCount() == 0) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("path", photo.getPath());
                    data.put("name", photo.getName());
                    data.put("modifiedDate", photo.getModifiedDate());
                    data.put("fileSize", photo.getFileSize());
                    data.put("tags", photo.getTags());
                    data.put("status", "pending");
                    return collectionReference.add(data);
                }
                return null;
            });
            addTask.addOnSuccessListener(documentReference -> {
                Log.d("PrepareBackupWorker", "Added document with ID: " + documentReference.getId());
            }).addOnFailureListener(e -> {
                Log.e("PrepareBackupWorker", "Document existed", e);
            });

            tasks.add(addTask);

        }

        try {
            Tasks.await(Tasks.whenAllComplete(tasks));
            return Result.success();
        } catch (ExecutionException e) {
            return Result.failure();
        } catch (InterruptedException e) {
            return Result.retry();
        }
    }
}
