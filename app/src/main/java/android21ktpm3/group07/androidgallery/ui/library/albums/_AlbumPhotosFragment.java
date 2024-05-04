package android21ktpm3.group07.androidgallery.ui.library.albums;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.ui.photos.PhotosFragment;

// TODO: Very very stupid way to do this, Should refactor later

public class _AlbumPhotosFragment extends PhotosFragment {
    private final String TAG = this.getClass().getSimpleName();
    private Long albumBucketId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (getActivity().getIntent().getBooleanExtra("isFavouriteAlbum", false)) {
            albumBucketId = null;
        } else {
            albumBucketId = getActivity().getIntent().getLongExtra("albumBucketID", 0);
        }

        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: " + albumBucketId);
        executor.execute(() -> {
            photoRepository.getPhotosInAlbum(albumBucketId);
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Menu menu = menuItemHandler.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            int itemId = item.getItemId();

            if (itemId == R.id.share || itemId == R.id.delete || itemId == R.id.move) {
                item.setVisible(true);
                item.setEnabled(true);
            } else {
                item.setVisible(false);
                item.setEnabled(false);
            }
        }
        menuItemHandler.hideToolbar();
    }

    @Override
    public void displayFragmentOptionItems() {
        menuItemHandler.showToolbar();
    }

    @Override
    public void hideFragmentOptionItems() {
        menuItemHandler.hideToolbar();
    }

    @Override
    protected void setViewModelListener() {
        photosViewModel.listenToMediaChangesInAlbum(albumBucketId);
        photosViewModel.listenToReloadInAlbumCall();
    }
}
