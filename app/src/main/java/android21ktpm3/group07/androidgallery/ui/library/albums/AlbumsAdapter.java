package android21ktpm3.group07.androidgallery.ui.library.albums;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.models.Album;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {
    private final Context context;
    private final List<Album> albums;

    public AlbumsAdapter(Context context, List<Album> albums) {
        this.context = context;
        this.albums = albums;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tv_albumName;
        private final TextView tv_itemsCount;
        private final ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_albumName = itemView.findViewById(R.id.tv_albumName);
            tv_itemsCount = itemView.findViewById(R.id.tv_itemsCount);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.album_container, parent, false);

        return new ViewHolder(v);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull AlbumsAdapter.ViewHolder holder, int position) {
        Album album = albums.get(position);
        Log.d("AlbumsAdapter", "onBindViewHolder: " + album.getCoverPhotoPath());
        Glide.with(context)
                .load(album.getCoverPhotoPath())
                .centerCrop()
                .into(holder.imageView);

        holder.tv_albumName.setText(album.getName());
        // TODO: use string resource instead of hard code
        holder.tv_itemsCount.setText(
                String.format("%d %s", album.getSize(), "items"));
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }
}
