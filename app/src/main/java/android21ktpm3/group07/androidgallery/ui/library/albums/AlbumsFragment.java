package android21ktpm3.group07.androidgallery.ui.library.albums;

import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android21ktpm3.group07.androidgallery.databinding.FragmentAlbumsBinding;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class AlbumsFragment extends Fragment {
    private FragmentAlbumsBinding binding;
    private AlbumsViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAlbumsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(AlbumsViewModel.class);
        // DI this?
        viewModel.setPhotoRepository(new PhotoRepository(this.getActivity()));
        viewModel.setUpdateTask(UpdateRecyclerView);
        viewModel.loadAlbums();

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        binding.recyclerView.setLayoutManager(layoutManager);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public Runnable UpdateRecyclerView = new Runnable() {
        @Override
        public void run() {
            Log.d("AlbumsFragment", "UpdateRecyclerView: " + viewModel.getAlbums().get(0).getCoverPhotoPath());
            binding.recyclerView.setAdapter(new AlbumsAdapter(
                    getActivity(),
                    viewModel.getAlbums()
            ));
        }
    };
}