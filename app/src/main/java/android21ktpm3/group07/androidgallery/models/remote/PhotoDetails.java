package android21ktpm3.group07.androidgallery.models.remote;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;

public class PhotoDetails {
    @PropertyName("path")
    public String localPath;

    @PropertyName("url")
    public String remoteUrl;

    @PropertyName("lastModified")
    public Date lastModified;

    public PhotoDetails() {
    }

    public PhotoDetails(String localPath, String remoteUrl, Date lastModified) {
        this.localPath = localPath;
        this.remoteUrl = remoteUrl;
        this.lastModified = lastModified;
    }
}
