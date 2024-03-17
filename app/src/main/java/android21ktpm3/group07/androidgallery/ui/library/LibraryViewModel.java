package android21ktpm3.group07.androidgallery.ui.library;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android21ktpm3.group07.androidgallery.models.Album;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;
import android21ktpm3.group07.androidgallery.ui.library.albums.AlbumsViewModel;

public class LibraryViewModel extends ViewModel {
    private AlbumsViewModel albumsViewModel;

    // TODO: Switch to DI, create factory
    public LibraryViewModel() {}

    public void setAlbumsViewModel(AlbumsViewModel albumsViewModel) {
        this.albumsViewModel = albumsViewModel;
    }
}