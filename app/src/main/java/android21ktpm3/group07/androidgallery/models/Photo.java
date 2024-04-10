package android21ktpm3.group07.androidgallery.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDate;
import java.util.Objects;

import android21ktpm3.group07.androidgallery.helpers.DateHelper;

public class Photo implements Parcelable {
    private final String path;
    private final String name;
    private final long modifiedDate;
    private final long takenDate;

    private String tags;

    private double fileSize;

    //  Either taken date when it is not 0 or modified date
    private LocalDate representativeDate;

    private boolean isLoading = false;


    public Photo(String path, String name, long modifiedDate, long takenDate, String tags,
                 double fizeSize) {
        this.path = path;
        this.name = name;
        this.modifiedDate = modifiedDate;
        this.takenDate = takenDate;
        this.tags = tags;
        this.fileSize = fizeSize;
    }

    protected Photo(Parcel in) {
        this.path = in.readString();
        this.name = in.readString();
        this.modifiedDate = in.readLong();
        this.fileSize = in.readDouble();
        this.tags = in.readString();
        this.takenDate = in.readLong();
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

    public long getTakenDate() {
        return takenDate;
    }

    public LocalDate getRepresentativeDate() {
        if (representativeDate == null) {
            representativeDate = DateHelper.getLocalDate(takenDate == 0 ? modifiedDate : takenDate);
        }
        return representativeDate;
    }

    public long getRepresentativeEpoch() {
        return takenDate == 0 ? modifiedDate : takenDate;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
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
