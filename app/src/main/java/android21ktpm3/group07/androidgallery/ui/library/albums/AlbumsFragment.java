package android21ktpm3.group07.androidgallery.ui.library.albums;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import android21ktpm3.group07.androidgallery.databinding.FragmentAlbumsBinding;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlbumsFragment extends Fragment {
    private FragmentAlbumsBinding binding;
    private AlbumsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /// get viewmodel from provider of activity scope
        // viewModel = new ViewModelProvider(requireActivity()).get(AlbumsViewModel.class);
        //
        // if (!viewModel.isLoaded()) {
        //     viewModel.loadAlbums();
        // }
        viewModel = new ViewModelProvider(AlbumsFragment.this).get(AlbumsViewModel.class);
        Log.d("AlbumsFragment", "onCreate: " + viewModel);
        viewModel.loadAlbums();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("AlbumsFragment", "onCreateView");

        binding = FragmentAlbumsBinding.inflate(inflater, container, false);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(new AlbumsAdapter(getContext(), viewModel.getAlbums()));

        return binding.getRoot();
    }


    @Override
    public void onDestroyView() {
        binding.recyclerView.setAdapter(null);
        binding = null;
        getViewModelStore().clear();

        super.onDestroyView();
    }
}