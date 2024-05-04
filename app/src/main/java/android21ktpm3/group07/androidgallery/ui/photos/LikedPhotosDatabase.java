package android21ktpm3.group07.androidgallery.ui.photos;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {LikedPhoto.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class LikedPhotosDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "liked_photos_database";
    private static volatile LikedPhotosDatabase instance;

    public abstract LikedPhotosDao likedPhotosDao();

}

