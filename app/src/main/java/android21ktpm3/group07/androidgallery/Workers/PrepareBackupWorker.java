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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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

        DocumentReference userRef = db.collection("users").document(userId);
        CollectionReference collectionReference = userRef.collection("images");


        Task<List<Task<DocumentReference>>> taskListTask = userRef.get().continueWithTask(task -> {
            DocumentSnapshot documentSnapshot = task.getResult();
            LocalDate lastBackupDate = null;
            if (documentSnapshot.contains("lastBackupDate")) {
                Date date = documentSnapshot.getDate("lastBackupDate");
                Instant instant = date.toInstant();
                lastBackupDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                Log.d("PrepareBackupWorker", "Last backup date: " + lastBackupDate);
            }

            List<Task<DocumentReference>> tasks = new ArrayList<>();
            for (Photo photo: photoList.subList(0, Math.min(photoList.size(), 200))) {
                if (lastBackupDate != null && photo.getRepresentativeDate().isBefore(lastBackupDate)) {
                    continue;
                }
                Map<String, Object> data = new HashMap<>();
                data.put("path", photo.getPath());
                data.put("name", photo.getName());
                data.put("modifiedDate", photo.getModifiedDate());
                data.put("fileSize", photo.getFileSize());
                data.put("tags", photo.getTags());
                data.put("status", "pending");

                Task<DocumentReference> addTask = collectionReference.add(data);
                addTask.addOnSuccessListener(documentReference -> {
                    Log.d("PrepareBackupWorker", "Added photo: " + documentReference.getId());
                });
                tasks.add(addTask);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("lastBackupDate", Date.from(Instant.now()));
            userRef.set(data);
            return Tasks.forResult(tasks);
        });

        try {
            List<Task<DocumentReference>> tasks = Tasks.await(taskListTask);
            Tasks.await(Tasks.whenAllComplete(tasks));

            return Result.success();
        } catch (ExecutionException e) {
            return Result.failure();
        } catch (InterruptedException e) {
            return Result.retry();
        }
    }
}
