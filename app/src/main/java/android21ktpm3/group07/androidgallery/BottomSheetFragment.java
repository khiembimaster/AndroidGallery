package android21ktpm3.group07.androidgallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android21ktpm3.group07.androidgallery.databinding.FragmentBottomSheetBinding;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    private static final String TAG = "BottomSheetFragment";
    Context context;
    FragmentBottomSheetBinding binding;
    FirebaseAuth auth = null;

    // To use default options:
    ImageLabeler labeler;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();

    public BottomSheetFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        auth = FirebaseAuth.getInstance();
        // Inflate the layout for this fragment
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false);

        binding.btnLogout.setOnClickListener(v -> {
            if(context instanceof MainActivity){
                ((MainActivity) context).onBottomSheetItemClick("logout");
            }
        });
        binding.btnSignInWithGoogle.setOnClickListener(v -> {
            if(context instanceof MainActivity){
                ((MainActivity) context).onBottomSheetItemClick("signInWithGoogle");
            }
        });

        // Or, to set the minimum confidence required:
         ImageLabelerOptions options =
             new ImageLabelerOptions.Builder()
                 .setConfidenceThreshold(0.5f)
                 .build();
         labeler = ImageLabeling.getClient(options);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();

        refreshUI(currentUser);

    }

    //TODO: Create a service to upload images to Firebase Storage
    private void upLoadUserImages(FirebaseUser user){
        if (user == null) return;


        // Load images from local storage
        PhotoRepository photoRepository = new PhotoRepository(context);
        List<Photo> imageUrls = photoRepository.GetAllPhotos(); //TODO: Load images from local storage
        List<String> downLoadUrls = new ArrayList<>();

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
                    Glide.with(this)
                            .asBitmap()
                            .load(downloadUri.toString())
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    // Bitmap is ready here
                                    InputImage image = InputImage.fromBitmap(resource, 0);
                                    labeler.process(image).addOnSuccessListener(imageLabels -> {
                                        List <String> labels = new ArrayList<>();
                                        for (ImageLabel label : imageLabels) {
                                            labels.add(label.getText());
                                        }
                                        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                                        Map<String, Object> data = new HashMap<>();
                                        data.put("url", downloadUri.toString());
                                        data.put("labels", labels);
                                        db.collection("users")
                                                .document(userId)
                                                .collection("images")
                                                .add(data)
                                                .addOnSuccessListener(documentReference -> {
                                                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                                }).addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error adding document", e);
                                                });
                                        return;
                                    }).addOnFailureListener(e -> Log.e(TAG, "Error when upload your images", e));
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                    // Called when the drawable is removed from the target
                                }
                            });

                } else {
                    Log.e(TAG, "Error when get download url", task.getException());
                }
            });
        }
    }

    public void refreshUI(FirebaseUser user){
        if (user == null) {
            binding.imgUserAvatar.setImageResource(R.drawable.account_circle_fill1_wght500_grad200_opsz24);
            binding.txtDisplayName.setText("Guest");
            binding.txtUserEmail.setText("Please sign in to use this feature");
            binding.btnBackupData.setVisibility(View.GONE);
            binding.btnLogout.setVisibility(View.GONE);
            return;
        };

        Glide.with(this)
                .load(user.getPhotoUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imgUserAvatar);

        binding.txtDisplayName.setText(user.getDisplayName());
        binding.txtUserEmail.setText(user.getEmail());
        binding.btnBackupData.setOnClickListener(v -> {
            upLoadUserImages(user);
        });
    }

    public interface OnBottomSheetItemClickListener {
        void onBottomSheetItemClick(String item);
    }

}
