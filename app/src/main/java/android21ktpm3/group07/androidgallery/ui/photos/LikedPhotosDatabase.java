package android21ktpm3.group07.androidgallery.ui.photos;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.util.List;

@Database(entities = {LikedPhoto.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class LikedPhotosDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "liked_photos_database";
    private static volatile LikedPhotosDatabase instance;
    public abstract LikedPhotosDao likedPhotosDao();

}

