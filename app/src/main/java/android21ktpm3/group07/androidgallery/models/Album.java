package android21ktpm3.group07.androidgallery.models;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Objects;

public class Album {
    private String name;
    private String coverPhotoPath;
    private String path;
    private int size = 0;
    private long lastModifiedDate;

    private long BucketID;


    public Album() {}

    public Album(String name, String path, String coverPhotoPath, long lastModifiedDate, long bucketID) {
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

    public String getCoverPhotoPath() {
        return coverPhotoPath;
    }

    public String getPath() {
        return path;
    }

    public int getSize() {
        return size;
    }

    public long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public long getBucketID() {
        return BucketID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setLastModifiedDate(long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public void setCoverPhotoPath(String coverPhotoPath) {
        this.coverPhotoPath = coverPhotoPath;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @NonNull
    @Override
    public String toString() {
        return path;
    }
}
