package com.persistent.bionation.ui.camera;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Observation {
    @SerializedName("total_results")
    public Integer totalResult;
    @SerializedName("page")
    public Integer page;
    @SerializedName("per_page")
    public Integer perPage;
    @SerializedName("results")
    public List<ObservationItems> observationItems;
}
