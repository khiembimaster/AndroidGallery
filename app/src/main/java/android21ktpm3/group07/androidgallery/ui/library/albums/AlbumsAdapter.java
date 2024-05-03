package android21ktpm3.group07.androidgallery.ui.library.albums;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

import android21ktpm3.group07.androidgallery.BR;
import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.databinding.AlbumContainerBinding;
import android21ktpm3.group07.androidgallery.models.Album;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {
    private final Context context;
    private final ObservableList<Album> albums;

    public AlbumsAdapter(Context context, ObservableList<Album> albums) {
        this.context = context;
        this.albums = albums;

        albums.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<Album>>() {
            @Override
            public void onChanged(ObservableList<Album> sender) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void onItemRangeChanged(ObservableList<Album> sender, int positionStart,
                                           int itemCount) {
                notifyItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeInserted(ObservableList<Album> sender, int positionStart,
                                            int itemCount) {
                notifyItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(ObservableList<Album> sender, int fromPosition,
                                         int toPosition, int itemCount) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onItemRangeRemoved(ObservableList<Album> sender, int positionStart,
                                           int itemCount) {
                notifyItemRangeRemoved(positionStart, itemCount);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        AlbumContainerBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.album_container, parent, false
        );

        return new ViewHolder(binding);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull AlbumsAdapter.ViewHolder holder, int position) {
        Album album = albums.get(position);

        holder.bind(album);
        
        holder.binding.imageView.setOnClickListener(v -> onItemClick(album));
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    private void onItemClick(Album album) {
        Intent intent = new Intent(context, AlbumActivity.class);
        intent.putExtra("albumBucketID", album.getBucketID());
        context.startActivity(intent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AlbumContainerBinding binding;

        public ViewHolder(AlbumContainerBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }

        public void bind(Album album) {
            binding.setVariable(BR.album, album);
            binding.executePendingBindings();
        }
    }
}
