package android21ktpm3.group07.androidgallery;

import android.view.Menu;

public interface IMenuItemHandler {
    // TODO Update this to different callbacks, later on have a bar per fragment

    void setOnAccountItemClickListener(OnMenuItemClickListener listener);

    void setOnCreateNewItemClickListener(OnMenuItemClickListener listener);

    void setOnShareItemClickListener(OnMenuItemClickListener listener);

    void setOnDeleteItemClickListener(OnMenuItemClickListener listener);

    void setOnEditItemClickListener(OnMenuItemClickListener listener);

    void setOnMoveItemClickListener(OnMenuItemClickListener listener);

    Menu getMenu();

    interface OnMenuItemClickListener {
        void onClicked();
    }
}
