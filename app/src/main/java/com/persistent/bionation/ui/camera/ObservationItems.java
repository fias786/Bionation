package com.persistent.bionation.ui.camera;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ObservationItems {

    @SerializedName("taxon")
    public Taxon taxon;

    @SerializedName("photos")
    public List<Photos> photosList;

}
