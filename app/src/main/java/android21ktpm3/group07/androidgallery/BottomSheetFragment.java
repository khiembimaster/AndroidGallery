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
import androidx.work.WorkQuery;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.Locale;

import android21ktpm3.group07.androidgallery.Workers.PhotoSyncWorker;
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
        Log.d(TAG, Thread.currentThread().getName() + " " + Thread.currentThread().getId());

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

        workManager.getWorkInfosLiveData(WorkQuery.Builder.fromStates(Collections.singletonList(WorkInfo.State.RUNNING)).build()).observe(requireActivity(), workInfos -> {
            if (!workInfos.isEmpty()) {
                // There is at least one work that is currently running
                Log.d(TAG, workInfos.toString());
                workManager.getWorkInfoByIdLiveData(workInfos.get(0).getId())
                        .observe(this, workInfo -> {
                            if (workInfo != null) {
                                UserViewModel.setIsBackupProcessing(true);
                                UserViewModel.setCanUpload(false);
                                updateProgress(workInfo);
                            }
                        });
            } else {
                // No work is running
                updateProgress(null);

                Log.d(TAG, "No work is running");

                // OneTimeWorkRequest prepareWorkRequest =
                //         new OneTimeWorkRequest.Builder(_PrepareBackupWorker.class)
                //                 .build();
                // OneTimeWorkRequest uploadWorkRequest =
                //         new OneTimeWorkRequest.Builder(_PhotoSyncWorker.class)
                //                 .build();

                OneTimeWorkRequest syncWorkRequest =
                        new OneTimeWorkRequest.Builder(PhotoSyncWorker.class)
                                .build();

                workManager.getWorkInfoByIdLiveData(syncWorkRequest.getId())
                        .observe(this, workInfo -> {
                            if (workInfo != null) {
                                updateProgress(workInfo);
                            }
                            Log.d(TAG, "uploadWorkRequest: " + workInfo.getState());
                        });
                // WorkContinuation workContinuation = workManager
                //         .beginUniqueWork("backupWork", ExistingWorkPolicy.REPLACE,
                //         prepareWorkRequest)
                //         .then(uploadWorkRequest);
                binding.btnBackupData.setOnClickListener(v -> {
                    // workContinuation.enqueue();
                    workManager.enqueueUniqueWork(
                            "syncWork",
                            ExistingWorkPolicy.KEEP,
                            syncWorkRequest
                    );
                });
            }
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
                        Log.d(TAG, "updateProgress: " + progress);
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
        } else UserViewModel.setTotalImagesLeft(0L);
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
