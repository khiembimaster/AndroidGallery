package android21ktpm3.group07.androidgallery;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.concurrent.Executors;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SavedStateViewModelFactory factory = new SavedStateViewModelFactory(this.getApplication(), this);
        UserViewModel =
                new ViewModelProvider(this, factory).get(UserViewModel.class);


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

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        Log.d(TAG, "onCreate");
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Log.d(TAG, "User is signed in");
            showOneTapUI = false;
            bottomSheetFragment.showUserInfo(user);
        } else {
            Log.d(TAG, "User is signed out");
            showOneTapUI = true;
            bottomSheetFragment.showUserInfo(null);
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

        if(credential instanceof CustomCredential) {
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                // Use googleIdTokenCredential and extract id to validate and
                // authenticate on your server
                GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());
                String idToken = googleIdTokenCredential.getIdToken();
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
                                                updateUI(user);
                                            })
                                            .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("MainActivity", "signInWithCredential:failure", task.getException());
                                    updateUI(null);
                                }
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


    private void signOut(){
        auth.signOut();
        updateUI(null);
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

    @Override
    public UserViewModel getUserViewModel() {
        return UserViewModel;
    }

}