package android21ktpm3.group07.androidgallery.ui.library.albums;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import android21ktpm3.group07.androidgallery.services.PhotoService;
import android21ktpm3.group07.androidgallery.ui.photos.PhotosFragment;

// TODO: Very very stupid way to do this, Should refactor later
public class _AlbumPhotosFragment extends PhotosFragment {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void displayFragmentOptionItems() {
    }

    @Override
    public void hideFragmentOptionItems() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context.unbindService(connection);

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                PhotoService.LocalBinder binder = (PhotoService.LocalBinder) service;
                photoService = binder.getService();

                photoService.registerPhotoInAlbumLoadedCallback(photos ->
                        threadHandler.post(() -> loadPhotos(photos)));

                photoService.getLocalPhotosInAlbum(
                        getActivity().getIntent().getLongExtra("albumBucketID", -1)
                );

                isBound = true;
                Log.d(TAG, "Service connected");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isBound = false;
                Log.d(TAG, "Service disconnected");
            }
        };
        context.bindService(new Intent(context, PhotoService.class), connection,
                Context.BIND_AUTO_CREATE);
    }

}
