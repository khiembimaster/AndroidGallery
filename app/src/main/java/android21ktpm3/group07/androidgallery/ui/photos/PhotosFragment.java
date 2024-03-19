package android21ktpm3.group07.androidgallery.ui.photos;

import static androidx.core.content.FileProvider.getUriForFile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.File;
import java.util.ArrayList;

import android21ktpm3.group07.androidgallery.IMenuItemHandler;
import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.databinding.FragmentPhotosBinding;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class PhotosFragment extends Fragment implements PhotoAdapter.OnItemSelectedListener  {
    private FragmentPhotosBinding binding;
    private Menu menu;
    protected PhotosViewModel photosViewModel;

    protected IMenuItemHandler handler = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        photosViewModel = new ViewModelProvider(this).get(PhotosViewModel.class);
        photosViewModel.setPhotoRepository(new PhotoRepository(this.getActivity()));
        photosViewModel.setUpdateTask(UpdateRecyclerView);
        initVM();

        binding = FragmentPhotosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.recyclerView.setLayoutManager(layoutManager);

        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof IMenuItemHandler)) return;

        handler = (IMenuItemHandler) context;
        handler.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.share) {
                sharePhotos();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void initVM() {
        photosViewModel.loadPhotos();
    }

    public Runnable UpdateRecyclerView = new Runnable() {
        @Override
        public void run() {
            PhotosRecyclerAdapter adapter = new PhotosRecyclerAdapter(
                    getActivity(),
                    photosViewModel.getPhotosGroupByDate()
            );

            adapter.setChildItemSelectedAdapter(photo -> {
                displayShareOptionItem(photo);
                photosViewModel.addToSelectedPhotos(photo);
            });
            adapter.setChildItemUnselectedAdapter(photo -> {
                photosViewModel.removeFromSelectedPhotos(photo);
                if (photosViewModel.getSelectedPhotos().isEmpty()) {
                    hideShareOptionItem();
                }
            });

            binding.recyclerView.setAdapter(adapter);
        }
    };

    public void displayShareOptionItem(Photo photo) {
//        handler.getMenu().findItem(R.id.share)
//                .setVisible(true)
//                .setEnabled(true);
        System.out.println(photo.getFileSize());
        Intent intent = new Intent(getContext(), ImageActivity.class);
        intent.putExtra("photo_path", photo.getPath());
        intent.putExtra("photo_tags", photo.getTags());
        intent.putExtra("photo_date", photo.getModifiedDate());
        intent.putExtra("photo_size", photo.getFileSize());

        startActivity(intent);
    }

    public void hideShareOptionItem() {
        handler.getMenu().findItem(R.id.share)
                .setVisible(false)
                .setEnabled(false);
    }

    public void sharePhotos() {
        ArrayList<Uri> imageUris = new ArrayList<>();

        for (Photo photo : photosViewModel.getSelectedPhotos()) {
            File file = new File(photo.getPath());

            Uri imageUri = getUriForFile(
                    getContext(),
                    "android21ktpm3.group07.androidgallery.fileprovider",
                    file);
            imageUris.add(imageUri);
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUris);
        shareIntent.setType("image/*");

        startActivity(Intent.createChooser(shareIntent, "Share images to..."));
    }

    @Override
    public void onItemSelected(Photo photo) {

        Intent intent = new Intent(getContext(), ImageActivity.class);

        intent.putExtra("photo_path", photo.getPath());

        startActivity(intent);
    }
}