package android21ktpm3.group07.androidgallery;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android21ktpm3.group07.androidgallery.databinding.ActivityMainBinding;
import android21ktpm3.group07.androidgallery.services.PhotoService;
import android21ktpm3.group07.androidgallery.ui.photos.PhotoAdapter;

public class MainActivity extends AppCompatActivity implements IMenuItemHandler {
    public PhotoAdapter.OnItemSelectedListener childSelectedCB;

    private MaterialToolbar.OnMenuItemClickListener onMenuItemClickListener;
    private ActivityMainBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;

    private PhotoService photoService;
    private boolean serviceBound = false;
    private boolean permissionsGranted = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                doWhenServiceBound();
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

        //        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);

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
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                    SignInCredential googleCredential = null;
                    try {
                        googleCredential =
                                oneTapClient.getSignInCredentialFromIntent(result.getData());
                    } catch (ApiException e) {
                        throw new RuntimeException(e);
                    }
                    String idToken = googleCredential.getGoogleIdToken();
                    if (idToken != null) {
                        // Got an ID token from Google. Use it to authenticate
                        // with Firebase.
                        AuthCredential firebaseCredential =
                                GoogleAuthProvider.getCredential(idToken, null);
                        auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(MainActivity.this, task -> {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's
                                        // information
                                        Log.d("MainActivity", "signInWithCredential:success");
                                        FirebaseUser user = auth.getCurrentUser();

                                        // Map<String, ArrayList<String>> data = new HashMap<>();
                                        // data.put("images", new ArrayList<>());
                                        //
                                        // db.collection("users").document(user.getUid()).set(data)
                                        //         .addOnSuccessListener(aVoid -> Log.d(TAG,
                                        //                 "DocumentSnapshot successfully
                                        //                 written!"))
                                        //         .addOnFailureListener(e -> Log.w(TAG, "Error " +
                                        //                 "writing document", e));

                                        //  TODO Is executing this in a separate thread necessary?
                                        executor.execute(() -> {
                                            Map<String, Object> data = new HashMap<>();
                                            data.put("images", new ArrayList<>());

                                            db.collection("users").document(user.getUid())
                                                    .set(data, SetOptions.merge())
                                                    .addOnSuccessListener(aVoid -> Log.d(TAG,
                                                            "DocumentSnapshot successfully " +
                                                                    "written!"))
                                                    .addOnFailureListener(e -> Log.w(TAG, "Error " +
                                                            "writing " +
                                                            "document", e));
                                        });

                                        updateUI(user);
                                        doWhenServiceBound();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("MainActivity", "signInWithCredential:failure",
                                                task.getException());
                                        updateUI(null);
                                    }
                                });
                    }
                });

        binding.materialToolbar.setOnMenuItemClickListener(item -> {
            if (onMenuItemClickListener != null) {
                return onMenuItemClickListener.onMenuItemClick(item);
            }
            return false;
        });

        NavController navController = Navigation.findNavController(this,
                R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);
        doWhenServiceBound();

        this.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.account) {
                if (showOneTapUI) {
                    signIn();
                } else toggleBottomSheet();
                return true;
            }

            return false;
        });
    }

    @Override
    public void setOnMenuItemClickListener(MaterialToolbar.OnMenuItemClickListener listener) {
        onMenuItemClickListener = listener;
        if (binding != null) {
            binding.materialToolbar.setOnMenuItemClickListener(listener);
        }
    }

    @Override
    public Menu getMenu() {
        return binding.materialToolbar.getMenu();
    }

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
                            doWhenServiceBoundAndPermissionsGranted();
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

            requestPermissionLauncher.launch(permissions.toArray(new String[0]));
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Log.d(TAG, "User is signed in");
            showOneTapUI = false;
            binding.materialToolbar.setTitle(user.getDisplayName());
        } else {
            Log.d(TAG, "User is signed out");
            showOneTapUI = true;
        }
    }

    private void signIn() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    try {
                        IntentSenderRequest intentSenderRequest =
                                new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender())
                                        .build();
                        activityResultLauncher.launch(intentSenderRequest);
                        showOneTapUI = false;
                    } catch (Exception e) {
                        Log.e("MainActivity", "Couldn't start One Tap UI: " + e);
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e("MainActivity", e.getLocalizedMessage());
                });
    }

    private void signOut() {
        auth.signOut();
        showOneTapUI = true;
    }

    private void toggleBottomSheet() {
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    // TODO: Any way to ensure service is started and bound before calling it?
    private void doWhenServiceBoundAndPermissionsGranted() {
        if (!serviceBound || !permissionsGranted) return;

        photoService.getLocalPhotos();
    }

    private void doWhenServiceBound() {
        if (!serviceBound) return;

        photoService.setFirebaseUser(auth.getCurrentUser());
        // photoService.test();
        photoService.getRemotePhotos();
    }
}