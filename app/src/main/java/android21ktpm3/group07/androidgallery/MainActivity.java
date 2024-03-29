package android21ktpm3.group07.androidgallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.signin.internal.SignInClientImpl;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android21ktpm3.group07.androidgallery.databinding.ActivityMainBinding;
import android21ktpm3.group07.androidgallery.models.Album;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.ui.photos.ImageActivity;
import android21ktpm3.group07.androidgallery.ui.photos.PhotoAdapter;

public class MainActivity extends AppCompatActivity implements IMenuItemHandler{
    private MaterialToolbar.OnMenuItemClickListener onMenuItemClickListener;
    private ActivityMainBinding binding;
    public PhotoAdapter.OnItemSelectedListener childSelectedCB;
    // Firebase
    private FirebaseAuth auth;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;
    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    private static final String TAG = "MainActivity";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    //--------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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

        binding.materialToolbar.setOnMenuItemClickListener(item -> {
            if (onMenuItemClickListener != null) {
                return onMenuItemClickListener.onMenuItemClick(item);
            }
            return false;
        });

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);


        Log.d(TAG, "onCreate");
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

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);

        this.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.account) {
                if (showOneTapUI) {
                    signIn();
                }
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
                    } catch (Exception e) {
                        Log.e("MainActivity", "Couldn't start One Tap UI: " + e);
                    }
                    showOneTapUI = false;
                })
                .addOnFailureListener(this, e -> {
                    Log.e("MainActivity", e.getLocalizedMessage());
                });
    }

    private void signOut(){
        auth.signOut();
        showOneTapUI = true;
    }

    private void upLoadUserImages(FirebaseUser user){
        if (user == null) return;


        // Load images from local storage
        List<String> imageUrls = new ArrayList<>(); //TODO: Load images from local storage
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
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .build();
        for(String url : imageUrls){
            StorageReference imageRef = storageRef.child(user.getUid()+"/images/"+"/"+url);

            // Upload file to Firebase Storage
            UploadTask uploadTask = imageRef.putFile(Uri.fromFile(new File(url)), metadata);
            uploadTask.addOnFailureListener(e -> {
                Log.e(TAG, "Error when upload your images", e);
            }).addOnSuccessListener(taskSnapshot -> {
                Log.d(TAG, "Your images stored successfully!");
            });

            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return imageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    downLoadUrls.add(downloadUri.toString());
                } else {
                    Log.e(TAG, "Error when get download url", task.getException());
                }
            });
        }


        // Save downLoadUri to Firebase Firestore
        db.collection("user_images").document(user.getUid())
                .set(downLoadUrls)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Your images stored successfully!");
                }).addOnFailureListener(e -> Log.e(TAG, "Error when upload your images", e));

        // Save albums' urls to Firebase Firestore
//        db.collection("user_albums").document(user.getUid())
//                .set(albums)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d(TAG, "Your albums stored successfully!");
//                }).addOnFailureListener(e -> Log.e(TAG, "Error when upload your albums", e));
    }
}