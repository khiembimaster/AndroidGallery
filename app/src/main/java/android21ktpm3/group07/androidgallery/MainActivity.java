package android21ktpm3.group07.androidgallery;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import android21ktpm3.group07.androidgallery.databinding.ActivityMainBinding;
import android21ktpm3.group07.androidgallery.models.Photo;
import android21ktpm3.group07.androidgallery.ui.photos.ImageActivity;
import android21ktpm3.group07.androidgallery.ui.photos.PhotoAdapter;

public class MainActivity extends AppCompatActivity implements IMenuItemHandler{
    private MaterialToolbar.OnMenuItemClickListener onMenuItemClickListener;
    private ActivityMainBinding binding;
    public PhotoAdapter.OnItemSelectedListener childSelectedCB;


    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                for (String key : isGranted.keySet()) {
                    Log.d("MainActivity", key + " " + isGranted.get(key));
                }
            });

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



        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        try {
            String pkgName = getPackageName();
            requestPermissionLauncher.launch(
                    getPackageManager()
                            .getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS)
                            .requestedPermissions);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        Log.d("MainActivity", "onCreate");
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



}