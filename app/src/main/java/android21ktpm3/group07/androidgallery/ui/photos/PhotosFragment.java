package android21ktpm3.group07.androidgallery.ui.photos;

import static androidx.core.content.FileProvider.getUriForFile;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
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
import android21ktpm3.group07.androidgallery.services.PhotoService;

public class PhotosFragment extends Fragment {
    protected PhotosViewModel photosViewModel;
    protected IMenuItemHandler handler;

    private FragmentPhotosBinding binding;
    private Menu menu;
    private PhotoService photoService;
    private Context context;
    private PhotosRecyclerAdapter adapter;

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("PhotosFragment", "Received broadcast");

            photosViewModel.AddPhotos(photoService.photos);

            if (adapter == null) {
                initializeRecyclerView();
            }
        }
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

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();

        Intent photoServiceIntent = new Intent(context, PhotoService.class);
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                PhotoService.LocalBinder binder = (PhotoService.LocalBinder) service;
                photoService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("PhotoService", "Service disconnected");
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                    new MyReceiver(),
                    new IntentFilter(PhotoService.ACTION_GET_LOCAL_PHOTOS),
                    Context.RECEIVER_NOT_EXPORTED
            );
        } else {
            context.registerReceiver(
                    new MyReceiver(),
                    new IntentFilter(PhotoService.ACTION_GET_LOCAL_PHOTOS)
            );
        }

        context.startService(photoServiceIntent);
        context.bindService(photoServiceIntent, connection, Context.BIND_AUTO_CREATE);

        photosViewModel = new ViewModelProvider(this).get(PhotosViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPhotosBinding.inflate(inflater, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initializeRecyclerView() {
        adapter = new PhotosRecyclerAdapter(
                context,
                photosViewModel.getPhotos()
        );
        adapter.setChildItemSelectedListener(photo -> {
            displayShareOptionItem();
            photosViewModel.addToSelectedPhotos(photo);
        });
        adapter.setChildItemUnselectedListener(photo -> {
            photosViewModel.removeFromSelectedPhotos(photo);
            if (photosViewModel.getSelectedPhotos().isEmpty()) {
                hideShareOptionItem();
            }
        });
        adapter.setChildItemViewListener(this::viewPhoto);
        binding.recyclerView.setAdapter(adapter);
    }

    public void displayShareOptionItem() {
        handler.getMenu().findItem(R.id.share)
                .setVisible(true)
                .setEnabled(true);
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
                    context,
                    "android21ktpm3.group07.androidgallery.fileprovider",
                    file);
            imageUris.add(imageUri);
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUris);
        shareIntent.setType("image/*");

        startActivity(Intent.createChooser(shareIntent, "Share images to..."));
    }

    public void viewPhoto(Photo photo) {
        Intent intent = new Intent(getContext(), ImageActivity.class);
        intent.putExtra("photo_path", photo.getPath());
        intent.putExtra("photo_tags", photo.getTags());
        intent.putExtra("photo_date", photo.getModifiedDate());
        intent.putExtra("photo_size", photo.getFileSize());

        startActivity(intent);
    }

    private void upLoadToFirebase() {

    }
}