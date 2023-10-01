package com.persistent.bionation.ui.camera;

import java.util.List;

public class SpeciesObject {
    public double score=0.0;
    public String name = "";
    public int taxon_id= 0;
    public float rank= 0;
    public List<Integer> ancestor_ids = null;

    public SpeciesObject(double score, String name, int taxon_id, float rank, List<Integer> ancestor_ids) {
        this.score = score;
        this.name = name;
        this.taxon_id = taxon_id;
        this.rank = rank;
        this.ancestor_ids = ancestor_ids;
    }

    @Override
    public String toString() {
        return "SpeciesObject{" +
                "score=" + score +
                ", name='" + name + '\'' +
                ", taxon_id=" + taxon_id +
                ", rank=" + rank +
                ", ancestor_ids=" + ancestor_ids +
                '}';
    }
}
