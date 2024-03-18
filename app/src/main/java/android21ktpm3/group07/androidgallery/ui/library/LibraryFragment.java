package android21ktpm3.group07.androidgallery.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android21ktpm3.group07.androidgallery.databinding.FragmentLibraryBinding;
import android21ktpm3.group07.androidgallery.ui.library.albums.AlbumsViewModel;

public class LibraryFragment extends Fragment {
    private FragmentLibraryBinding binding;
    private LibraryViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(LibraryViewModel.class);
        viewModel.setAlbumsViewModel(new ViewModelProvider(this).get(AlbumsViewModel.class));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}