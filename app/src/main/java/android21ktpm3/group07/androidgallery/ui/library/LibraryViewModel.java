package android21ktpm3.group07.androidgallery.ui.library;

import androidx.lifecycle.ViewModel;

import android21ktpm3.group07.androidgallery.ui.library.albums.AlbumsViewModel;

public class LibraryViewModel extends ViewModel {
    private AlbumsViewModel albumsViewModel;

    // TODO: Switch to DI, create factory
    public LibraryViewModel() {
    }

}