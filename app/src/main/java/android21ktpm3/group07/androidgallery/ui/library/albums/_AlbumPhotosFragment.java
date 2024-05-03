package android21ktpm3.group07.androidgallery.ui.library.albums;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import android21ktpm3.group07.androidgallery.ui.photos.PhotosFragment;

// TODO: Very very stupid way to do this, Should refactor later

public class _AlbumPhotosFragment extends PhotosFragment {
    private final String TAG = this.getClass().getSimpleName();
    private long albumBucketId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        albumBucketId = getActivity().getIntent().getLongExtra("albumBucketID", -1);
        
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: " + albumBucketId);
        photoRepository.getPhotosInAlbum(albumBucketId);
    }

    @Override
    public void displayFragmentOptionItems() {
    }

    @Override
    public void hideFragmentOptionItems() {
    }

    @Override
    protected void setViewModelListener() {
        photosViewModel.listenToMediaChangesInAlbum(albumBucketId);
        photosViewModel.listenToReloadInAlbumCall();
    }
}
