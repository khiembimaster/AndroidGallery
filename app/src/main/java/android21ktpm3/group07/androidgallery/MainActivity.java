package android21ktpm3.group07.androidgallery;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import android21ktpm3.group07.androidgallery.databinding.ActivityMainBinding;
import android21ktpm3.group07.androidgallery.ui.photos.PhotoAdapter;

public class MainActivity extends AppCompatActivity implements
        IMenuItemHandler,
        BottomSheetFragment.OnBottomSheetItemClickListener
{
    private MaterialToolbar.OnMenuItemClickListener onMenuItemClickListener;
    private ActivityMainBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;
    private BottomSheetFragment bottomSheetFragment;
    public PhotoAdapter.OnItemSelectedListener childSelectedCB;
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

        binding.materialToolbar.setOnMenuItemClickListener(item -> {
            if (onMenuItemClickListener != null) {
                return onMenuItemClickListener.onMenuItemClick(item);
            }
            return false;
        });
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
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        SignInCredential googleCredential = null;
                        try {
                            googleCredential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                        } catch (ApiException e) {
                            throw new RuntimeException(e);
                        }
                        String idToken = googleCredential.getGoogleIdToken();
                        if (idToken !=  null) {
                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                            auth.signInWithCredential(firebaseCredential)
                                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                Log.d("MainActivity", "signInWithCredential:success");
                                                FirebaseUser user = auth.getCurrentUser();

                                                db.collection("users").document(user.getUid()).set(new HashMap<>())
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                                                })
                                                                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                                                updateUI(user);
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Log.w("MainActivity", "signInWithCredential:failure", task.getException());
                                                updateUI(null);
                                            }
                                        }
                                    });
                        }
                    }
                });

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        Log.d(TAG, "onCreate");
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Log.d(TAG, "User is signed in");
            showOneTapUI = false;
            bottomSheetFragment.refreshUI(user);
        } else {
            Log.d(TAG, "User is signed out");
            showOneTapUI = true;
            bottomSheetFragment.refreshUI(null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        this.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.account) {
                toggleBottomSheet();
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

    private void signIn(){
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    try {
                        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender())
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

    private void signOut(){
        auth.signOut();
        showOneTapUI = true;
    }

    private void toggleBottomSheet() {
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

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
}