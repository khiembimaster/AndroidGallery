package android21ktpm3.group07.androidgallery.ui.photos;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.lifecycle.ViewModel;

import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android21ktpm3.group07.androidgallery.helpers.ListHelper;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.models.PhotoGroup;

public class PhotosViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();

    private final ObservableList<Photo> photosData = new ObservableArrayList<>();

    // TODO: make a list as data source and add cb to update this accordingly
    private final ObservableList<PhotoGroup> photoGroups = new ObservableArrayList<PhotoGroup>();


    // TODO: Switch to DI, create factory
    public PhotosViewModel() {
        photosData.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<Photo>>() {
            @Override
            public void onChanged(ObservableList<Photo> sender) {
            }

            @Override
            public void onItemRangeChanged(ObservableList<Photo> sender, int positionStart,
                                           int itemCount) {
            }

            @Override
            public void onItemRangeInserted(ObservableList<Photo> sender, int positionStart,
                                            int itemCount) {
                for (int i = positionStart; i < positionStart + itemCount; i++) {
                    Photo photo = sender.get(i);

                    boolean isAdded = false;
                    for (PhotoGroup group : photoGroups) {
                        if ((group.getDate()).isEqual(photo.getRepresentativeDate())) {
                            ListHelper.addAndMaintainSorted(
                                    group.getPhotos(),
                                    photo,
                                    Comparator.comparing(
                                            Photo::getRepresentativeEpoch,
                                            Comparator.reverseOrder())
                            );

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

                        // photoGroups.add(newGroup);
                        ListHelper.addAndMaintainSorted(
                                photoGroups,
                                newGroup,
                                Comparator.comparing(PhotoGroup::getDate, Comparator.reverseOrder())
                        );
                    }
                }

            }

            @Override
            public void onItemRangeMoved(ObservableList<Photo> sender, int fromPosition,
                                         int toPosition, int itemCount) {
            }

            @Override
            public void onItemRangeRemoved(ObservableList<Photo> sender, int positionStart,
                                           int itemCount) {
                for (int i = positionStart; i < positionStart + itemCount; i++) {
                    Photo photo = sender.get(i);
                    for (PhotoGroup group : photoGroups) {
                        if ((group.getDate()).isEqual(photo.getRepresentativeDate())) {
                            group.getPhotos().remove(photo);
                            if (group.getPhotos().isEmpty()) {
                                photoGroups.remove(group);
                            }
                            break;
                        }
                    }
                }
            }
        });
    }

    public ObservableList<PhotoGroup> getPhotoGroups() {
        return photoGroups;
    }

    public final ObservableList<Photo> getPhotosData() {
        return photosData;
    }

    // TODO: move the init part to service/executor
    public void AddPhotos(List<Photo> photoList) {
        photosData.addAll(photoList);
    }

    public void RemovePhotos(List<Photo> photoList) {
        photosData.removeAll(photoList);
    }

    public void test() {
        Photo test = photoGroups.get(0).getPhotos().get(0);

        long time = Instant.now().plus(Period.ofDays(20)).toEpochMilli();
        List<Photo> list = new ArrayList<>();
        list.add(new Photo(
                0,
                test.getPath(),
                "test",
                time,
                time,
                "",
                0,
                "0"
        ));
        AddPhotos(list);
    }

    public void test2() {
        Photo test = photoGroups.get(0).getPhotos().get(0);
        test.setRemoteUrl("test");
    }

}