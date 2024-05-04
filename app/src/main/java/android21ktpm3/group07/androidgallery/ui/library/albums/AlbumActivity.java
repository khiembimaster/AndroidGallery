package android21ktpm3.group07.androidgallery.ui.library.albums;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.material.snackbar.Snackbar;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.databinding.ActivityAlbumBinding;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlbumActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfiguration;
    private ActivityAlbumBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAlbumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        setSupportActionBar(binding.toolbar);

        binding.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action"
                        , Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show());
    }
}