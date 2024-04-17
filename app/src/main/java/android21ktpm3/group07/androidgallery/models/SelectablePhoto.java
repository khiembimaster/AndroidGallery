package android21ktpm3.group07.androidgallery.models;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import android21ktpm3.group07.androidgallery.BR;

public class SelectablePhoto extends BaseObservable {
    private final Photo photo;
    private boolean isSelected;

    public SelectablePhoto(Photo photo) {
        this.photo = photo;
        this.isSelected = false;
    }

    public Photo getPhoto() {
        return photo;
    }

    @Bindable
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        notifyPropertyChanged(BR.selected);
    }
}
