package android21ktpm3.group07.androidgallery.ui.photos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
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
    private List<Pair<LocalDate, List<Photo>>> groupedPhotos;
    private int innerViewsPerRow = 4;

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
            // FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(itemView.getContext());
            // layoutManager.setFlexDirection(FlexDirection.ROW);

            GridLayoutManager layoutManager = new GridLayoutManager(
                    itemView.getContext(),
                    innerViewsPerRow
            );

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
        // TODO: Refactor later, this is a mess :v
        LocalDate date = groupedPhotos.get(position).first;
        List<Photo> photoList = groupedPhotos.get(position).second;

        holder.dateText.setText(DateHelper.formatDate(date, "dd/MM/yyyy"));

        PhotoAdapter innerAdapter = new PhotoAdapter(context, photoList, innerViewsPerRow);
        innerAdapter.setOnItemSelectedListener(childSelectedCB);
        innerAdapter.setOnItemUnselectedListener(childUnselectedCB);
        innerAdapter.setOnItemViewListener(childViewCB);

        holder.innerRecyclerView.setAdapter(innerAdapter);
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
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
