package android21ktpm3.group07.androidgallery.ui.photos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LikedPhotosDao {
    @Query("SELECT * FROM liked_photos")
    List<LikedPhoto> getAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(LikedPhoto likedPhoto);

    @Delete
    void delete(LikedPhoto likedPhoto);
}
