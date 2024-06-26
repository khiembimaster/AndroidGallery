package android21ktpm3.group07.androidgallery.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.time.LocalDate;
import java.util.Objects;

import android21ktpm3.group07.androidgallery.BR;
import android21ktpm3.group07.androidgallery.helpers.DateHelper;
import android21ktpm3.group07.androidgallery.helpers.ISelectable;

public class Photo extends BaseObservable implements Parcelable, ISelectable {
    private final String path;
    private final String name;
    private final long modifiedDate;
    private final long takenDate;
    private final Uri contentUri;

    private String tags;
    private boolean isFavourite;

    private double fileSize;

    //  Either taken date when it is not 0 or modified date
    private LocalDate representativeDate;

    private boolean isLoading = false;
    private boolean isSelected = false;
    private String remoteUrl = null;

    private boolean isAnimated = false;

    public Photo(String path, String name, long modifiedDate, long takenDate, String tags,
                 double fizeSize, Uri contentUri, boolean isFavourite) {
        this.path = path;
        this.name = name;
        this.modifiedDate = modifiedDate;
        this.takenDate = takenDate;
        this.tags = tags;
        this.fileSize = fizeSize;
        this.isFavourite = isFavourite;
        this.contentUri = contentUri;
    }


    protected Photo(Parcel in) {
        this.path = in.readString();
        this.name = in.readString();
        this.modifiedDate = in.readLong();
        this.fileSize = in.readDouble();
        this.tags = in.readString();
        this.takenDate = in.readLong();
        this.contentUri = in.readParcelable(Uri.class.getClassLoader());
        this.isFavourite = in.readByte() != 0;
        this.isSelected = in.readByte() != 0;
        this.remoteUrl = in.readString();
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

    public boolean getIsFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(boolean isFavourite) {
        this.isFavourite = isFavourite;
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

    @Bindable
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        notifyPropertyChanged(BR.selected);
        notifyChange();
    }

    @Bindable
    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
        notifyPropertyChanged(BR.remoteUrl);
    }

    public Uri getContentUri() {
        return contentUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return Objects.equals(path, photo.path);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(name);
        dest.writeLong(modifiedDate);
        dest.writeDouble(fileSize);
        dest.writeString(tags);
        dest.writeLong(takenDate);
        dest.writeParcelable(contentUri, flags);
        dest.writeByte((byte) (isFavourite ? 1 : 0));
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeString(remoteUrl);
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public void setAnimated(boolean animated) {
        this.isAnimated = animated;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public int describeContents() {
        return 0;
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
