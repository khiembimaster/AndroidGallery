package android21ktpm3.group07.androidgallery.ui.photos;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "liked_photos")
public class LikedPhoto {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "photo_url")
    private String photoUrl;

    public LikedPhoto(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}

