package com.persistent.bionation.data;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class BadgesDataObject extends RealmObject {
    @PrimaryKey
    public String id;
    @Required
    public String scientific_name;
    @Required
    public String imageUrl;
    @Required
    public String time;
    @Required
    public String commonName;
    @Required
    public String isThreatened;
    @Required
    public String observeCount;

    public BadgesDataObject(){

    }

    public BadgesDataObject(String id, String scientific_name, String imageUrl, String time, String commonName, String isThreatened, String observeCount) {
        this.id = id;
        this.scientific_name = scientific_name;
        this.imageUrl = imageUrl;
        this.time = time;
        this.commonName = commonName;
        this.isThreatened = isThreatened;
        this.observeCount = observeCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getScientific_name() {
        return scientific_name;
    }

    public void setScientific_name(String scientific_name) {
        this.scientific_name = scientific_name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
