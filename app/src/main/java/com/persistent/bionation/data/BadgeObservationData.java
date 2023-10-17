package com.persistent.bionation.data;

public class BadgeObservationData {
    String urlToImage,title,time,commonName,isThreatened,observeCount;

    public BadgeObservationData(String urlToImage, String title, String time, String commonName, String isThreatened, String observeCount) {
        this.urlToImage = urlToImage;
        this.title = title;
        this.time = time;
        this.commonName = commonName;
        this.isThreatened = isThreatened;
        this.observeCount = observeCount;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getIsThreatened() {
        return isThreatened;
    }

    public void setIsThreatened(String isThreatened) {
        this.isThreatened = isThreatened;
    }

    public String getObserveCount() {
        return observeCount;
    }

    public void setObserveCount(String observeCount) {
        this.observeCount = observeCount;
    }
}
