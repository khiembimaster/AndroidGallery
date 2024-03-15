package android21ktpm3.group07.androidgallery.ui.photos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.ArrayList;

import android21ktpm3.group07.androidgallery.R;

public class PhotosRecyclerAdapter extends RecyclerView.Adapter<PhotosRecyclerAdapter.ViewHolder> {
    final private String[] dates = {"1", "2","3","4"};
    final private int[][] images = {
            {R.drawable.avatar01, R.drawable.avatar02,  R.drawable.avatar03,  R.drawable.avatar04,  R.drawable.avatar05},
            { R.drawable.avatar01,  R.drawable.avatar09, R.drawable.avatar06, R.drawable.avatar07,  R.drawable.avatar08},
            {R.drawable.avatar01, R.drawable.avatar04,  R.drawable.avatar05},
            {R.drawable.avatar01, R.drawable.avatar02,  R.drawable.avatar03,  R.drawable.avatar04,  R.drawable.avatar05, R.drawable.avatar05, R.drawable.avatar05, R.drawable.avatar05, R.drawable.avatar05,R.drawable.avatar05, R.drawable.avatar05, R.drawable.avatar05, R.drawable.avatar05, R.drawable.avatar05, R.drawable.avatar05, },
    };
    private final ArrayList<Image> imgList = new ArrayList<>();

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView date;
        RecyclerView innerRecyclerView;
        RecyclerView.Adapter innerAdapter;

//        Flow imageFlow;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date_text);
            innerRecyclerView = itemView.findViewById(R.id.image_by_date_recycler_view);
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(itemView.getContext());
            layoutManager.setFlexDirection(FlexDirection.ROW);



//            LinearLayoutManager layoutManager = new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);
//            GridLayoutManager layoutManager = new GridLayoutManager(itemView.getContext(), 3, GridLayoutManager.VERTICAL, false);

            innerRecyclerView.setLayoutManager(layoutManager);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.images_by_date_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // TODO: Refactor later, this is a mess :v
        holder.date.setText(dates[position]);
        holder.innerAdapter = new PhotoAdapter(images[position]);
        holder.innerRecyclerView.setAdapter(holder.innerAdapter);

        // TODO: Feed image data programmatically into this ViewHolder later
    }

    @Override
    public int getItemCount() {
        return dates.length;
    }
}
