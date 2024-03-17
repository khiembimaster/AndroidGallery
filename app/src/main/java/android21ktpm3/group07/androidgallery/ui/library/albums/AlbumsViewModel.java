package android21ktpm3.group07.androidgallery.ui.library.albums;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android21ktpm3.group07.androidgallery.models.Album;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class AlbumsViewModel extends ViewModel {
    private List<Album> albums;
    private PhotoRepository photoRepository;
    private Runnable updateTask;

    // Use shared executor service instead?
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    // TODO: Switch to DI, create factory
    public AlbumsViewModel() {}

    public void setPhotoRepository(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public void setUpdateTask(Runnable updateTask) {
        this.updateTask = updateTask;
    }

    public void loadAlbums() {
        executor.execute(() -> {
            albums = photoRepository.GetAlbums();

            handler.post(updateTask);
        });
    }
}