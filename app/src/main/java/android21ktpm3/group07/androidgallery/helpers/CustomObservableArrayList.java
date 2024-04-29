package android21ktpm3.group07.androidgallery.helpers;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableArrayList;

import java.util.Collection;

public class CustomObservableArrayList<T> extends ObservableArrayList<T> {
    @Override
    public boolean removeAll(@NonNull Collection c) {
        return super.removeAll(c);
    }

}
