package android21ktpm3.group07.androidgallery.ui.photos;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import android21ktpm3.group07.androidgallery.models.Photo;

public class PhotosViewModel extends ViewModel {
    private final List<Photo> photos = new ArrayList<>();
    private final List<Photo> selectedPhotos = new ArrayList<>();

    private MutableLiveData<List<Photo>> photosLiveData = new MutableLiveData<>();

    // TODO: Switch to DI, create factory
    public PhotosViewModel() {
    }

    public List<Photo> getSelectedPhotos() {
        return selectedPhotos;
    }

    public void addToSelectedPhotos(Photo photo) {
        selectedPhotos.add(photo);
    }

    public void removeFromSelectedPhotos(Photo photo) {
        selectedPhotos.remove(photo);
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void AddPhotos(List<Photo> photoList) {
        photos.addAll(photoList);
    }
}