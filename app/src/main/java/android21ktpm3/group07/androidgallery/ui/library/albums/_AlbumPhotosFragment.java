package android21ktpm3.group07.androidgallery.ui.library.albums;

import android.widget.Toast;

import android21ktpm3.group07.androidgallery.ui.photos.PhotosFragment;

// TODO: Very very stupid way to do this, Should refactor later
public class _AlbumPhotosFragment extends PhotosFragment {
    @Override
    public void initVM() {
        long albumBucketID = -1;
        try {
            albumBucketID = getActivity().getIntent().getLongExtra("albumBucketID", -1);
            if (albumBucketID == -1) {
                throw new Exception("albumBucketID not found");
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        super.photosViewModel.loadPhotos(albumBucketID);
    }
}
