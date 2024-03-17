package android21ktpm3.group07.androidgallery.ui.photos;

import android.content.Context;
import android.text.Layout;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android21ktpm3.group07.androidgallery.R;
import android21ktpm3.group07.androidgallery.models.Photo;

public class PhotosRecyclerAdapter extends RecyclerView.Adapter<PhotosRecyclerAdapter.ViewHolder> {
    private final Context context;
    private final List<Pair<LocalDate, List<Photo>>> groupedPhotos;

    public PhotosRecyclerAdapter(Context context, List<Pair<LocalDate, List<Photo>>> groupedPhotos) {
        this.context = context;
        this.groupedPhotos = groupedPhotos;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final TextView dateText;
        private final RecyclerView innerRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dateText = itemView.findViewById(R.id.date_text);
            innerRecyclerView = itemView.findViewById(R.id.image_by_date_recycler_view);

            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(itemView.getContext());
            layoutManager.setFlexDirection(FlexDirection.ROW);
            innerRecyclerView.setLayoutManager(layoutManager);
        }
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

        holder.dateText.setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        PhotoAdapter innerAdapter = new PhotoAdapter(context, photoList);
        holder.innerRecyclerView.setAdapter(innerAdapter);

        // TODO: Feed image data programmatically into this ViewHolder later
    }

    @Override
    public int getItemCount() {
        return groupedPhotos.size();
    }
}
