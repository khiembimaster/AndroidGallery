package android21ktpm3.group07.androidgallery.models.remote;

import com.google.firebase.firestore.PropertyName;

public class PhotoDetails {
    @PropertyName("path")
    public String localPath;

    @PropertyName("url")
    public String remoteUrl;

    public PhotoDetails() {
    }

    public PhotoDetails(String localPath, String remoteUrl) {
        this.localPath = localPath;
        this.remoteUrl = remoteUrl;
    }
}
