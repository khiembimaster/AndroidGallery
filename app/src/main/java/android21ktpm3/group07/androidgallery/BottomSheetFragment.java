package android21ktpm3.group07.androidgallery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.UrlUriLoader;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android21ktpm3.group07.androidgallery.databinding.FragmentBottomSheetBinding;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    private static final String TAG = "BottomSheetFragment";
    FragmentBottomSheetBinding binding;
    FirebaseAuth auth;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();

    public BottomSheetFragment() {
        // Required empty public constructor
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false);

        FirebaseUser user = auth.getCurrentUser();
        Glide.with(this)
                .load(user.getPhotoUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imgUserAvatar);

        binding.txtDisplayName.setText(user.getDisplayName());
        binding.txtUserEmail.setText(user.getEmail());
        binding.btnLogout.setOnClickListener(v -> {
            auth.signOut();
            getActivity().finish();
        });
        binding.btnBackupData.setOnClickListener(v -> {

            upLoadUserImages(user);

        });

        return binding.getRoot();
    }

    //TODO: Create a service to upload images to Firebase Storage
    private void upLoadUserImages(FirebaseUser user){
        if (user == null) return;


        // Load images from local storage
        PhotoRepository photoRepository = new PhotoRepository(getContext());
        List<Photo> imageUrls = photoRepository.GetAllPhotos(); //TODO: Load images from local storage

        List<String> downLoadUrls = new ArrayList<>();

        // Load albums
//        List<Album> loadedalbums = new ArrayList<>();//TODO: Load albums from local storage
//        Map<String, Object> albums = new HashMap<>();
//        for(Album album : loadedalbums){
//            albums.put(album.getName(), album.getPhotos()); //TODO: Create getPhotos() method where it returns a list of photos
//        }

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        // Create file metadata including the content type
        for(Photo photo : imageUrls){

            Uri file = Uri.fromFile(new File(photo.getPath()));

            StorageReference imageRef = storageRef.child("user/"+user.getUid()+"/"+file.getLastPathSegment());
            // Upload file to Firebase Storage
            UploadTask uploadTask = imageRef.putFile(file);

            uploadTask.addOnFailureListener(e -> {
                Log.e(TAG, "Error when upload your images", e);
                int errorCode = ((StorageException) e).getErrorCode();
                String errorMessage = e.getMessage();
            }).addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Your images stored successfully!");
                binding.progressBar.setVisibility(View.GONE);
            }).addOnProgressListener(taskSnapshot -> {
                binding.progressBar.setVisibility(View.VISIBLE);
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                binding.progressBar.setProgress((int) progress);
                Log.d(TAG, "Upload is " + progress + "% done");

            }).addOnPausedListener(taskSnapshot -> {
                Log.d(TAG, "Upload is paused");
                binding.progressBar.setVisibility(View.GONE);
            });

            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return imageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    db.collection("users").document(auth.getCurrentUser().getUid())
                            .update("images", FieldValue.arrayUnion(downloadUri.toString()))
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Your images stored successfully!");
                            }).addOnFailureListener(e -> Log.e(TAG, "Error when upload your images", e));
                } else {
                    Log.e(TAG, "Error when get download url", task.getException());
                }
            });
        }


//

        // Save albums' urls to Firebase Firestore
//        db.collection("user_albums").document(user.getUid())
//                .set(albums)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d(TAG, "Your albums stored successfully!");
//                }).addOnFailureListener(e -> Log.e(TAG, "Error when upload your albums", e));
    }


    private boolean checkReadExternalPermission()
    {
        String permission = Manifest.permission.READ_MEDIA_IMAGES;
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}
