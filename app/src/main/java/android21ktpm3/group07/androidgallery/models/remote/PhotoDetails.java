package android21ktpm3.group07.androidgallery.models.remote;

import com.google.firebase.firestore.PropertyName;

public class PhotoDetails {
    @PropertyName("local")
    public String localPath;

    @PropertyName("remote")
    public String remoteUrl;

    public PhotoDetails() {
    }

    public PhotoDetails(String localPath, String remoteUrl) {
        this.localPath = localPath;
        this.remoteUrl = remoteUrl;
    }
}
