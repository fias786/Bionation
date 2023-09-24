package com.persistent.bionation;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeoLocation {

    @SerializedName("coordinates")
    public List<Float> coordinatesList;
}
