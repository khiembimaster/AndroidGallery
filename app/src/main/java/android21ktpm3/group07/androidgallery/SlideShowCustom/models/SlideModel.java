package android21ktpm3.group07.androidgallery.SlideShowCustom.models;

import android.media.MediaPlayer;
import android.net.Uri;

import android21ktpm3.group07.androidgallery.SlideShowCustom.constants.ScaleTypes;

public class SlideModel {
    private String imageUrl;
    private Integer imagePath;
    private Uri imageUri;
    private String title;
    private ScaleTypes scaleType;


    public SlideModel(String imageUrl, String title, ScaleTypes scaleType) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.scaleType = scaleType;
    }

    public SlideModel(Uri imageUri, String title, ScaleTypes scaleType) {
        this.imageUri = imageUri;
        this.title = title;
        this.scaleType = scaleType;
    }




    public SlideModel(Integer imagePath, String title, ScaleTypes scaleType) {
        this.imagePath = imagePath;
        this.title = title;
        this.scaleType = scaleType;
    }


    public SlideModel(String imageUrl, ScaleTypes scaleType) {
        this.imageUrl = imageUrl;
        this.scaleType = scaleType;
    }

    public SlideModel(Uri imageUri, ScaleTypes scaleType) {
        this.imageUri = imageUri;
        this.scaleType = scaleType;
    }



    public SlideModel(Integer imagePath, ScaleTypes scaleType) {
        this.imagePath = imagePath;
        this.scaleType = scaleType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getImagePath() {
        return imagePath;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public Uri getAudioUri() {
        return getAudioUri();
    }


    public String getTitle() {
        return title;
    }

    public ScaleTypes getScaleType() {
        return scaleType;
    }
}