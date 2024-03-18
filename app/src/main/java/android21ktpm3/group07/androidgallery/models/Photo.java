package android21ktpm3.group07.androidgallery.models;

import java.util.Objects;

public class Photo {
    private final String path;
    private final String name;
    private final long modifiedDate;

    public Photo(String path, String name, long modifiedDate) {
        this.path = path;
        this.name = name;
        this.modifiedDate = modifiedDate;
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
}
