package android21ktpm3.group07.androidgallery;

import android.view.Menu;

import com.google.android.material.appbar.MaterialToolbar;

public interface IMenuItemHandler {
    void setOnMenuItemClickListener(MaterialToolbar.OnMenuItemClickListener listener);

    Menu getMenu();
}
