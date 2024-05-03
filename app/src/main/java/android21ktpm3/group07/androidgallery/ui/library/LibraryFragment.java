package android21ktpm3.group07.androidgallery.ui.library;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import android21ktpm3.group07.androidgallery.IMenuItemHandler;
import android21ktpm3.group07.androidgallery.databinding.FragmentLibraryBinding;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LibraryFragment extends Fragment {
    @Inject
    PhotoRepository photoRepository;

    private FragmentLibraryBinding binding;
    private LibraryViewModel viewModel;
    private ActivityResultLauncher<PickVisualMediaRequest> photoPicker;

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof IMenuItemHandler)) return;


        IMenuItemHandler menuItemHandler = (IMenuItemHandler) context;
        menuItemHandler.setOnCreateNewItemClickListener(() -> {
            createAlbum();
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        photoPicker = registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(),
                uris -> {
                    if (!uris.isEmpty()) {
                        Log.d("PhotoPicker", "Selected " + uris.size() + " photos");
                        photoRepository.copyPhotosToFolder(
                                uris, "/storage/emulated/0/Pictures/test");
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                }
        );
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(LibraryViewModel.class);

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onDestroy() {
        Log.d("LibraryFragment", "onDestroy");
        super.onDestroy();
    }

    private void createAlbum() {
        photoPicker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }
}