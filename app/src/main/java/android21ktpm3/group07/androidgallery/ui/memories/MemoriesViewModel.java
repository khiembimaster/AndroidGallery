package android21ktpm3.group07.androidgallery.ui.memories;

import android.os.Handler;
import android.os.Looper;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;
import android21ktpm3.group07.androidgallery.ui.photos.PhotosViewModel;

public class MemoriesViewModel extends ViewModel {

    private List<Photo> photos;

    private PhotoRepository photoRepository;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    Handler handler = new Handler(Looper.getMainLooper());

    private Runnable updateTask;





    public MemoriesViewModel() {


    }
    public void setPhotoRepository(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public void loadPhotos() {
        executor.execute(() -> {
            photos = photoRepository.GetAllPhotos();

            handler.post(updateTask);


        });
    }
    public void loadPhotos(long albumBucketID) {
        executor.execute(() -> {
            photos = photoRepository.getPhotosInAlbum(albumBucketID);

            handler.post(updateTask);
        });
    }


    public List<Photo> getPhotosGroupByDatePrevious() {
       // LocalDate currentDate = LocalDate.now();

        LocalDate specificDate = LocalDate.of(2024, 3, 19);

        return photos.stream()
                .filter(photo -> {

                    LocalDate photoDate = toLocalDate(photo.getModifiedDate());
                    return photoDate.isEqual(specificDate);
                })
                .collect(Collectors.toList());
    }


    private LocalDate toLocalDate(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void setUpdateTask(Runnable updateTask) {
        this.updateTask = updateTask;
    }








}