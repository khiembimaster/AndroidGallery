package android21ktpm3.group07.androidgallery.ui.photos;

import android.util.Log;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.models.PhotoGroup;

public class PhotosViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private final ObservableList<PhotoGroup> photoGroups = new ObservableArrayList<>();

    // TODO: Switch to DI, create factory
    public PhotosViewModel() {
    }

    public ObservableList<PhotoGroup> getPhotoGroups() {
        return photoGroups;
    }

    public void AddPhotos(List<Photo> photoList) {
        List<PhotoGroup> groupsToUpdate = new ArrayList<>();

        for (Photo photo : photoList) {
            boolean isAdded = false;
            for (PhotoGroup group : photoGroups) {
                if ((group.getDate()).isEqual(photo.getRepresentativeDate())) {
                    group.getPhotos().add(photo);
                    groupsToUpdate.add(group);
                    isAdded = true;
                    break;
                }
            }

            if (!isAdded) {
                PhotoGroup newGroup = new PhotoGroup(
                        photo.getRepresentativeDate(),
                        new ObservableArrayList<>()
                );
                newGroup.getPhotos().add(photo);
                photoGroups.add(newGroup);
            }
        }

        for (PhotoGroup group : groupsToUpdate) {
            group.getPhotos().sort(Comparator.comparing(Photo::getRepresentativeDate));
        }

        photoGroups.sort(Comparator.comparing(PhotoGroup::getDate, Comparator.reverseOrder()));
    }

    public void test() {
        Log.d(TAG, "test: " + photoGroups.get(0).getPhotos().size());
        
        photoGroups.get(0).getPhotos().get(0).setRemoteUrl("test");
        List<Photo> list = new ArrayList<>();
        list.add(photoGroups.get(0).getPhotos().get(0));
        AddPhotos(list);

        Log.d(TAG, "test: " + photoGroups.get(0).getPhotos().size());
    }

}