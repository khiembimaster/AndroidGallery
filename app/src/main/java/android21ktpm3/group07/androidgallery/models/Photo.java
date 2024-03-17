package android21ktpm3.group07.androidgallery.models;

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
}
