package android21ktpm3.group07.androidgallery.ui.photos;

import androidx.room.TypeConverter;

public class Converters {
    @TypeConverter
    public static String fromLikedPhoto(LikedPhoto likedPhoto) {
        return likedPhoto == null ? null : likedPhoto.getPhotoUrl();
    }

    @TypeConverter
    public static LikedPhoto toLikedPhoto(String photoUrl) {
        return photoUrl == null ? null : new LikedPhoto(photoUrl);
    }
}
