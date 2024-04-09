package android21ktpm3.group07.androidgallery.ui.photos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.helpers.DateHelper;
import android21ktpm3.group07.androidgallery.models.Photo;

public class PhotosRecyclerAdapter extends RecyclerView.Adapter<PhotosRecyclerAdapter.ViewHolder> {
    private final Context context;
    private final List<Photo> photos;
    private List<Item> groupedPhotos;
    private int imagesPerRow = 4;

    @Nullable
    public PhotoAdapter.OnItemSelectedListener childSelectedCB;
    @Nullable
    public PhotoAdapter.OnItemUnselectedListener childUnselectedCB;
    public PhotoAdapter.OnItemViewListener childViewCB;

    public PhotosRecyclerAdapter(Context context, List<Photo> photos) {
        this.context = context;
        this.photos = photos;

        groupPhotosByDate();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView dateText;
        private final RecyclerView innerRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dateText = itemView.findViewById(R.id.date_text);
            innerRecyclerView = itemView.findViewById(R.id.image_by_date_recycler_view);

            // Flexbox won't display all images: https://github.com/google/flexbox-layout/issues/420
            // FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(context);
            // layoutManager.setFlexDirection(FlexDirection.ROW);

            GridLayoutManager layoutManager = new GridLayoutManager(context, imagesPerRow);
            // LinearLayoutManager layoutManager = new LinearLayoutManager(context,
            //         LinearLayoutManager.VERTICAL, false);

            innerRecyclerView.setLayoutManager(layoutManager);
        }
    }

    public void setChildItemSelectedListener(PhotoAdapter.OnItemSelectedListener cb) {
        childSelectedCB = cb;
    }

    public void setChildItemUnselectedListener(PhotoAdapter.OnItemUnselectedListener cb) {
        childUnselectedCB = cb;
    }

    public void setChildItemViewListener(PhotoAdapter.OnItemViewListener cb) {
        childViewCB = cb;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View recylerView = inflater.inflate(R.layout.images_by_date_layout, parent, false);

        return new ViewHolder(recylerView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item cur = groupedPhotos.get(position);

        holder.dateText.setText(DateHelper.formatDate(cur.date, "dd/MM/yyyy"));

        if (cur.adapter == null) {
            PhotoAdapter adapter = new PhotoAdapter(context, cur.photos);
            adapter.setOnItemSelectedListener(childSelectedCB);
            adapter.setOnItemUnselectedListener(childUnselectedCB);
            adapter.setOnItemViewListener(childViewCB);

            cur.adapter = adapter;
        }

        holder.innerRecyclerView.setAdapter(cur.adapter);

        // PhotoAdapter adapter = new PhotoAdapter(context, cur.photos);
        // adapter.setOnItemSelectedListener(childSelectedCB);
        // adapter.setOnItemUnselectedListener(childUnselectedCB);
        // adapter.setOnItemViewListener(childViewCB);
        // holder.innerRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.innerRecyclerView.setAdapter(null);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return groupedPhotos.size();
    }

    private void groupPhotosByDate() {
        groupedPhotos = photos.stream()
                .sorted((photo1, photo2) -> Long.compare(
                        photo2.getRepresentativeEpoch(),
                        photo1.getRepresentativeEpoch()))
                .collect(Collectors.groupingBy(Photo::getRepresentativeDate))
                .entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getKey().compareTo(entry1.getKey()))
                .map(entry -> new Item(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private static class Item {
        public LocalDate date;
        public List<Photo> photos;
        public PhotoAdapter adapter;

        public Item(LocalDate date, List<Photo> photos) {
            this.date = date;
            this.photos = photos;
        }
    }
}
