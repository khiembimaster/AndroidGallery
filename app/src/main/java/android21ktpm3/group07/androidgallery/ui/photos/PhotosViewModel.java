package android21ktpm3.group07.androidgallery.ui.photos;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import androidx.lifecycle.ViewModel;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import android21ktpm3.group07.androidgallery.helpers.ListHelper;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.models.PhotoGroup;
import android21ktpm3.group07.androidgallery.models.remote.PhotoDetails;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PhotosViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private final PhotoRepository photoRepository;
    private final ExecutorService executor;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final ObservableList<Photo> photosData = new ObservableArrayList<>();
    private final ObservableList<PhotoGroup> photoGroups = new ObservableArrayList<>();

    private Map<String, PhotoDetails> remotePhotosMap = new HashMap<>();

    // reponse callbacks
    private PhotoRepository.GetLocalPhotosCallback getAllLocalPhotosCallback;
    private PhotoRepository.GetLocalPhotosCallback getPhotosInAlbumCallback;
    private PhotoRepository.GetRemotePhotosCallback getRemotePhotosCallback;
    private PhotoRepository.MediaChangedCallback mediaChangedCallback;
    private PhotoRepository.MediaChangedCallback mediaChangedInAlbumCallback;


    @Inject
    public PhotosViewModel(PhotoRepository photoRepository, ExecutorService executor) {
        this.photoRepository = photoRepository;
        this.executor = executor;

        getRemotePhotosCallback = new PhotoRepository.GetRemotePhotosCallback() {
            @Override
            public void onCompleted(List<PhotoDetails> remotePhotos) {
                handler.post(() -> {
                    Log.d(TAG, "Remote photos loaded");

                    remotePhotosMap = new HashMap<>(
                            (int) Math.ceil(remotePhotos.size() / 0.75)
                    );

                    for (PhotoDetails remotePhoto : remotePhotos) {
                        remotePhotosMap.put(remotePhoto.localPath, remotePhoto);
                    }

                    for (Photo localPhoto : photosData) {
                        PhotoDetails remotePhoto = remotePhotosMap.get(localPhoto.getPath());

                        if (remotePhoto != null) {
                            localPhoto.setRemoteUrl(remotePhoto.remoteUrl);
                        } else {
                            localPhoto.setRemoteUrl(null);
                        }
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                handler.post(() -> {
                    Log.e(TAG, "Failed to load remote photos", e);
                    for (Photo localPhoto : photosData) {
                        localPhoto.setRemoteUrl(null);
                    }
                });
            }
        };

        // enable this by default
        photoRepository.addGetAllRemotePhotosCallback(getRemotePhotosCallback);

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

                    PhotoDetails remotePhoto = remotePhotosMap.get(photo.getPath());
                    if (remotePhoto != null) {
                        photo.setRemoteUrl(remotePhoto.remoteUrl);
                    } else {
                        photo.setRemoteUrl(null);
                    }

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
                // for (int i = positionStart; i < positionStart + itemCount; i++) {
                //     Photo photo = sender.get(i);
                //     for (PhotoGroup group : photoGroups) {
                //         if ((group.getDate()).isEqual(photo.getRepresentativeDate())) {
                //             group.getPhotos().remove(photo);
                //             if (group.getPhotos().isEmpty()) {
                //                 photoGroups.remove(group);
                //             }
                //             break;
                //         }
                //     }
                // }
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

    public void AddPhoto(Photo photo) {
        photosData.add(photo);
    }

    public void RemovePhotos(List<Photo> photoList) {
        photosData.removeAll(photoList);

        for (Photo photo : photoList) {
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

    public void RemovePhoto(Uri uri) {
        for (Photo photo : photosData) {
            if (!photo.getContentUri().equals(uri)) continue;

            photosData.remove(photo);
            for (PhotoGroup group : photoGroups) {
                if ((group.getDate()).isEqual(photo.getRepresentativeDate())) {
                    group.getPhotos().remove(photo);
                    if (group.getPhotos().isEmpty()) {
                        photoGroups.remove(group);
                    }
                    break;
                }
            }
            break;
        }

    }

    public void clear() {
        photosData.clear();
        photoGroups.clear();
    }

    public void test() {
        Photo photo = photoGroups.get(0).getPhotos().get(0);
    }

    public void test2() {
        Photo test = photoGroups.get(0).getPhotos().get(0);
        test.setRemoteUrl("test");
    }

    public void loadLocalPhotos() {
        executor.execute(photoRepository::getAllLocalPhotos);
    }

    public void listenToReloadCall() {
        if (getAllLocalPhotosCallback != null) throw new IllegalStateException("Already listening");

        getAllLocalPhotosCallback = photos -> handler.post(() -> {
            clear();
            photosData.addAll(photos);
        });
        photoRepository.addGetAllLocalPhotosCallback(getAllLocalPhotosCallback);
    }

    // TODO: Specify the albumBucketID in the callback
    public void listenToReloadInAlbumCall() {
        if (getPhotosInAlbumCallback != null) throw new IllegalStateException("Already listening");

        getAllLocalPhotosCallback = photos -> handler.post(() -> photosData.addAll(photos));
        photoRepository.addGetPhotosInAlbumCallback(getAllLocalPhotosCallback);
    }

    public void listenToMediaChanges() {
        if (mediaChangedCallback != null) throw new IllegalStateException("Already listening");

        mediaChangedCallback = new PhotoRepository.MediaChangedCallback() {
            @Override
            public void onAdded(Photo photo, long albumBucketID) {
                PhotoDetails remotePhoto = remotePhotosMap.get(photo.getPath());
                if (remotePhoto != null) {
                    photo.setRemoteUrl(remotePhoto.remoteUrl);
                }

                handler.post(() -> AddPhoto(photo));
            }

            @Override
            public void onDeleted(Uri uri) {
                handler.post(() -> RemovePhoto(uri));
            }
        };
        photoRepository.addMediaChangedCallback(mediaChangedCallback);
    }

    public void listenToMediaChangesInAlbum(long albumBucketID) {
        if (mediaChangedInAlbumCallback != null)
            throw new IllegalStateException("Already listening");

        mediaChangedInAlbumCallback = new PhotoRepository.MediaChangedCallback() {
            @Override
            public void onAdded(Photo photo, long bucketID) {
                if (bucketID != albumBucketID) return;

                PhotoDetails remotePhoto = remotePhotosMap.get(photo.getPath());
                if (remotePhoto != null) {
                    photo.setRemoteUrl(remotePhoto.remoteUrl);
                }

                handler.post(() -> AddPhoto(photo));
            }

            @Override
            public void onDeleted(Uri uri) {
                handler.post(() -> RemovePhoto(uri));
            }
        };
        photoRepository.addMediaChangedCallback(mediaChangedInAlbumCallback);
    }

    @Override
    protected void onCleared() {
        if (getAllLocalPhotosCallback != null) {
            photoRepository.removeGetAllLocalPhotosCallback(getAllLocalPhotosCallback);
        }
        if (getPhotosInAlbumCallback != null) {
            photoRepository.removeGetPhotosInAlbumCallback(getPhotosInAlbumCallback);
        }
        if (getRemotePhotosCallback != null) {
            photoRepository.removeGetAllRemotePhotosCallback(getRemotePhotosCallback);
        }
        if (mediaChangedCallback != null) {
            photoRepository.removeMediaChangedCallback(mediaChangedCallback);
        }
        if (mediaChangedInAlbumCallback != null) {
            photoRepository.removeMediaChangedCallback(mediaChangedInAlbumCallback);
        }

        super.onCleared();
    }
}