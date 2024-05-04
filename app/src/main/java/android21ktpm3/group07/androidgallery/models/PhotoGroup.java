package android21ktpm3.group07.androidgallery.models;

import androidx.databinding.ObservableList;

import java.time.LocalDate;

public class PhotoGroup {
    private final LocalDate date;
    private final ObservableList<Photo> photos;
    
    public PhotoGroup(LocalDate localDate, ObservableList<Photo> photos) {
        this.date = localDate;
        this.photos = photos;
    }

    public ObservableList<Photo> getPhotos() {
        return photos;
    }

    public LocalDate getDate() {
        return date;
    }
}
