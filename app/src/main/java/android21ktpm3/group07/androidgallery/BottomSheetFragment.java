package android21ktpm3.group07.androidgallery;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

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
import java.util.concurrent.TimeUnit;

import android21ktpm3.group07.androidgallery.databinding.FragmentBottomSheetBinding;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;

public class BottomSheetFragment extends BottomSheetDialogFragment {
    private static final String TAG = "BottomSheetFragment";
    private UserViewModel UserViewModel;
    private OnBottomSheetItemClickListener listener;
    private FragmentBottomSheetBinding binding;

    // To use default options:
    public BottomSheetFragment() {
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
        View root = binding.getRoot();

        if(getContext() instanceof MainActivity){
            listener =(OnBottomSheetItemClickListener) getContext();
            UserViewModel = listener.getUserViewModel();
        }else {
            throw new RuntimeException(getContext().toString()
                    + " must implement OnBottomSheetItemClickListener");
        }

        ProgressBar progressBar = binding.progressBar;
        Button btnBackupData = binding.btnBackupData;
        Button btnLogout = binding.btnLogout;
        Button btnSignInWithGoogle = binding.btnSignInWithGoogle;
        UserViewModel.getCanUpload().observe(getViewLifecycleOwner(), btnBackupData::setEnabled);
        UserViewModel.getCanSignIn().observe(getViewLifecycleOwner(), btnSignInWithGoogle::setEnabled);
        UserViewModel.getCanLogOut().observe(getViewLifecycleOwner(), btnLogout::setEnabled);
        UserViewModel.getFirebaseUser().observe(getViewLifecycleOwner(), this::showUserInfo);
        UserViewModel.getIsProcessing().observe(getViewLifecycleOwner(), progressBar::setIndeterminate);

        binding.btnBackupData.setOnClickListener(v -> {
            UserViewModel.setIsProcessing(true);
            UserViewModel.setCanUpload(false);

//            PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(PhotoUploadWorker.class, 24, TimeUnit.HOURS)
//                        .setConstraints(new Constraints.Builder()
//                            .setRequiresCharging(true)
//                            .build())
//                    .build();
//            WorkManager
//                    .getInstance(requireContext())
//                    .enqueueUniquePeriodicWork("sendLogs", ExistingPeriodicWorkPolicy.KEEP, workRequest);
              WorkRequest workRequest = new OneTimeWorkRequest.Builder(PhotoUploadWorker.class)
                      .build();
            WorkManager
                    .getInstance(requireContext())
                    .enqueue(workRequest);
         });
        binding.btnLogout.setOnClickListener(v -> {
            listener.onBottomSheetItemClick("logout");
        });
        binding.btnSignInWithGoogle.setOnClickListener(v -> {
            UserViewModel.setCanSignIn(false);
            UserViewModel.setIsProcessing(true);
            binding.progressBar.setVisibility(View.VISIBLE);
            listener.onBottomSheetItemClick("signInWithGoogle");
        });


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null){
            showUserInfo(auth.getCurrentUser());
        }
    }

    public void showUserInfo(FirebaseUser user){
        if (user == null) {
            binding.imgUserAvatar.setImageResource(R.drawable.account_circle_fill1_wght500_grad200_opsz24);
            binding.txtDisplayName.setText("Guest");
            binding.txtUserEmail.setText("Please sign in to use this feature");
            UserViewModel.setCanLogOut(false);
            UserViewModel.setCanSignIn(true);
            UserViewModel.setCanUpload(false);
            return;
        };

        UserViewModel.setCanLogOut(true);
        UserViewModel.setCanSignIn(false);
        UserViewModel.setCanUpload(true);
        UserViewModel.setIsProcessing(false);

        Glide.with(this)
                .load(user.getPhotoUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imgUserAvatar);

        binding.txtDisplayName.setText(user.getDisplayName());
        binding.txtUserEmail.setText(user.getEmail());
    }

    public interface OnBottomSheetItemClickListener {
        void onBottomSheetItemClick(String item);
        UserViewModel getUserViewModel();
    }

}
