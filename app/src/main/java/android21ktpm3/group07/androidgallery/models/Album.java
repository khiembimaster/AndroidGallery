package android21ktpm3.group07.androidgallery.models;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import android21ktpm3.group07.androidgallery.BR;
import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.helpers.ISelectable;

public class Album extends BaseObservable implements ISelectable {
    private String name;
    private String coverPhotoPath;
    private String path;
    private int size = 0;
    private long lastModifiedDate;
    private Long BucketID;
    private boolean isSelected;
    private boolean isAnimated;

    public static final int favouriteDrawable = R.drawable.baseline_favorite_24;

    public Album() {
    }

    public Album(String name, String path, String coverPhotoPath, long lastModifiedDate,
                 Long bucketID) {
        this.name = name;
        this.path = path;
        this.size = 1;
        this.coverPhotoPath = coverPhotoPath;
        this.lastModifiedDate = lastModifiedDate;
        this.BucketID = bucketID;
    }

    public String getName() {
        return name;
    }

    @Bindable
    public String getCoverPhotoPath() {
        return coverPhotoPath;
    }

    public String getPath() {
        return path;
    }

    @Bindable
    public int getSize() {
        return size;
    }

    @Bindable
    public long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public Long getBucketID() {
        return BucketID;
    }

    @Override
    public boolean isAnimated() {
        return isAnimated;
    }

    @Override
    public void setAnimated(boolean animated) {
        isAnimated = animated;
    }

    @Bindable
    public boolean isSelected() {
        return isSelected;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setLastModifiedDate(long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        notifyPropertyChanged(BR.lastModifiedDate);
    }

    public void setCoverPhotoPath(String coverPhotoPath) {
        this.coverPhotoPath = coverPhotoPath;
        notifyPropertyChanged(BR.coverPhotoPath);
    }

    public void setSize(int size) {
        this.size = size;
        notifyPropertyChanged(BR.size);
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        notifyPropertyChanged(BR.selected);
    }

    @NonNull
    @Override
    public String toString() {
        return path;
    }
}
