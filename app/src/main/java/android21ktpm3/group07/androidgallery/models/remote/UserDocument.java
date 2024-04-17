package android21ktpm3.group07.androidgallery.models.remote;

import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;

public class UserDocument {
    @PropertyName("images")
    public ArrayList<PhotoDetails> photos;

    public UserDocument() {
    }

    public UserDocument(ArrayList<PhotoDetails> photos) {
        this.photos = photos;
    }
}
