package android21ktpm3.group07.androidgallery.ui.editor;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.databinding.ActivityPhotoEditorBinding;

public class PhotoEditor extends AppCompatActivity {
    ActivityPhotoEditorBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityPhotoEditorBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Uri uri = getIntent().getData();
        // Log.e("PhotoEditor", url);
        binding.textView.setText(uri.getPath());
    }
}