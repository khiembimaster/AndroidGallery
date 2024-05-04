package android21ktpm3.group07.androidgallery.models.remote;

import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.Date;

public class ImageDocument {
    @PropertyName("data")
    public ArrayList<PhotoDetails> photos;

    @PropertyName("updatedAt")
    public Date updatedAt;

    public ImageDocument() {
        photos = new ArrayList<>();
    }

    public ImageDocument(ArrayList<PhotoDetails> photos, Date updatedAt) {
        this.photos = photos;
        this.updatedAt = updatedAt;
    }
}
