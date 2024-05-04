package android21ktpm3.group07.androidgallery.ui.search;

import java.util.Date;
import java.util.List;

public class Photo {

    private final String url;
    private final String name;
    private final Date modifiedDate;
    private final List<String> tags;
    private final double fileSize;

    public Photo(String url, String name, Date modifiedDate, List<String> tags, double fileSize) {
        this.url = url;
        this.name = name;
        this.modifiedDate = modifiedDate;
        this.tags = tags;
        this.fileSize = fileSize;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public List<String> getTags() {
        return tags;
    }

    public double getFileSize() {
        return fileSize;
    }

}
