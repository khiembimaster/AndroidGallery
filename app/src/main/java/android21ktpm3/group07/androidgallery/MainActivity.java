package android21ktpm3.group07.androidgallery;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;

import android21ktpm3.group07.androidgallery.databinding.ActivityMainBinding;
import android21ktpm3.group07.androidgallery.ui.photos.Image;
import android21ktpm3.group07.androidgallery.ui.photos.PhotoAdapter;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PhotoAdapter photoAdapter;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }
    @Override
    protected void onResume() {
        super.onResume();

       System.out.println("check xem co quya lai khong");
    }

}