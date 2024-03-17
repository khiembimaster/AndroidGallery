package android21ktpm3.group07.androidgallery;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.util.Pair;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import android21ktpm3.group07.androidgallery.ui.photos.Image;

public class Utilities {

    public static void createImage(Context context, int imageResourceId, ArrayList<Image> imgList) {
        // Lấy ngày giờ hiện tại
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Định dạng ngày giờ theo mong muốn
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = currentDateTime.format(formatter);

        double fileSize = getByteImage(context, imageResourceId);
        imgList.add(new Image(formattedDateTime,fileSize , imageResourceId));



    }
    public static double getByteImage(Context context, int imageResourceId){

        Resources resources = context.getResources();
        double fileSizeInKB = 0;
        try {
            // Lấy InputStream của hình ảnh từ resource
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(resources, imageResourceId, options);

            // Lấy kích thước hình ảnh trong đơn vị byte
            int fileSizeInBytes = options.outWidth * options.outHeight * 4; // Giả sử 4 bytes cho mỗi pixel

            // Chuyển đổi sang kilobyte
            fileSizeInKB = fileSizeInBytes / 1024.0;

            // In ra kết quả
            System.out.println("Kích thước hình ảnh: " + fileSizeInKB + " KB");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileSizeInKB;
    }

    public static Pair<ArrayList<String>, ArrayList<ArrayList<Image>>> groupImagesByDate(Context context, ArrayList<Image> imgList) {
        ArrayList<String> dates = new ArrayList<>();
        ArrayList<ArrayList<Image>> groupedImages = new ArrayList<>();

        // Duyệt qua từng hình ảnh
        for (Image image : imgList) {
            // Lấy ngày tháng năm của hình ảnh
            String date = image.getCreatedDate().substring(0, 10); // Lấy ngày/tháng/năm từ chuỗi định dạng

            // Nếu ngày/tháng/năm chưa có trong mảng date, thêm vào mảng date và tạo một nhóm mới trong mảng 2 chiều
            if (!dates.contains(date)) {
                dates.add(date);
                groupedImages.add(new ArrayList<>());
            }

            // Tìm vị trí của ngày/tháng/năm trong mảng date
            int index = dates.indexOf(date);

            // Thêm hình ảnh vào nhóm tương ứng trong mảng 2 chiều
            groupedImages.get(index).add(image);
        }

        // Trả về cặp (dates, groupedImages)
        return new Pair<>(dates, groupedImages);
    }
}
