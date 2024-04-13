package android21ktpm3.group07.androidgallery.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.models.remote.PhotoDetails;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class PhotoService extends Service {
    private final String TAG = this.getClass().getSimpleName();
    private final IBinder binder = new LocalBinder();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final PhotoRepository photoRepository = new PhotoRepository(this);

    private LocalPhotosLoadedCallback localPhotosLoadedCallback;
    private RemotePhotosLoadedCallback remotePhotosLoadedCallback;
    private LocalPhotosInAlbumLoadedCallback localPhotosInAlbumLoadedCallback;
    private PhotosDeletedCallback photosDeletedCallback;

    public class LocalBinder extends Binder {
        public PhotoService getService() {
            return PhotoService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyReceiver receiver = new MyReceiver();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter("test"));

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void registerPhotoLoadedCallback(LocalPhotosLoadedCallback callback) {
        localPhotosLoadedCallback = callback;
    }

    public void registerPhotoInAlbumLoadedCallback(LocalPhotosInAlbumLoadedCallback callback) {
        localPhotosInAlbumLoadedCallback = callback;
    }

    public void registerRemotePhotosLoadedCallback(RemotePhotosLoadedCallback callback) {
        remotePhotosLoadedCallback = callback;
    }

    public void registerPhotosDeletedCallback(PhotosDeletedCallback callback) {
        photosDeletedCallback = callback;
    }


    public void getLocalPhotos() {
        executor.execute(() -> {
            ArrayList<Photo> photos = photoRepository.GetAllPhotos();
            Log.d("PhotoService", "Getting local photos: " + photos.size());

            if (localPhotosLoadedCallback != null)
                localPhotosLoadedCallback.onCompleted(photos);
        });
    }

    public void getLocalPhotosInAlbum(long albumId) {
        executor.execute(() -> {
            ArrayList<Photo> photos = photoRepository.getPhotosInAlbum(albumId);
            Log.d("PhotoService", "Getting local photos in album: " + photos.size());

            if (localPhotosInAlbumLoadedCallback != null)
                localPhotosInAlbumLoadedCallback.onCompleted(photos);
        });
    }

    public void updateSyncingStatus(List<Photo> localPhotos) {
        executor.execute(() -> {
            ArrayList<PhotoDetails> remotePhotos = photoRepository.getAllRemotePhotos();
            Log.d("PhotoService", "Getting remote photos: " + remotePhotos.size());

            HashMap<String, PhotoDetails> remotePhotosMap = new HashMap<>(
                    (int) Math.ceil(remotePhotos.size() / 0.75)
            );
            for (PhotoDetails remotePhoto : remotePhotos) {
                remotePhotosMap.put(remotePhoto.localPath, remotePhoto);
            }

            for (Photo localPhoto : localPhotos) {
                PhotoDetails remotePhoto = remotePhotosMap.get(localPhoto.getPath());

                if (remotePhoto != null)
                    localPhoto.setRemoteUrl(remotePhoto.remoteUrl);
            }

            if (remotePhotosLoadedCallback != null)
                remotePhotosLoadedCallback.onCompleted();
        });
    }

    public void deletePhotos(List<Photo> photos) {
        executor.execute(() -> {
            List<Photo> deletedPhotos = photoRepository.deletePhotos(photos);

            if (photosDeletedCallback != null) {
                if (deletedPhotos.size() == photos.size())
                    photosDeletedCallback.onCompleted(photos);
                else
                    photosDeletedCallback.onFailed(photos);
            }
        });
    }


    public void setFirebaseUser(FirebaseUser user) {
        photoRepository.setFirebaseUser(user);
    }

    public void test() {
        executor.execute(photoRepository::test);
    }

    private void syncPhotos() {

    }

    public interface LocalPhotosLoadedCallback {
        void onCompleted(ArrayList<Photo> photos);
    }

    public interface RemotePhotosLoadedCallback {
        void onCompleted();
    }

    public interface LocalPhotosInAlbumLoadedCallback {
        void onCompleted(ArrayList<Photo> photos);
    }

    public interface PhotosDeletedCallback {
        void onCompleted(List<Photo> deletedPhotos);

        void onFailed(List<Photo> deletedPhotos);
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Objects.equals(action, "test")) {
                Log.d(TAG, "Received test broadcast");
            }
        }
    }
}