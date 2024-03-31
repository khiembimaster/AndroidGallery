package android21ktpm3.group07.androidgallery.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Photo implements Parcelable {
    private final String path;
    private final String name;
    private final long modifiedDate;

    private String tags;

    private double fileSize;



    public Photo(String path, String name, long modifiedDate, String tags, double fizeSize) {
        this.path = path;
        this.name = name;
        this.modifiedDate = modifiedDate;
        this.tags = tags;
        this.fileSize = fizeSize;
    }
    protected Photo(Parcel in) {
        this.path = in.readString();
        this.name = in.readString();
        this.modifiedDate = in.readLong();
        this.fileSize = in.readDouble();
        this.tags = in.readString();
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public long getModifiedDate() {
        return modifiedDate;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public double getFileSize() {
        return fileSize;
    }

    public void setFileSize(double fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return Objects.equals(path, photo.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public int describeContents() {
        return 0;
    }



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(name);
        dest.writeLong(modifiedDate);
        dest.writeDouble(fileSize);
        dest.writeString(tags);
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
}
