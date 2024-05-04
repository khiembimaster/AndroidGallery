package android21ktpm3.group07.androidgallery.Utils.SlideShowCustom.constants;



public enum ScaleTypes {
    FIT("fit"),
    CENTER_CROP("centerCrop"),
    CENTER_INSIDE("centerInside");

    private final String value;

    ScaleTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
