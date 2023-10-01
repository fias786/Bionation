package com.persistent.bionation.ui.explore;

import com.google.gson.annotations.SerializedName;

public class Taxon {

    @SerializedName("name")
    public String scientificName;

    @SerializedName("preferred_common_name")
    public String commonName;

    @SerializedName("default_photo")
    public SpeciesPhoto speciesPhoto;

    @SerializedName("threatened")
    public String isThreatened;

    @SerializedName("wikipedia_summary")
    public String wikipediaSummary;

    @SerializedName("wikipedia_url")
    public String wikipedia_url;

    public static class SpeciesPhoto {

        @SerializedName("medium_url")
        public String photoMediumUrl;

        @SerializedName("square_url")
        public String photoSquareUrl;

        @SerializedName("url")
        public String photoUrl;

    }
}
