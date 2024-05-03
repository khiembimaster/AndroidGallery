package android21ktpm3.group07.androidgallery.ui.library.albums;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import android21ktpm3.group07.androidgallery.models.Album;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AlbumsViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private final PhotoRepository photoRepository;
    private final ExecutorService executor;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final ObservableList<Album> albums = new ObservableArrayList<>();

    private boolean isLoaded = false;

    @Inject
    public AlbumsViewModel(PhotoRepository photoRepository, ExecutorService executor) {
        Log.d(TAG, "AlbumsViewModel: constructor");
        this.photoRepository = photoRepository;
        this.executor = executor;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public ObservableList<Album> getAlbums() {
        return albums;
    }

    public void loadAlbums() {
        isLoaded = true;
        executor.execute(() -> {
            List<Album> result = photoRepository.getAlbums();

            for (Album album : result) {
                Log.d(TAG, "loadAlbums: " + album.getName() + " " + album.getBucketID());
            }

            handler.post(() -> {
                albums.clear();
                albums.addAll(result);
            });
        });
    }

    @Override
    protected void onCleared() {
        Log.d(TAG, "onCleared");
        super.onCleared();
    }
}