package com.persistent.bionation.data;

public class ObservationImageData {

    String largeImage,firstSmallImage,secondSmallImage;

    public ObservationImageData(String largeImage, String firstSmallImage, String secondSmallImage) {
        this.largeImage = largeImage;
        this.firstSmallImage = firstSmallImage;
        this.secondSmallImage = secondSmallImage;
    }

    public String getLargeImage() {
        return largeImage;
    }

    public void setLargeImage(String largeImage) {
        this.largeImage = largeImage;
    }

    public String getFirstSmallImage() {
        return firstSmallImage;
    }

    public void setFirstSmallImage(String firstSmallImage) {
        this.firstSmallImage = firstSmallImage;
    }

    public String getSecondSmallImage() {
        return secondSmallImage;
    }

    public void setSecondSmallImage(String secondSmallImage) {
        this.secondSmallImage = secondSmallImage;
    }
}
