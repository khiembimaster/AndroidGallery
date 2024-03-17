package android21ktpm3.group07.androidgallery.ui.photos;

import android.content.Context;
import android.util.Pair;
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
import android21ktpm3.group07.androidgallery.Utilities;


public class PhotosRecyclerAdapter extends RecyclerView.Adapter<PhotosRecyclerAdapter.ViewHolder> {
    final private String[] dates;
    final private int[][] images = {
            {R.drawable.avatar01, R.drawable.avatar02,  R.drawable.avatar03,  R.drawable.avatar04,  R.drawable.avatar05},
            { R.drawable.avatar01,  R.drawable.avatar09, R.drawable.avatar06, R.drawable.avatar07,  R.drawable.avatar08},
            {R.drawable.avatar01, R.drawable.avatar04,  R.drawable.avatar05},
            {R.drawable.avatar01, R.drawable.avatar02,  R.drawable.avatar03,  R.drawable.avatar04,  R.drawable.avatar05, R.drawable.avatar05, R.drawable.avatar05, R.drawable.avatar05, R.drawable.avatar05,R.drawable.avatar05, R.drawable.avatar05, R.drawable.avatar05, R.drawable.avatar05, R.drawable.avatar05, R.drawable.avatar05, },
    };
    private ArrayList<Image> imgList = new ArrayList<>();
    private final ArrayList<ArrayList<Image>> groupedImages;
    private Context mContext;
    private PhotosFragment photosFragment;

    public PhotosRecyclerAdapter(Context context, PhotosFragment photosFragment) {
        this.mContext = context;
        this.photosFragment = photosFragment;
        // Gọi phương thức createImage trong constructor và truyền context
        for (int i = 0; i < images.length; i++) {
            for (int j = 0; j < images[i].length; j++) {
                Utilities.createImage(context, images[i][j], imgList);

            }
        }
        Pair<ArrayList<String>, ArrayList<ArrayList<Image>>> result = Utilities.groupImagesByDate(context, imgList);

        // Gán các phần tử trong kết quả trả về vào các biến dates và groupedImages
        dates = result.first.toArray(new String[0]);
        groupedImages = result.second;
        System.out.println("khoi tao toan mang");
    }









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

        Image image = imgList.get(position);
        // TODO: Refactor later, this is a mess :v
        holder.date.setText(dates[position]);
        holder.innerAdapter = new PhotoAdapter(mContext,photosFragment,groupedImages.get(position));
        holder.innerRecyclerView.setAdapter(holder.innerAdapter);

        // TODO: Feed image data programmatically into this ViewHolder later
    }

    @Override
    public int getItemCount() {
        return dates.length;
    }

    public ArrayList<Image> getImgList() {
        return imgList;
    }
    public void updateImgList(ArrayList<Image> newImgList) {
        imgList = newImgList;
        notifyDataSetChanged(); // Thông báo cho adapter biết rằng dữ liệu đã thay đổi
    }




}
