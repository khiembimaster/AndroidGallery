package android21ktpm3.group07.androidgallery.ui.photos;



import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable{
    private String createdDate;
    private double byteImg;
    private int image;
    private String comment;



    public Image(String createdDate, double byteImg,int image){
        this.createdDate = createdDate;
        this.byteImg = byteImg;
        this.image = image;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public double getByteImg() {
        return byteImg;
    }

    public void setByteImg(double byteImg) {
        this.byteImg = byteImg;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(createdDate);
        dest.writeDouble(byteImg);
        dest.writeInt(image);
    }

    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    private Image(Parcel in) {
        createdDate = in.readString();
        byteImg = in.readDouble();
        image = in.readInt();
    }



}

