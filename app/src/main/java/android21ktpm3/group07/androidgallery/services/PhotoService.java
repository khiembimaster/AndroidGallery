package android21ktpm3.group07.androidgallery.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class PhotoService extends Service {
    public final static String ACTION_GET_LOCAL_PHOTOS = "android21ktpm3.group07.androidgallery" +
            ".services.PhotoService.GET_LOCAL_PHOTOS";

    private final IBinder binder = new LocalBinder();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final PhotoRepository photoRepository = new PhotoRepository(this);

    public ArrayList<Photo> photos;

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
        executor.execute(this::getLocalPhotos);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void getLocalPhotos() {
        photos = photoRepository.GetAllPhotos();
        Log.d("PhotoService", "Getting local photos: " + photos.size());
        Intent intent = new Intent(ACTION_GET_LOCAL_PHOTOS);
        sendBroadcast(intent);
    }

    private ArrayList<Photo> getRemotePhotos() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void syncPhotos() {

    }
}