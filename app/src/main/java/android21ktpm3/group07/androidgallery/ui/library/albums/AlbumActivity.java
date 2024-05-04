package android21ktpm3.group07.androidgallery.ui.library.albums;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.material.snackbar.Snackbar;

import android21ktpm3.group07.androidgallery.IMenuItemHandler;
import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.databinding.ActivityAlbumBinding;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlbumActivity extends AppCompatActivity implements IMenuItemHandler {
    private AppBarConfiguration appBarConfiguration;
    private ActivityAlbumBinding binding;

    private OnMenuItemClickListener onEditItemClickListener;
    private OnMenuItemClickListener onAccountItemClickListener;
    private OnMenuItemClickListener onCreateNewItemClickListener;
    private OnMenuItemClickListener onShareItemClickListener;
    private OnMenuItemClickListener onDeleteItemClickListener;
    private OnMenuItemClickListener onMoveItemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAlbumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.materialToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.account) {
                return true;
            } else if (item.getItemId() == R.id.create_new) {
                onCreateNewItemClickListener.onClicked();
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


        binding.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action"
                        , Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show());
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
        binding.materialToolbar.setVisibility(View.GONE);
    }

    @Override
    public void showToolbar() {
        binding.materialToolbar.setVisibility(View.VISIBLE);
    }
}