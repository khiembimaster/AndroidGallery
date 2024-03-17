package android21ktpm3.group07.androidgallery.ui.photos;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class PhotosViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    private List<Photo> photos;
    private PhotoRepository photoRepository;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    private Runnable updateRunnable;

    // TODO: Switch to DI
    public PhotosViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is photos fragment");
    }

    public void setPhotoRepository(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public void setUpdateRunnable(Runnable updateRunnable) {
        this.updateRunnable = updateRunnable;
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void loadPhotos() {
        executor.execute(() -> {
            photos = photoRepository.GetAllPhotos();

            handler.post(updateRunnable);
        });
    }


    /**
     * Groups the photos by their modified date and returns a list of pairs, where the
     * first element of each pair is the date and the second element is a list of photos
     * taken on that date. Both lists are sorted in descending order.
     *
     * @return A list of pairs of dates and lists of photos.
     */
    public List<Pair<LocalDate, List<Photo>>> getPhotosGroupByDate() {
        return photos.stream()
                .sorted((photo1, photo2) -> Long.compare(photo2.getModifiedDate(), photo1.getModifiedDate()))
                .collect(Collectors.groupingBy(photo -> toLocalDate(photo.getModifiedDate())))
                .entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getKey().compareTo(entry1.getKey()))
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private LocalDate toLocalDate(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}