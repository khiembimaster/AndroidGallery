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
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class PhotoService extends Service {
    public ArrayList<Photo> photos;

    private final String TAG = this.getClass().getSimpleName();
    private final IBinder binder = new LocalBinder();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final PhotoRepository photoRepository = new PhotoRepository(this);

    private PhotoLoadedCallback photoLoadedCallback;

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

    public void registerPhotoLoadedCallback(PhotoLoadedCallback callback) {
        photoLoadedCallback = callback;
    }

    public void getLocalPhotos() {
        executor.execute(this::getLocalPhotosFunc);
    }

    public void setFirebaseUser(FirebaseUser user) {
        photoRepository.setFirebaseUser(user);
    }

    public void test() {
        photoRepository.test();
    }

    private void getLocalPhotosFunc() {
        photos = photoRepository.GetAllPhotos();
        Log.d("PhotoService", "Getting local photos: " + photos.size());
        // Intent intent = new Intent(ACTION_GET_LOCAL_PHOTOS);
        // localBroadcastManager.sendBroadcast(intent);

        photoLoadedCallback.onCompleted(photos);
    }

    private ArrayList<Photo> getRemotePhotos() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void syncPhotos() {

    }

    public interface PhotoLoadedCallback {
        void onCompleted(ArrayList<Photo> photos);
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