package android21ktpm3.group07.androidgallery;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import android21ktpm3.group07.androidgallery.Workers.PhotoSyncWorker;
import android21ktpm3.group07.androidgallery.databinding.ActivityMainBinding;
import android21ktpm3.group07.androidgallery.repositories.PhotoRepository;
import android21ktpm3.group07.androidgallery.services.PhotoService;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements
        IMenuItemHandler,
        BottomSheetFragment.OnBottomSheetItemClickListener {
    @Inject
    PhotoRepository photoRepository;

    private OnMenuItemClickListener onEditItemClickListener;
    private OnMenuItemClickListener onAccountItemClickListener;
    private OnMenuItemClickListener onCreateNewItemClickListener;
    private OnMenuItemClickListener onShareItemClickListener;
    private OnMenuItemClickListener onDeleteItemClickListener;
    private OnMenuItemClickListener onMoveItemClickListener;
    private ActivityMainBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;

    private PhotoService photoService;
    private boolean serviceBound = false;
    private boolean permissionsGranted = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private BottomSheetFragment bottomSheetFragment;
    private UserViewModel UserViewModel;
    // Firebase --------------------------------
    private FirebaseAuth auth;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;
    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    private static final String TAG = "MainActivity";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //--------------------------------------------
    private PhotoSyncWorker photoSyncWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SavedStateViewModelFactory factory = new SavedStateViewModelFactory(
                this.getApplication(), this
        );
        UserViewModel =
                new ViewModelProvider(this, factory).get(UserViewModel.class);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.materialToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.account) {
                toggleBottomSheet();
                return true;
            } else if (item.getItemId() == R.id.create_new) {
                try {
                    try {
                        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // display error state to the user
                    }
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }
                return true;
            } else if (item.getItemId() == R.id.share) {
                onShareItemClickListener.onClicked();
                return true;
            } else if (item.getItemId() == R.id.delete) {
                onDeleteItemClickListener.onClicked();
                return true;
            } else if (item.getItemId() == R.id.edit) {
                onEditItemClickListener.onClicked();
                return true;
            } else if (item.getItemId() == R.id.move) {
                onMoveItemClickListener.onClicked();
                return true;
            }

            return false;
        });

        Intent photoServiceIntent = new Intent(this, PhotoService.class);
        startService(photoServiceIntent);

        // bind photo service
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                PhotoService.LocalBinder binder = (PhotoService.LocalBinder) service;
                photoService = binder.getService();

                serviceBound = true;
                doWhenServiceBoundAndPermissionsGranted();
                doWhenServiceBoundAndUserSignedIn();
                Log.d(TAG, "Service connected");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceBound = false;
                Log.d(TAG, "Service disconnected");
            }
        };
        bindService(new Intent(this, PhotoService.class), connection, BIND_AUTO_CREATE);

        requestPermission();

        if (bottomSheetFragment == null) {
            bottomSheetFragment = new BottomSheetFragment();
        }


        auth = FirebaseAuth.getInstance();
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        Log.d(TAG, "onCreate");
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Log.d(TAG, "User is signed in");
            showOneTapUI = false;
            bottomSheetFragment.showUserInfo(user);

            // FIXME change this shenanigan
            doWhenServiceBoundAndUserSignedIn();
            doWhenUserSignedIn();
        } else {
            Log.d(TAG, "User is signed out");
            showOneTapUI = true;
            bottomSheetFragment.showUserInfo(null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void setOnAccountItemClickListener(OnMenuItemClickListener listener) {
        this.onAccountItemClickListener = listener;
    }

    @Override
    public void setOnCreateNewItemClickListener(OnMenuItemClickListener listener) {
        this.onCreateNewItemClickListener = listener;
    }

    @Override
    public void setOnShareItemClickListener(OnMenuItemClickListener listener) {
        this.onShareItemClickListener = listener;
    }

    @Override
    public void setOnDeleteItemClickListener(OnMenuItemClickListener listener) {
        this.onDeleteItemClickListener = listener;
    }

    @Override
    public void setOnEditItemClickListener(OnMenuItemClickListener listener) {
        this.onEditItemClickListener = listener;
    }

    @Override
    public void setOnMoveItemClickListener(OnMenuItemClickListener listener) {
        this.onMoveItemClickListener = listener;
    }

    @Override
    public Menu getMenu() {
        return binding.materialToolbar.getMenu();
    }

    @Override
    public void hideToolbar() {
    }

    @Override
    public void showToolbar() {
    }

    //  TODO: Move to application class ?
    private void requestPermission() {
        ActivityResultLauncher<String[]> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                        isGranted -> {
                            for (Map.Entry<String, Boolean> entry : isGranted.entrySet()) {
                                String permission = entry.getKey();
                                Boolean granted = entry.getValue();
                                Log.d(TAG, permission + " " + granted);

                                if (!granted) {
                                    finish();
                                }
                            }

                            permissionsGranted = true;
                            doWhenPermissionsGranted();
                            // doWhenServiceBoundAndPermissionsGranted();
                            Log.d(TAG, "Authentication finished");
                        }
                );

        try {
            String pkgName = getPackageName();
            ArrayList<String> permissions = new ArrayList<>(Arrays.asList(getPackageManager()
                    .getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS)
                    .requestedPermissions));

            // minSdkVersion doesn't work so we have to manually do this
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                permissions.remove("android.permission.READ_MEDIA_IMAGES");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions.add("android.permission.ACCESS_MEDIA_LOCATION");
            }

            requestPermissionLauncher.launch(permissions.toArray(new String[0]));
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    private void signIn() {
        // Use your app or activity context to instantiate a client instance of
        // CredentialManager.
        CredentialManager credentialManager = CredentialManager.create(this);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(getString(R.string.web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        // Launch sign in flow and do getCredential Request to retrieve the credentials
        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result);
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        /*handleFailure(e);*/
                        e.printStackTrace();
                    }
                }

        );
    }

    public void handleSignIn(GetCredentialResponse result) {
        // Handle the successfully returned credential.
        Credential credential = result.getCredential();

        if (credential instanceof CustomCredential) {
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                // Use googleIdTokenCredential and extract id to validate and
                // authenticate on your server
                GoogleIdTokenCredential googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());
                String idToken = googleIdTokenCredential.getIdToken();
                // Got an ID token from Google. Use it to authenticate
                // with Firebase.
                AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(
                                MainActivity.this,
                                task -> {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's
                                        // information
                                        Log.d("MainActivity", "signInWithCredential:success");
                                        FirebaseUser user = auth.getCurrentUser();
                                        updateUI(user);
                                        db.collection("users").document(user.getUid()).set(new HashMap<>())
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "DocumentSnapshot successfully" +
                                                            " " +
                                                            "written!");
                                                })
                                                .addOnFailureListener(e -> Log.w(TAG, "Error " +
                                                        "writing " +
                                                        "document", e));


                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("MainActivity", "signInWithCredential:failure",
                                                task.getException());
                                        updateUI(null);
                                    }
                                })
                        .addOnCanceledListener(() -> {
                            Log.d(TAG, "signInWithCredential: canceled");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error signing in with credential", e);
                        });

            } else {
                // Catch any unrecognized custom credential type here.
                Log.e(TAG, "Unexpected type of credential");
            }
        }
    }

    private void signOut() {
        auth.signOut();
        updateUI(null);
        showOneTapUI = true;

        executor.execute(() -> {
            photoRepository.setFirebaseUser(null);
            photoRepository.getAllRemotePhotos();
        });
    }

    private void toggleBottomSheet() {
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    // TODO: Any way to ensure service is started and bound before calling it?
    private void doWhenServiceBoundAndPermissionsGranted() {
        if (!serviceBound || !permissionsGranted) return;

        // photoService.getLocalPhotos();
    }

    private void doWhenServiceBoundAndUserSignedIn() {
        // if (!serviceBound) return;
        // FirebaseUser user = auth.getCurrentUser();
        // if (user == null) return;
        //
        //
        // photoService.setFirebaseUser(user);
        // photoRepository.getAllRemotePhotos();
        // photoService.test();
        // photoService.getRemotePhotos();
    }

    private void doWhenPermissionsGranted() {
        executor.execute(() -> {
            photoRepository.getAllLocalPhotos();
        });

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            executor.execute(() -> {
                photoRepository.setFirebaseUser(user);
                photoRepository.getAllRemotePhotos();
            });
        }
    }

    private void doWhenUserSignedIn() {
        Log.d(TAG, "doWhenUserSignedIn");

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        photoRepository.setFirebaseUser(user);

        if (!permissionsGranted) return;

        executor.execute(() -> {
            photoRepository.getAllRemotePhotos();
        });
    }

    // TODO update this shenanigan in the next commit
    // private void updateSyncingStatus() {
    //     Fragment fragment =
    //             getSupportFragmentManager().findFragmentById(R.id
    //             .nav_host_fragment_activity_main);
    //     PhotosFragment pf =
    //             (PhotosFragment) fragment.getChildFragmentManager().getFragments().get(0);
    //
    //
    //     photoService.updateSyncingStatus(pf.photosViewModel.getPhotosData());
    // }

    @Override
    public void onBottomSheetItemClick(String item) {
        switch (item) {
            case "signInWithGoogle":

                signIn();
                return;
            case "logout":
                signOut();
                return;
            default:
        }
    }

    @Override
    public UserViewModel getUserViewModel() {
        return UserViewModel;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}