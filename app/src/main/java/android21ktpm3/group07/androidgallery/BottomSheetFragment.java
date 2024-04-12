package android21ktpm3.group07.androidgallery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import android21ktpm3.group07.androidgallery.Workers.PhotoUploadWorker;
import android21ktpm3.group07.androidgallery.Workers.PrepareBackupWorker;
import android21ktpm3.group07.androidgallery.databinding.FragmentBottomSheetBinding;

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

        Log.d(TAG, "onCreateView: ");

        // Inflate the layout for this fragment
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getContext() instanceof MainActivity) {
            listener = (OnBottomSheetItemClickListener) getContext();
            UserViewModel = listener.getUserViewModel();
        } else {
            throw new RuntimeException(getContext().toString()
                    + " must implement OnBottomSheetItemClickListener");
        }

        ProgressBar pbSignIn = binding.pbSignIn;
        ProgressBar pbBackup = binding.pbBackup;
        Button btnBackupData = binding.btnBackupData;
        Button btnLogout = binding.btnLogout;
        Button btnSignInWithGoogle = binding.btnSignInWithGoogle;
        TextView totalImages = binding.totalImages;
        UserViewModel.getCanUpload().observe(getViewLifecycleOwner(), btnBackupData::setEnabled);
        UserViewModel.getCanSignIn().observe(getViewLifecycleOwner(),
                btnSignInWithGoogle::setEnabled);
        UserViewModel.getCanLogOut().observe(getViewLifecycleOwner(), btnLogout::setEnabled);
        UserViewModel.getFirebaseUser().observe(getViewLifecycleOwner(), this::showUserInfo);
        UserViewModel.getIsSignInProcessing().observe(getViewLifecycleOwner(),
                pbSignIn::setIndeterminate);
        UserViewModel.getIsBackupProcessing().observe(getViewLifecycleOwner(),
                pbBackup::setIndeterminate);

        UserViewModel.getTotalImagesLeft().observe(getViewLifecycleOwner(), count -> {
            totalImages.setText(String.format(Locale.ENGLISH, "%d images left", count));
        });
        WorkManager workManager = WorkManager
                .getInstance(requireContext());


        if (!workManager.getWorkInfosForUniqueWork("startBackup").isDone()) {
            workManager.getWorkInfosForUniqueWorkLiveData("startBackup").observe(getViewLifecycleOwner(), workInfos -> {
                if (workInfos != null && !workInfos.isEmpty()) {
                    WorkInfo workInfo = workInfos.get(0);
                    UserViewModel.setIsBackupProcessing(true);
                    UserViewModel.setCanUpload(false);
                    updateProgress(workInfo);
                }
            });
        } else {
            UserViewModel.setIsBackupProcessing(false);
        }

        OneTimeWorkRequest prepareWorkRequest =
                new OneTimeWorkRequest.Builder(PrepareBackupWorker.class)
                        .build();
        OneTimeWorkRequest uploadWorkRequest =
                new OneTimeWorkRequest.Builder(PhotoUploadWorker.class)
                        .build();
        workManager.getWorkInfoByIdLiveData(uploadWorkRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        updateProgress(workInfo);
                    }
                });
        binding.btnBackupData.setOnClickListener(v -> {
            workManager.enqueueUniqueWork("prepareBackup", ExistingWorkPolicy.REPLACE,
                    prepareWorkRequest);
            workManager.enqueueUniqueWork("startBackup", ExistingWorkPolicy.REPLACE,
                    uploadWorkRequest);
        });


        binding.btnLogout.setOnClickListener(v -> {
            UserViewModel.setCanLogOut(false);
            UserViewModel.setIsSignInProcessing(false);
            listener.onBottomSheetItemClick("logout");
        });
        binding.btnSignInWithGoogle.setOnClickListener(v -> {
            UserViewModel.setCanSignIn(false);
            UserViewModel.setIsSignInProcessing(true);
            listener.onBottomSheetItemClick("signInWithGoogle");
        });


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        showUserInfo(auth.getCurrentUser());
    }

    private void updateProgress(WorkInfo workInfo) {
        if (workInfo != null) {
            // Update your UI with progress here
            switch (workInfo.getState()) {
                case ENQUEUED:
                    UserViewModel.setIsBackupProcessing(true);
                    UserViewModel.setCanUpload(false);
                    break;
                case RUNNING:
                    if (workInfo.getProgress() != null && workInfo.getProgress().getKeyValueMap().containsKey("total_count")) {
                        long progress = workInfo.getProgress().getLong("total_count", 0);
                        // Update your UI with progress here
                        UserViewModel.setTotalImagesLeft(progress);

                    }
                    break;
                case SUCCEEDED:
                    UserViewModel.setIsBackupProcessing(false);
                    UserViewModel.setCanUpload(true);
                    long progress = workInfo.getOutputData().getLong("total_count", 0);
                    UserViewModel.setTotalImagesLeft(progress);
                    break;
                case FAILED:
                    UserViewModel.setIsBackupProcessing(false);
                    UserViewModel.setCanUpload(true);
                    break;
            }
        }
    }

    public void showUserInfo(FirebaseUser user) {
        if (user == null) {
            binding.imgUserAvatar.setImageResource(R.drawable.account_circle_fill1_wght500_grad200_opsz24);
            binding.txtDisplayName.setText("Guest");
            binding.txtUserEmail.setText("Please sign in to use this feature");
            UserViewModel.setCanLogOut(false);
            UserViewModel.setCanSignIn(true);
            UserViewModel.setCanUpload(false);
            return;
        }
        ;

        if (Boolean.TRUE.equals(UserViewModel.getIsSignInProcessing().getValue())) {
            UserViewModel.setIsSignInProcessing(false);
        }
        if (Boolean.TRUE.equals(UserViewModel.getIsBackupProcessing().getValue())) {
            UserViewModel.setIsBackupProcessing(false);
        }

        UserViewModel.setCanLogOut(true);
        UserViewModel.setCanSignIn(false);
        UserViewModel.setCanUpload(true);

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
