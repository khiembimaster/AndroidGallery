package android21ktpm3.group07.androidgallery.ui.photos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;

import android21ktpm3.group07.androidgallery.R;

public class PhotosRecyclerAdapter extends RecyclerView.Adapter<PhotosRecyclerAdapter.ViewHolder> {
    final private String[] dates = {"1", "2","3","4"};
    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView date;
//        Flow imageFlow;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date_text);
//            imageFlow = itemView.findViewById(R.id.images_flow);
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
        holder.date.setText(dates[position]);
    }

    @Override
    public int getItemCount() {
        return dates.length;
    }
}
