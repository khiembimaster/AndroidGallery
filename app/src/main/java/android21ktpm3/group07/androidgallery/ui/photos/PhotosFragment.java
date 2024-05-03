package android21ktpm3.group07.androidgallery.ui.photos;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import android21ktpm3.group07.androidgallery.IMenuItemHandler;
import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.databinding.FragmentPhotosBinding;
import android21ktpm3.group07.androidgallery.helpers.IntentSenderLauncher;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;
import android21ktpm3.group07.androidgallery.services.PhotoService;
import android21ktpm3.group07.androidgallery.ui.editor.PhotoEditor;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PhotosFragment extends Fragment {
    @Inject
    PhotoRepository photoRepository;
    @Inject
    ExecutorService executor;

    public PhotosViewModel photosViewModel;
    protected IMenuItemHandler menuItemHandler;
    protected PhotoService photoService;

    private final String TAG = "PhotosFragment";
    protected final Handler threadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
    private IntentSenderLauncher intentSenderLauncher;
    private FragmentPhotosBinding binding;
    protected Context context;
    private PhotosRecyclerAdapter adapter;
    protected boolean isBound = false;
    protected ServiceConnection connection;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof IMenuItemHandler)) return;

        intentSenderLauncher = new IntentSenderLauncher(this);

        menuItemHandler = (IMenuItemHandler) context;
        menuItemHandler.setOnShareItemClickListener(() -> {
            sharePhotos();
            adapter.clearSelectedPhotos();

        });
        menuItemHandler.setOnDeleteItemClickListener(() -> {
            deletePhotos();
            adapter.clearSelectedPhotos();
        });
        menuItemHandler.setOnEditItemClickListener(() -> {
            editPhoto();
            adapter.clearSelectedPhotos();
        });
        menuItemHandler.setOnCreateNewItemClickListener(() -> {
            photosViewModel.test();
            // photoService.updateSyncingStatus(photosViewModel.getPhotosData());
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                PhotoService.LocalBinder binder = (PhotoService.LocalBinder) service;
                photoService = binder.getService();

                photoService.registerPhotoLoadedCallback(photos ->
                        threadHandler.post(() -> loadPhotos(photos)));

                photoService.registerPhotosDeletedCallback(new PhotoService.PhotosDeletedCallback() {
                    @Override
                    public void onSuccess(List<Photo> deletedPhotos) {
                        threadHandler.post(() -> {
                            photosViewModel.RemovePhotos(deletedPhotos);
                        });
                    }

                    @Override
                    public void onFailure(List<Photo> deletedPhotos) {
                        threadHandler.post(() -> {
                            photosViewModel.RemovePhotos(deletedPhotos);
                            Snackbar.make(
                                    binding.getRoot(),
                                    "Failed to delete photo(s)",
                                    Snackbar.LENGTH_SHORT
                            ).show();
                        });
                    }
                });

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

        photosViewModel = new ViewModelProvider(this).get(PhotosViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPhotosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        binding.recyclerView.setLayoutManager(layoutManager);

        // Initialize the RecyclerView adapter
        initializeRecyclerView();

        // Set the adapter to the RecyclerView
        binding.recyclerView.setAdapter(adapter);

        initializeRecyclerView();
    }

    @Override
    public void onDestroyView() {
        binding.recyclerView.setAdapter(null);
        binding = null;
        super.onDestroyView();
    }

    private void initializeRecyclerView() {
        adapter = new PhotosRecyclerAdapter(
                context,
                photosViewModel.getPhotoGroups()
        );

        adapter.setChildItemActionCallback(new PhotoAdapter.ItemActionCallback() {
            @Override
            public void onItemSelect(Photo photo) {
                // Toast.makeText(context, "Selected " + photo.getPath(), Toast.LENGTH_SHORT)
                // .show();
            }

            @Override
            public void onItemUnselect(Photo photo) {
                // Toast.makeText(context, "Unselected " + photo.getPath(), Toast.LENGTH_SHORT)
                // .show();
            }

            @Override
            public void onItemView(Photo photo) {
                viewPhoto(photo);
            }
        });
        adapter.setSelectingModeDisplayingCallback(new PhotosRecyclerAdapter.SelectingModeDisplayingCallback() {
            @Override
            public void onExit() {
                hideFragmentOptionItems();
            }

            @Override
            public void onEnter() {
                displayFragmentOptionItems();
            }
        });

        binding.recyclerView.setAdapter(adapter);
    }

    public void displayFragmentOptionItems() {
        Menu menu = menuItemHandler.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            int itemId = item.getItemId();

            if (itemId == R.id.share || itemId == R.id.delete || itemId == R.id.edit) {
                item.setVisible(true);
                item.setEnabled(true);
            } else {
                item.setVisible(false);
                item.setEnabled(false);
            }
        }
    }

    public void hideFragmentOptionItems() {
        Menu menu = menuItemHandler.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            int itemId = item.getItemId();

            if (itemId == R.id.share || itemId == R.id.delete || itemId == R.id.edit) {
                item.setVisible(false);
                item.setEnabled(false);
            } else {
                item.setVisible(true);
                item.setEnabled(true);
            }
        }
    }

    public void sharePhotos() {
        ArrayList<Uri> imageUris = new ArrayList<>();

        for (Photo photo : adapter.getSelectedPhotos()) {
            Log.d(TAG, "Sharing " + photo.getPath() + " with uri " + photo.getContentUri());
            imageUris.add(photo.getContentUri());
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUris);
        shareIntent.setType("image/*");

        startActivity(Intent.createChooser(shareIntent, "Share images to..."));
    }

    public void deletePhotos() {
        List<Photo> selectedPhotos = adapter.getSelectedPhotos();
        executor.execute(() -> {
            photoRepository.deleteLocalPhotos(
                    selectedPhotos,
                    intentSenderLauncher,
                    null
            );
        });
    }

    public void editPhoto() {
        if (adapter.getSelectedPhotos().size() > 1) {
            Snackbar.make(
                    binding.getRoot(),
                    "Cannot edit multiple photos",
                    Snackbar.LENGTH_SHORT
            ).show();
            return;
        }
        // start editor activity
        Intent intent = new Intent(getContext(), PhotoEditor.class);
        intent.setData(Uri.parse(adapter.getSelectedPhotos().get(0).getPath()));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting editor activity");
        }
    }

    public void viewPhoto(Photo photo) {
        Intent intent = new Intent(getContext(), ImageActivity.class);
        intent.putExtra("photo_path", photo.getPath());
        intent.putExtra("photo_tags", photo.getTags());
        intent.putExtra("photo_date", photo.getModifiedDate());
        intent.putExtra("photo_size", photo.getFileSize());
        intent.putExtra("photo_name", photo.getName());
        intent.putExtra("photo_takenDate", photo.getTakenDate());
        intent.putExtra("photo_isFavourite", photo.getIsFavourite());
        // intent.putExtra("photo_id", photo.getId());


        startActivity(intent);
    }

    protected void loadPhotos(ArrayList<Photo> photos) {
        photosViewModel.AddPhotos(photos);

        if (adapter == null) {
            initializeRecyclerView();
        }
    }

    private void test() {

    }
}